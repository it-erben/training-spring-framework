# Umbenennung nach gfu/spring-framework

## Ziel

Mit dem Basis-Teil deckt das Repository Spring Core, Spring-Boot-Grundlagen
und Fortgeschrittenenthemen ab. Der Pfad `gfu/spring-boot-advanced`
beschreibt davon nur den dritten Block. Dieses Dokument beschreibt die
Umbenennung nach `gfu/spring-framework` und die Nachführung in den internen
Repositories.

Die Spec `2026-07-28-spring-boot-basis-teil-design.md` legte `gfu/spring-boot`
als Zielnamen fest. Dieser Beschluss wird durch `gfu/spring-framework`
ersetzt.

## Umfang

Betroffen sind der GitLab-Projektpfad, der Anzeigename, die
Terraform-Verwaltung in `internal/repository-setup` und die lokale
Arbeitskopie.

Nicht betroffen ist die Deployment-Kette: Der Kurs hat kein HelmRelease in
`proxmox-setup/clusters/k8s-01/apps/`, kein Manifest in `image-automation/`
und bindet in `.gitlab-ci.yml` keine `training-deploy`-Komponente ein, nur
PDF-Publisher, Linter, Maven-Verify und Release. Es existieren weder
Subdomain noch Container-Images noch cert-manager-Einträge.

## Zielnamen

| Feld | alt | neu |
| --- | --- | --- |
| Pfad | `gfu/spring-boot-advanced` | `gfu/spring-framework` |
| Anzeigename | Spring Boot Advanced | Spring Framework |
| Beschreibung | Codebeispiele, Übungen und Folien für die Spring Boot-Schulung | Codebeispiele, Übungen und Folien zur Spring-Schulung (Basis und Advanced) |

Die Projekt-ID 76899678 bleibt über den Rename stabil.

## Reihenfolge

`feat/basis-teil` wird über einen Merge Request nach `main` gebracht. Erst
danach folgt der Rename, damit kein offener Merge Request im Pfadwechsel
liegt.

Der lokale Verzeichnis-Rename steht am Ende. Der Worktree
`.claude/worktrees/basis-teil` liegt innerhalb des Repositories; ein `mv` des
Elternverzeichnisses entzieht einer dort laufenden Shell das
Arbeitsverzeichnis.

## GitLab-Rename

`glab repo update` kennt weder `--path` noch `--name` — nur `--description`,
`--defaultBranch` und `--archive`. Pfad, Name und Beschreibung gehen deshalb
in einem PUT über die API:

```
glab api projects/76899678 --method PUT \
  --field path=spring-framework \
  --field name='Spring Framework' \
  --field description='Codebeispiele, Übungen und Folien zur Spring-Schulung (Basis und Advanced)'
```

GitLab legt einen Redirect vom alten Pfad an. Dieser hält, bis der Pfad
`gfu/spring-boot-advanced` neu belegt wird, und ersetzt daher nicht das
Umstellen der Remotes.

## Terraform (internal/repository-setup)

Drei Änderungen in einem Merge Request, nach dem Muster von `f2966c8`
(`components/gfu-deploy` → `components/training-deploy`):

- `projects.tf` — Map-Key `"gfu/spring-boot-advanced"` → `"gfu/spring-framework"`,
  `name` und `description` angeglichen.
- `imports.tf` — `to = gitlab_project.this["gfu/spring-framework"]`.
- `imports.tf`, Abschnitt `# Renames` — `moved`-Block:

  ```hcl
  moved {
    from = gitlab_project.this["gfu/spring-boot-advanced"]
    to   = gitlab_project.this["gfu/spring-framework"]
  }
  ```

Der `moved`-Block ist zwingend: Der Map-Key ist Teil der Resource-Adresse.
Ohne ihn plant Terraform destroy und create und scheitert an
`prevent_destroy`.

`name` und `description` stehen in `ignore_changes` und werden von Terraform
nicht durchgesetzt; sie werden in der Konfiguration trotzdem korrekt geführt.
`ignore_changes` bleibt unverändert — `name` dort zu entfernen ließe
Terraform die Namen aller verwalteten Repositories neu setzen.

Weil der Rename bereits in GitLab erfolgt ist, ist der Apply ein reiner
State-Umzug. Ein Plan, der eine API-Änderung an der Resource zeigt, ist ein
Abbruchkriterium.

## Lokale Arbeitskopie

```
cd ~/repositories/it-erben/gfu
git -C spring-boot-advanced worktree remove .claude/worktrees/basis-teil
mv spring-boot-advanced spring-framework
cd spring-framework
git remote set-url origin git@gitlab.com:it-erben/gfu/spring-framework.git
```

Bleibt der Worktree bestehen, brechen beim `mv` beide gitdir-Zeiger, da sie
absolut sind. `git worktree repair` im Haupt-Worktree richtet die
`.git`-Datei des verschachtelten Worktrees, ein zweiter Aufruf von dort aus
die Admin-Seite unter `.git/worktrees/`.

`/Users/aerben/repositories/it-erben/.idea/vcs.xml` und `workspace.xml`
zeigen auf den alten Pfad. IntelliJ führt diese lokalen Dateien beim nächsten
Öffnen nach.

## Dokumente im Repository

`docs/superpowers/specs/2026-07-28-spring-boot-basis-teil-design.md` und
`docs/superpowers/plans/2026-07-28-spring-boot-basis-teil.md` nennen
`gfu/spring-boot` als Zielnamen und halten fest, das lokale Verzeichnis bleibe
`spring-boot-advanced`. Beides wird auf den tatsächlichen Stand gebracht.

## components/pdf-publisher

Eigenes Repository, eigener Merge Request.

`slides/` ist die Test-Fixture, gegen die die Komponente in ihrer eigenen CI
läuft (`source-dir: "slides"`), und bleibt bestehen. Falsch ist die
Beschriftung: Die README bezeichnet das Repository als Träger der Decks der
Spring-Boot-Advanced-Schulung, `slides/template.html` und die Header der
beiden `slides.md` tragen denselben Namen. README auf das Beispiel-Deck der
Komponente umschreiben, Titel und Header neutral fassen.

## Unverändert

Maven-Koordinaten (`tech.erben:sb-training`, Module `sb-advanced-*` und
`sb-basics-*`) und `slides/template.html` im Kurs-Repository
(„Spring Boot Schulung").

## Verifikation

```
glab api projects/76899678 | jq '.path_with_namespace, .name'
```

erwartet `it-erben/gfu/spring-framework` und `Spring Framework`.

Der Terraform-Plan im Merge Request zeigt den `moved`-State-Umzug und keine
Änderung an der Resource. Die Pipeline im umbenannten Repository läuft grün;
die Pages-URL wandert auf `…/gfu/spring-framework`, verlinkt ist die alte
nirgends im Bestand. `git remote -v` und `git worktree list` im umbenannten
Verzeichnis zeigen den neuen Pfad.
