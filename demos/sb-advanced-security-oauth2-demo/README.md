# OAuth2 Login Demo (GitHub)

Schwestermodul zu `sb-advanced-security-basic-demo`: Zeigt einen OAuth2 Login via GitHub (Authorization Code Flow) mit Spring Security.

## Setup GitHub OAuth App

1) GitHub → Settings → Developer settings → OAuth Apps → New OAuth App.
   - Homepage URL: `http://localhost:8080/`
   - Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`
2) Client-ID und Client-Secret kopieren.
3) Lokale Variablen setzen (z.B. im Terminal):

   ```bash
   export GITHUB_CLIENT_ID=...
   export GITHUB_CLIENT_SECRET=...
   ```

   Alternativ die Properties direkt in `application.yml` eintragen.

## Starten

```bash
mvn -f ../pom.xml -pl sb-advanced-security-oauth2-demo -am spring-boot:run
```

## Was gezeigt wird

- `/` (Thymeleaf-View): Login-Button (`/oauth2/authorization/github`), nach Login Anzeige der GitHub-Profile-Attribute, Logout-Link.
- `/api/me`: Gibt den `OAuth2User` (Attribute, Principal-Name) plus Access-Token-Vorschau zurück.
- Security: Alle Routen außer `"/", "/index", "/index.html", "/css/**", "/webjars/**"` sind geschützt; Login erfolgt via GitHub, Logout leitet zurück auf `/`.

## Optional: GitHub API call

Mit dem Access Token lässt sich z.B. `curl -H "Authorization: Bearer <token>" https://api.github.com/user/repos` testen. In der Anwendung ist das Token nur gekürzt sichtbar (`accessTokenPreview`), um es nicht versehentlich zu leaken.
