# AGENTS

## Linting

Diese Repos verwenden Pre-Commit Hooks:

- Markdown: `markdownlint-cli2` mit `--fix`, wobei die line-length maximal 80 ist
- YAML: `yamllint` (extends relaxed, line-length max 140)
- Links: `lychee` mit `--accept 429,200`, `--exclude http://localhost.*`, `--exclude-path .npm-cache`,
  `--max-concurrency 4`, `--retry-wait-time 2`, `--timeout 20`, `--cache`

Bitte halte `.pre-commit-config.yaml` und diese Einstellungen synchron, wenn du die Linter- oder Link-Check-Regeln
aenderst.

## Struktur

- Im Verzeichnis `slides` befinden sich Verzeichnisse für Module einer Spring-Boot-Schulung, jeweils mit einer datei
  `slides.md` welche Marp-Slides enthält.

- `assignments` enthält Übungsaufgaben
- `solutions` deren Lösungen.

Halte die Lösungen in solutions immer synchron mit den assignments.
