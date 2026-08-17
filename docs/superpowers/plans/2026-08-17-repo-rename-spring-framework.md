# Umbenennung nach gfu/spring-framework — Implementierungsplan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Das GitLab-Projekt `it-erben/gfu/spring-boot-advanced` heißt
`it-erben/gfu/spring-framework`, und Terraform, lokale Arbeitskopie sowie
die davon sprechenden Dokumente stimmen wieder.

**Architecture:** Der Rename passiert zuerst über die GitLab-API. Terraform
zieht anschließend nur den State nach — Map-Key, `import`-Block und ein
`moved`-Block —, sodass der Apply keine API-Änderung auslöst. Danach folgen
lokale Arbeitskopie, Dokumente im Kurs-Repository und die Beschriftung in
`components/pdf-publisher`.

**Tech Stack:** GitLab REST API v4 über `glab api`, Terraform mit Provider
`gitlabhq/gitlab ~> 17.0`, Git-Worktrees, `pre-commit` mit markdownlint,
yamllint und lychee.

**Spec:** `docs/superpowers/specs/2026-08-17-repo-rename-spring-framework-design.md`

## Global Constraints

- Zielpfad: `gfu/spring-framework`. Anzeigename: `Spring Framework`.
  Beschreibung: `Codebeispiele, Übungen und Folien zur Spring-Schulung (Basis und Advanced)`.
- Die Projekt-ID `76899678` bleibt über den Rename stabil. Alle API-Aufrufe
  adressieren das Projekt über diese ID, nicht über den Pfad.
- Der `moved`-Block in Terraform ist zwingend. Der Map-Key ist Teil der
  Resource-Adresse; ohne ihn plant Terraform destroy und create und scheitert
  an `prevent_destroy`.
- Ein Terraform-Plan, der eine API-Änderung an
  `gitlab_project.this["gfu/spring-framework"]` zeigt, ist ein
  Abbruchkriterium — nicht applyen, Ursache klären.
- `lifecycle.ignore_changes` bleibt unverändert. `name` dort zu entfernen
  ließe Terraform die Namen aller verwalteten Repositories neu setzen.
