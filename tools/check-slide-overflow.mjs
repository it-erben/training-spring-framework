#!/usr/bin/env node
/**
 * Prueft Marp-Decks darauf, ob Folieninhalt ueber die 1280x720-Buehne
 * hinauslaeuft und im PDF abgeschnitten wird.
 *
 *   node tools/check-slide-overflow.mjs slides/00_Spring_Core/slides.md ...
 *   node tools/check-slide-overflow.mjs --json slides/00_Spring_Core/slides.md
 *
 * Exit-Code 1, sobald eine Folie ueberlaeuft.
 *
 * Braucht `marp` im PATH und eine Chrome-Binary. Der Chrome-Pfad kommt aus
 * $CHROME_PATH, sonst aus dem Puppeteer-Cache oder den Standardpfaden.
 * Keine npm-Abhaengigkeiten: die Steuerung laeuft ueber das
 * DevTools-Protokoll mit den in Node 22 eingebauten fetch und WebSocket.
 */
import { execFileSync, spawn } from 'node:child_process';
import { mkdtempSync, readFileSync, writeFileSync, existsSync, readdirSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, basename, dirname, resolve } from 'node:path';

// Toleranz in CSS-Pixeln. Darunter beruehrt der Inhalt die Kante, wird aber
// nicht sichtbar angeschnitten.
const TOLERANCE = 2;
const NAV_TIMEOUT_MS = 60_000;

const CHROME_CANDIDATES = [
  '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  '/Applications/Chromium.app/Contents/MacOS/Chromium',
  '/usr/bin/google-chrome',
  '/usr/bin/chromium',
  '/usr/bin/chromium-browser',
];

function findChrome() {
  if (process.env.CHROME_PATH) return process.env.CHROME_PATH;
  const cache = join(process.env.HOME ?? '', '.cache/puppeteer/chrome');
  if (existsSync(cache)) {
    for (const rev of readdirSync(cache).sort().reverse()) {
      for (const rel of [
        'chrome-mac-arm64/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing',
        'chrome-mac-x64/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing',
        'chrome-linux64/chrome',
      ]) {
        const p = join(cache, rev, rel);
        if (existsSync(p)) return p;
      }
    }
  }
  const hit = CHROME_CANDIDATES.find(existsSync);
  if (hit) return hit;
  throw new Error('Keine Chrome-Binary gefunden. $CHROME_PATH setzen.');
}

/**
 * Laeuft in der Seite und liefert erst, wenn das Layout steht.
 *
 * Marp rendert Codebloecke als <marp-pre>-Custom-Element, das seine echte
 * Hoehe erst nach dem Upgrade bekommt; vorher meldet jeder Block konstant
 * 184px. Eine feste Wartezeit rennt gegen dieses Upgrade und liefert von
 * Lauf zu Lauf andere Zahlen, deshalb wird gepollt, bis sich die Hoehen
 * ueber mehrere Runden nicht mehr aendern. Das Polling braucht echte Zeit,
 * darum CDP statt --virtual-time-budget.
 */
const PROBE = `new Promise(function (done, fail) {
  var STABLE_ROUNDS = 6, POLL_MS = 100, DEADLINE = Date.now() + 45000;
  // Mindestlaufzeit zusaetzlich zur Stabilitaet: unter Last kann das
  // Layout eine halbe Sekunde lang auf einem Zwischenzustand ruhen,
  // bevor die Schrift wirklich angewandt ist.
  var MIN_SETTLE_MS = 1200, started = Date.now();
  var last = null, stable = 0;

  // Nur Sections mit id sind echte Folien. Marpit legt fuer jede
  // ![bg]-Grafik zwei weitere Container an ("background" und "pseudo"),
  // die keine Seite erzeugen. Mitgezaehlt melden sie eine zu hohe
  // Folienzahl und verschieben die Zuordnung von Messwert zu Nummer:
  // ihre id ist leer, Number('') ergibt 0.
  function sections() { return document.querySelectorAll('section[id]'); }
  function snapshot() {
    return [].map.call(sections(), function (s) {
      return s.scrollHeight + 'x' + s.clientHeight;
    }).join(',');
  }

  function tick() {
    var cur = snapshot();
    stable = cur === last ? stable + 1 : 0;
    last = cur;
    if (stable >= STABLE_ROUNDS && Date.now() - started >= MIN_SETTLE_MS) {
      return done([].map.call(sections(), function (s) {
        var cs = getComputedStyle(s);
        return {
          slide: Number(s.id),
          box: s.clientHeight,
          content: s.scrollHeight,
          over: s.scrollHeight - s.clientHeight,
          // Fuer die Korrektur: Innenmass und wirksame Schriftgroesse, damit
          // die Dichte-Stufen nicht auf Theme-Annahmen festgenagelt sind.
          pad: Math.round((parseFloat(cs.paddingTop) || 0) + (parseFloat(cs.paddingBottom) || 0)),
          font: parseFloat(cs.fontSize) || 0
        };
      }));
    }
    if (Date.now() > DEADLINE) return fail(new Error('Layout kam nicht zur Ruhe'));
    setTimeout(tick, POLL_MS);
  }

  function start() {
    var pending = [];
    if (document.fonts) pending.push(document.fonts.ready);
    if (document.querySelector('marp-pre') && window.customElements) {
      pending.push(customElements.whenDefined('marp-pre'));
    }
    Promise.all(pending).then(tick, tick);
  }

  if (document.readyState === 'complete') start();
  else window.addEventListener('load', start);
})`;

class Cdp {
  constructor(ws) {
    this.ws = ws;
    this.id = 0;
    this.waiting = new Map();
    ws.addEventListener('message', (e) => {
      const msg = JSON.parse(e.data);
      const slot = this.waiting.get(msg.id);
      if (!slot) return;
      this.waiting.delete(msg.id);
      msg.error ? slot.reject(new Error(msg.error.message)) : slot.resolve(msg.result);
    });
  }

  send(method, params = {}, sessionId) {
    const id = ++this.id;
    return new Promise((res, rej) => {
      this.waiting.set(id, { resolve: res, reject: rej });
      this.ws.send(JSON.stringify({ id, method, params, sessionId }));
    });
  }
}

function launchChrome(chrome) {
  const proc = spawn(chrome, [
    '--headless',
    '--disable-gpu',
    '--no-sandbox',
    '--hide-scrollbars',
    '--window-size=1280,720',
    '--remote-debugging-port=0',
    '--user-data-dir=' + mkdtempSync(join(tmpdir(), 'slide-chrome-')),
    'about:blank',
  ]);
  return new Promise((res, rej) => {
    let buf = '';
    const to = setTimeout(() => rej(new Error('Chrome meldete keinen Debug-Port')), 30_000);
    proc.stderr.on('data', (d) => {
      buf += d;
      const m = buf.match(/DevTools listening on (ws:\/\/\S+)/);
      if (m) {
        clearTimeout(to);
        res({ proc, wsUrl: m[1] });
      }
    });
    proc.on('exit', (c) => rej(new Error(`Chrome beendete sich mit ${c}`)));
  });
}

function connect(url) {
  return new Promise((res, rej) => {
    const ws = new WebSocket(url);
    ws.addEventListener('open', () => res(ws));
    ws.addEventListener('error', () => rej(new Error('WebSocket zu Chrome fehlgeschlagen')));
  });
}

function render(mdPath, work) {
  const abs = resolve(mdPath);
  // Alle Decks heissen slides.md, deshalb den Modulordner in den Namen ziehen.
  const stem = `${basename(dirname(abs))}-${basename(abs, '.md')}`;
  const html = join(work, stem + '.html');
  execFileSync('marp', ['--html', '--template', 'bare', '--allow-local-files', '-o', html, abs], {
    stdio: ['ignore', 'ignore', 'pipe'],
  });
  return html;
}

async function main() {
  const args = process.argv.slice(2);
  const asJson = args.includes('--json');
  const files = args.filter((a) => !a.startsWith('--'));
  if (files.length === 0) {
    console.error('Aufruf: node tools/check-slide-overflow.mjs <slides.md> [...]');
    process.exit(2);
  }

  const work = mkdtempSync(join(tmpdir(), 'slide-overflow-'));
  const { proc, wsUrl } = await launchChrome(findChrome());
  const cdp = new Cdp(await connect(wsUrl));
  const all = {};

  async function measureOnce(f, html) {
    const { targetId } = await cdp.send('Target.createTarget', { url: 'about:blank' });
    try {
      const { sessionId } = await cdp.send('Target.attachToTarget', { targetId, flatten: true });
      await cdp.send('Page.enable', {}, sessionId);
      await cdp.send('Page.navigate', { url: 'file://' + html }, sessionId);
      const { result, exceptionDetails } = await Promise.race([
        cdp.send('Runtime.evaluate', { expression: PROBE, awaitPromise: true, returnByValue: true }, sessionId),
        new Promise((_, rej) => setTimeout(() => rej(new Error(`Zeitueberschreitung bei ${f}`)), NAV_TIMEOUT_MS)),
      ]);
      if (exceptionDetails) throw new Error(`${f}: ${exceptionDetails.text}`);
      return result.value;
    } finally {
      await cdp.send('Target.closeTarget', { targetId }).catch(() => {});
    }
  }

  try {
    for (const f of files) {
      const html = render(f, work);
      // Der erste Anlauf kann an einer traegen Ziel-Erzeugung scheitern.
      // Ein zweiter Versuch ist billiger als ein falsch-roter Lauf.
      for (let attempt = 1; ; attempt++) {
        try {
          all[f] = await measureOnce(f, html);
          break;
        } catch (e) {
          if (attempt >= 2) throw e;
          console.error(`  (Wiederholung nach: ${e.message})`);
        }
      }
    }
  } finally {
    cdp.ws.close();
    proc.kill();
  }

  const failing = [];
  for (const f of files) for (const r of all[f]) if (r.over > TOLERANCE) failing.push(r);

  if (asJson) {
    console.log(JSON.stringify(all, null, 2));
  } else {
    for (const f of files) {
      const bad = all[f].filter((r) => r.over > TOLERANCE);
      console.log(`${f}  ${all[f].length} Folien, ${bad.length} mit Ueberlauf`);
      for (const r of bad) {
        console.log(`  Folie ${String(r.slide).padStart(2)}  +${r.over}px  (Inhalt ${r.content} / Box ${r.box})`);
      }
    }
  }
  // process.exit() verwirft stdout, das noch nicht geschrieben ist. Sobald
  // --json ueber mehrere Decks den Pipe-Puffer von 64 KB fuellt, kommt die
  // Ausgabe abgeschnitten an. Exit-Code setzen und Node regulaer beenden
  // lassen, damit der Puffer vorher leerlaeuft.
  process.exitCode = failing.length ? 1 : 0;
}

main().catch((e) => {
  console.error(e.message);
  process.exitCode = 2;
});