- Unverändert bleiben die Maven-Koordinaten (`tech.erben:sb-training`, Module
  `sb-advanced-*` und `sb-basics-*`) und `slides/template.html` im
  Kurs-Repository („Spring Boot Schulung").
- Commit-Nachrichten: conventional commit, Imperativ, Betreffzeile ≤ 72
  Zeichen, Umlaute ausgeschrieben statt transkribiert.
- `glab` leitet den Zielhost aus dem Git-Verzeichnis des Arbeitsverzeichnisses
  ab. Alle `glab`-Aufrufe laufen innerhalb eines Klons der Gruppe `it-erben`.

## Betroffene Dateien

Kurs-Repository (`gfu/spring-framework`, ehemals `gfu/spring-boot-advanced`):

- Modify: `docs/superpowers/specs/2026-07-28-spring-boot-basis-teil-design.md:32-34`
  — nennt `gfu/spring-boot` als Zielnamen.
- Modify: `docs/superpowers/plans/2026-07-28-spring-boot-basis-teil.md:214`
  — `cd` auf den alten lokalen Verzeichnisnamen.
- Modify: `docs/superpowers/plans/2026-07-28-spring-boot-basis-teil.md:2365-2376`
  — Schritt 9 beschreibt den Rename mit falschem Zielnamen und einem
  `glab`-Aufruf, den es so nicht gibt.

`internal/repository-setup`:

- Modify: `projects.tf:51-55` — Map-Key, `name`, `description`.
- Modify: `imports.tf:34-37` — Zieladresse des `import`-Blocks.
- Modify: `imports.tf:127-131` — `moved`-Block im Abschnitt `# Renames`.

`components/pdf-publisher`:

- Modify: `README.md:3` — falsche Aussage über den Zweck des Repositories.
- Modify: `README.md:8` und `README.md:31` — verweisen auf
  `templates/pdf-pages/template.yml`; die Datei heißt
  `templates/build-publish/template.yml`.
- Modify: `slides/template.html:6,44` — Titel und Überschrift der Fixture.
- Modify: `slides/00_Microservices_Architecture/slides.md:4` und
  `slides/01_Configuration/slides.md:4` — `header:` der Fixture.

Keine Datei wird angelegt oder gelöscht.

---

### Task 1: feat/basis-teil nach main mergen

Der Rename darf nicht in einen offenen Merge Request fallen. Diese Task
bringt den Branch nach `main` und macht den Worktree entbehrlich.

**Files:**

- Modify: keine — reine Git- und GitLab-Operationen.

**Interfaces:**

- Consumes: nichts.
- Produces: `origin/main` enthält `feat/basis-teil`; der Worktree
  `.claude/worktrees/basis-teil` wird von keiner Task mehr gebraucht.

- [ ] **Schritt 1: Ausgangszustand prüfen**

```bash
cd /Users/aerben/repositories/it-erben/gfu/spring-boot-advanced/.claude/worktrees/basis-teil
git status --short
git log --oneline -1
```

Erwartung: leerer `git status`. Ist er es nicht, die offenen Änderungen erst
committen oder verwerfen — der Merge darf keinen Zwischenstand mitnehmen.

- [ ] **Schritt 2: Branch pushen**

```bash
git push origin feat/basis-teil
```

Erwartung: `Everything up-to-date` oder ein erfolgreicher Push der lokalen
Commits.

- [ ] **Schritt 3: Prüfen, ob schon ein Merge Request offen ist**

```bash
glab mr list --source-branch feat/basis-teil
```

Erwartung: entweder ein bestehender Merge Request — dann Schritt 4
überspringen und dessen IID merken — oder `No open merge requests`.

- [ ] **Schritt 4: Merge Request anlegen**

Nur ausführen, wenn Schritt 3 keinen offenen Merge Request gezeigt hat.

```bash
glab mr create \
  --source-branch feat/basis-teil \
  --target-branch main \
  --title "feat: Basis-Teil für die Spring-Schulung" \
  --description "Setzt docs/superpowers/specs/2026-07-28-spring-boot-basis-teil-design.md um: sechs Basis-Module, Umnummerierung der Advanced-Slides auf 10_ bis 17_, Maven-Koordinaten auf tech.erben:sb-training." \
  --remove-source-branch \
  --yes
```

- [ ] **Schritt 5: Pipeline abwarten**

```bash
glab ci status --branch feat/basis-teil
```

Erwartung: alle Jobs `success`. Bei `failed` hier abbrechen — der Rename
setzt einen grünen Stand voraus.

- [ ] **Schritt 6: Mergen**

Ohne IID löst `glab` den Merge Request aus dem aktuellen Branch auf.

```bash
glab mr merge --yes
```

- [ ] **Schritt 7: Merge verifizieren**

```bash
git fetch origin
git log --oneline origin/main -1
git rev-list --left-right --count origin/main...origin/feat/basis-teil
```

Erwartung: der Zählerstand endet auf `0` für die rechte Seite — `main` enthält
alle Commits des Branches.

---

### Task 2: GitLab-Projekt umbenennen

**Files:**

- Modify: keine — reiner API-Aufruf.

**Interfaces:**

- Consumes: `origin/main` aus Task 1.
- Produces: Projekt `76899678` liegt unter `it-erben/gfu/spring-framework`.
  Tasks 3 bis 6 setzen diesen Pfad voraus.

- [ ] **Schritt 1: Ausgangszustand festhalten**

```bash
glab api projects/76899678 | jq -r '.path_with_namespace, .name, .description'
```

Erwartung:

```text
it-erben/gfu/spring-boot-advanced
Spring Boot Advanced
Codebeispiele, Übungen und Folien für die Spring Boot-Schulung
```

Weicht das ab, wurde der Rename schon ganz oder teilweise ausgeführt — dann
mit Schritt 3 fortfahren statt blind zu überschreiben.

- [ ] **Schritt 2: Pfad, Name und Beschreibung setzen**

`glab repo update` kennt weder `--path` noch `--name`, nur `--description`,
`--defaultBranch` und `--archive`. Der Aufruf geht deshalb direkt gegen die
API:

```bash
glab api projects/76899678 --method PUT \
  --field path=spring-framework \
  --field name='Spring Framework' \
  --field description='Codebeispiele, Übungen und Folien zur Spring-Schulung (Basis und Advanced)'
```

- [ ] **Schritt 3: Ergebnis verifizieren**

```bash
glab api projects/76899678 | jq -r '.path_with_namespace, .name, .description'
```

Erwartung:

```text
it-erben/gfu/spring-framework
Spring Framework
Codebeispiele, Übungen und Folien zur Spring-Schulung (Basis und Advanced)
```

- [ ] **Schritt 4: Weiterleitung prüfen**

```bash
glab api projects/it-erben%2Fgfu%2Fspring-boot-advanced | jq -r '.id, .path_with_namespace'
```

Erwartung: `76899678` und `it-erben/gfu/spring-framework` — GitLab löst den
alten Pfad über die Weiterleitung auf. Diese hält nur, bis der alte Pfad neu
belegt wird, und ersetzt daher nicht Task 4.

---

### Task 3: Terraform nachziehen

Schließt die Abweichung zwischen GitLab und dem Terraform-State direkt nach
dem Rename.

**Files:**

- Modify: `/Users/aerben/repositories/it-erben/internal/repository-setup/projects.tf:51-55`
- Modify: `/Users/aerben/repositories/it-erben/internal/repository-setup/imports.tf:34-37`
- Modify: `/Users/aerben/repositories/it-erben/internal/repository-setup/imports.tf:127-131`

**Interfaces:**

- Consumes: den neuen Pfad aus Task 2.
- Produces: Resource-Adresse `gitlab_project.this["gfu/spring-framework"]`.

- [ ] **Schritt 1: Branch anlegen**

```bash
cd /Users/aerben/repositories/it-erben/internal/repository-setup
git checkout main
git pull
git checkout -b rename/spring-framework
```

- [ ] **Schritt 2: Map-Eintrag in `projects.tf` umschreiben**

Ersetzen:

```hcl
    "gfu/spring-boot-advanced" = {
      name        = "Spring Boot Advanced"
      description = "Codebeispiele, Übungen und Folien für die Spring Boot-Schulung"
      visibility  = "public"
    }
```

durch:

```hcl
    "gfu/spring-framework" = {
      name        = "Spring Framework"
      description = "Codebeispiele, Übungen und Folien zur Spring-Schulung (Basis und Advanced)"
      visibility  = "public"
    }
```

`name` und `description` stehen in `ignore_changes` und werden von Terraform
nicht durchgesetzt; sie werden trotzdem korrekt geführt, damit die
Konfiguration den Bestand beschreibt.

- [ ] **Schritt 3: `import`-Block in `imports.tf` umschreiben**

Ersetzen:

```hcl
import {
  to = gitlab_project.this["gfu/spring-boot-advanced"]
  id = "76899678"
}
```

durch:

```hcl
import {
  to = gitlab_project.this["gfu/spring-framework"]
  id = "76899678"
}
```

- [ ] **Schritt 4: `moved`-Block ergänzen**

Im Abschnitt `# Renames` von `imports.tf`, direkt unter den bestehenden Block
für `components/gfu-deploy`:

```hcl
moved {
  from = gitlab_project.this["gfu/spring-boot-advanced"]
  to   = gitlab_project.this["gfu/spring-framework"]
}
```

- [ ] **Schritt 5: Prüfen, dass kein alter Schlüssel übrig ist**

```bash
grep -rn "spring-boot-advanced" *.tf
```

Erwartung: genau ein Treffer, die `from`-Zeile des `moved`-Blocks.

- [ ] **Schritt 6: Formatierung und Syntax prüfen**

```bash
terraform fmt -check
terraform init -backend=false
terraform validate
```

Erwartung: `fmt -check` gibt nichts aus, `validate` meldet
`Success! The configuration is valid.` Der Backend-Zugriff bleibt außen vor —
der echte Plan läuft in der CI.

- [ ] **Schritt 7: Committen**

```bash
git add projects.tf imports.tf
git commit -m "Rename gfu/spring-boot-advanced to gfu/spring-framework"
```

- [ ] **Schritt 8: Merge Request anlegen**

```bash
git push -u origin rename/spring-framework
glab mr create \
  --source-branch rename/spring-framework \
  --target-branch main \
  --title "Rename gfu/spring-boot-advanced to gfu/spring-framework" \
  --description "Zieht den bereits in GitLab ausgeführten Rename nach. Der moved-Block verhindert, dass der geänderte Map-Key als destroy und create geplant wird." \
  --yes
```

- [ ] **Schritt 9: Plan der CI lesen**

```bash
glab ci status --branch rename/spring-framework
glab ci trace --branch rename/spring-framework
```

Erwartung im `plan`-Job: der `moved`-Umzug von
`gitlab_project.this["gfu/spring-boot-advanced"]` nach
`gitlab_project.this["gfu/spring-framework"]` und danach
`No changes. Your infrastructure matches the configuration.` Zeigt der Plan
eine Änderung an der Resource oder ein `destroy`, hier abbrechen.

- [ ] **Schritt 10: Mergen und Apply verifizieren**

```bash
glab mr merge --yes
glab ci status --branch main
```

Erwartung: der `apply`-Job auf `main` läuft grün durch.

---

### Task 4: Lokale Arbeitskopie umziehen

**Files:**

- Modify: keine — Verzeichnis- und Git-Operationen.

**Interfaces:**

- Consumes: den neuen Pfad aus Task 2.
- Produces: Arbeitskopie unter
  `/Users/aerben/repositories/it-erben/gfu/spring-framework` mit korrektem
  `origin`. Tasks 5 arbeitet in diesem Verzeichnis.

Diese Task muss aus einer Shell laufen, deren Arbeitsverzeichnis **nicht**
innerhalb des umzuziehenden Baums liegt. Ein `mv` des Elternverzeichnisses
entzieht einer dort laufenden Shell sonst das Arbeitsverzeichnis.

- [ ] **Schritt 1: Worktrees auflisten**

```bash
git -C /Users/aerben/repositories/it-erben/gfu/spring-boot-advanced worktree list
```

Erwartung: der Hauptbaum und
`.claude/worktrees/basis-teil` auf `feat/basis-teil`.

- [ ] **Schritt 2: Worktree entfernen**

Der Branch ist in Task 1 gemergt, der Worktree wird nicht mehr gebraucht.
Ihn zu entfernen erspart die Reparatur der absoluten gitdir-Zeiger.

```bash
git -C /Users/aerben/repositories/it-erben/gfu/spring-boot-advanced \
    worktree remove .claude/worktrees/basis-teil
```

Meldet der Befehl nicht committete Änderungen im Worktree, diese erst klären.
Soll der Worktree erhalten bleiben, ihn stehen lassen und stattdessen nach
Schritt 3 ausführen:

```bash
cd /Users/aerben/repositories/it-erben/gfu/spring-framework
git worktree repair
cd .claude/worktrees/basis-teil
git worktree repair
```

- [ ] **Schritt 3: Verzeichnis umbenennen**

```bash
cd /Users/aerben/repositories/it-erben/gfu
mv spring-boot-advanced spring-framework
```

- [ ] **Schritt 4: Remote umstellen**

```bash
cd /Users/aerben/repositories/it-erben/gfu/spring-framework
git remote set-url origin git@gitlab.com:it-erben/gfu/spring-framework.git
git fetch origin
```

- [ ] **Schritt 5: Verifizieren**

```bash
git remote -v
git worktree list
git status
```

Erwartung: beide Remote-Zeilen zeigen auf
`git@gitlab.com:it-erben/gfu/spring-framework.git`, `worktree list` listet nur
noch existierende Pfade, `git status` läuft ohne Fehler.

- [ ] **Schritt 6: IntelliJ-Zeiger zur Kenntnis nehmen**

`/Users/aerben/repositories/it-erben/.idea/vcs.xml` und `workspace.xml` zeigen
noch auf den alten Pfad. IntelliJ führt diese lokalen Dateien beim nächsten
Öffnen des Projekts nach; kein Handlungsbedarf im Repository.

---

### Task 5: Dokumente im Kurs-Repository berichtigen

Spec und Plan des Basis-Teils nennen `gfu/spring-boot` als Zielnamen und
halten fest, das lokale Verzeichnis bleibe `spring-boot-advanced`. Beides ist
seit Task 2 und Task 4 falsch.

**Files:**

- Modify: `docs/superpowers/specs/2026-07-28-spring-boot-basis-teil-design.md:32-34`
- Modify: `docs/superpowers/plans/2026-07-28-spring-boot-basis-teil.md:214`
- Modify: `docs/superpowers/plans/2026-07-28-spring-boot-basis-teil.md:2365-2376`

**Interfaces:**

- Consumes: die Arbeitskopie aus Task 4.
- Produces: nichts, was spätere Tasks brauchen.

- [ ] **Schritt 1: Branch anlegen**

```bash
cd /Users/aerben/repositories/it-erben/gfu/spring-framework
git checkout main
git pull
git checkout -b docs/rename-spring-framework
```

- [ ] **Schritt 2: Spec berichtigen**

In `docs/superpowers/specs/2026-07-28-spring-boot-basis-teil-design.md`
ersetzen:

```markdown
- GitLab-Repo `gfu/spring-boot-advanced` → `gfu/spring-boot`. GitLab legt
  beim Rename eine Weiterleitung an, bestehende Clones brechen nicht sofort.
  Lokales Verzeichnis analog, danach `git remote set-url`.
```

durch:

```markdown
- GitLab-Repo `gfu/spring-boot-advanced` → `gfu/spring-framework`. Ablauf und
  Nachführung in `2026-08-17-repo-rename-spring-framework-design.md`.
```

- [ ] **Schritt 3: Plan berichtigen**

In `docs/superpowers/plans/2026-07-28-spring-boot-basis-teil.md` den ganzen
Schritt 9 ersetzen. Alt:

````markdown
- [ ] **Schritt 9: GitLab-Repository umbenennen — nur nach Rückfrage**

Nicht automatisch ausführen. Dem Nutzer vorlegen:

```bash
glab repo update it-erben/gfu/spring-boot-advanced \
     --name "spring-boot" --path "spring-boot"
git remote set-url origin git@gitlab.com:it-erben/gfu/spring-boot.git
```

Das lokale Verzeichnis heißt danach weiterhin `spring-boot-advanced`; ob es
mitumbenannt wird, entscheidet der Nutzer.
````

Neu:

```markdown
- [ ] **Schritt 9: GitLab-Repository umbenennen**

Zielname `gfu/spring-framework`, lokales Verzeichnis zieht mit. Eigener Plan:
`2026-08-17-repo-rename-spring-framework.md`.
```

- [ ] **Schritt 4: Lokalen Pfad im Plan berichtigen**

In `docs/superpowers/plans/2026-07-28-spring-boot-basis-teil.md` Zeile 214
ersetzen:

```bash
cd /Users/aerben/repositories/it-erben/gfu/spring-boot-advanced
```

durch:

```bash
cd /Users/aerben/repositories/it-erben/gfu/spring-framework
```

- [ ] **Schritt 5: Prüfen, dass kein alter Name übrig ist**

Nur die beiden berichtigten Dateien prüfen — die Dokumente vom 2026-08-17
nennen den alten Pfad und den verworfenen Zielnamen absichtlich.

```bash
grep -nE "gfu/spring-boot([^-]|$)|spring-boot-advanced" \
  docs/superpowers/specs/2026-07-28-spring-boot-basis-teil-design.md \
  docs/superpowers/plans/2026-07-28-spring-boot-basis-teil.md
```

Erwartung: genau ein Treffer, Zeile 32 der Spec — dort steht der alte Pfad als
Ausgangspunkt der Umbenennung.

- [ ] **Schritt 6: Pre-Commit laufen lassen**

```bash
pre-commit run --all-files
```

Erwartung: markdownlint, yamllint und lychee `Passed` oder `Skipped`.

- [ ] **Schritt 7: Committen und Merge Request anlegen**

```bash
git add docs/superpowers/specs/2026-07-28-spring-boot-basis-teil-design.md \
        docs/superpowers/plans/2026-07-28-spring-boot-basis-teil.md
git commit -m "docs: Zielnamen der Umbenennung auf gfu/spring-framework ziehen"
git push -u origin docs/rename-spring-framework
glab mr create \
  --source-branch docs/rename-spring-framework \
  --target-branch main \
  --title "docs: Zielnamen der Umbenennung auf gfu/spring-framework ziehen" \
  --description "Spec und Plan des Basis-Teils nannten gfu/spring-boot." \
  --yes
```

- [ ] **Schritt 8: Pipeline abwarten und mergen**

```bash
glab ci status --branch docs/rename-spring-framework
glab mr merge --yes
```

Erwartung: Pipeline grün, Merge Request gemergt.

---

### Task 6: components/pdf-publisher entkoppeln

`slides/` ist die Fixture, gegen die die Komponente in ihrer eigenen CI läuft
(`.gitlab-ci.yml` setzt `source-dir: "slides"`). Die Dateien bleiben; falsch
ist ihre Beschriftung und die Beschreibung des Repositories.

**Files:**

- Modify: `/Users/aerben/repositories/it-erben/components/pdf-publisher/README.md:3,8,31`
- Modify: `/Users/aerben/repositories/it-erben/components/pdf-publisher/slides/template.html:6,44`
- Modify: `/Users/aerben/repositories/it-erben/components/pdf-publisher/slides/00_Microservices_Architecture/slides.md:4`
- Modify: `/Users/aerben/repositories/it-erben/components/pdf-publisher/slides/01_Configuration/slides.md:4`

**Interfaces:**

- Consumes: nichts.
- Produces: nichts.

- [ ] **Schritt 1: Branch anlegen**

```bash
cd /Users/aerben/repositories/it-erben/components/pdf-publisher
git checkout main
git pull
git checkout -b docs/entkoppeln-vom-kurs
```

- [ ] **Schritt 2: README-Zeile 3 ersetzen**

Alt:

```markdown
This repository holds Marp slide decks for the “Spring Boot Advanced” training and a tiny GitLab CI template that renders each deck to a PDF and publishes them via GitLab Pages.
```

Neu:

```markdown
This repository holds a GitLab CI component that renders every Marp deck under a source directory to a PDF and publishes them via GitLab Pages, plus a sample deck the component is tested against.
```

- [ ] **Schritt 3: Falschen Template-Pfad in der README berichtigen**

Die Datei heißt `templates/build-publish/template.yml`, nicht
`templates/pdf-pages/template.yml`. In Zeile 8 den Pfad ersetzen und in Zeile
30 bis 32 das Include-Beispiel auf die Komponenten-Syntax bringen. Alt:

````markdown
```yaml
include:
  - local: templates/pdf-pages/template.yml
```
````

Neu:

````markdown
```yaml
include:
  - component: gitlab.com/it-erben/components/pdf-publisher/build-publish@1.8.0
    inputs:
      source-dir: "slides"
```
````

- [ ] **Schritt 4: Fixture-Beschriftung neutral fassen**

In `slides/template.html` Zeile 6 und 44 jeweils `Spring Boot Advanced` durch
`PDF-Publisher Beispiel-Deck` ersetzen — also `<title>PDF-Publisher
Beispiel-Deck</title>` und `<h1>PDF-Publisher Beispiel-Deck</h1>`. Der
`%s`-Platzhalter bleibt unberührt.

In `slides/00_Microservices_Architecture/slides.md` und
`slides/01_Configuration/slides.md` jeweils Zeile 4:

```yaml
header: Beispiel-Deck
```

- [ ] **Schritt 5: Prüfen, dass keine Kursbezeichnung übrig ist**

```bash
grep -rn "Spring Boot Advanced" README.md slides/ templates/
```

Erwartung: keine Treffer.

- [ ] **Schritt 6: Committen und Merge Request anlegen**

```bash
git add README.md slides/template.html \
        slides/00_Microservices_Architecture/slides.md \
        slides/01_Configuration/slides.md
git commit -m "docs: Beispiel-Deck vom Kursnamen loesen"
git push -u origin docs/entkoppeln-vom-kurs
glab mr create \
  --source-branch docs/entkoppeln-vom-kurs \
  --target-branch main \
  --title "docs: Beispiel-Deck vom Kursnamen loesen" \
  --description "Die README beschrieb das Repository als Traeger der Kursfolien und verwies auf templates/pdf-pages/template.yml; die Datei liegt unter templates/build-publish/." \
  --yes
```

- [ ] **Schritt 7: Pipeline abwarten und mergen**

```bash
glab ci status --branch docs/entkoppeln-vom-kurs
glab mr merge --yes
```

Erwartung: der `build`-Job rendert beide Fixture-Decks weiterhin zu PDFs, die
Pipeline läuft grün, der Merge Request ist gemergt.

---

## Abschluss

```bash
glab api projects/76899678 | jq -r '.path_with_namespace, .name'
git -C /Users/aerben/repositories/it-erben/gfu/spring-framework remote -v
```

Erwartung: `it-erben/gfu/spring-framework`, `Spring Framework`, und ein
`origin`, der auf denselben Pfad zeigt.
