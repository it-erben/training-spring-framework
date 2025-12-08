package tech.erben.security.oauth2.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2DemoController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public OAuth2DemoController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/api/me")
    public Map<String, Object> me(
        @AuthenticationPrincipal OAuth2User oauth2User,
        Principal principal,
        OAuth2AuthenticationToken authentication
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", oauth2User.getName());
        response.put("attributes", oauth2User.getAttributes());
        response.put("principalName", principal.getName());

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
            authentication.getAuthorizedClientRegistrationId(),
            principal.getName()
        );
        if (client != null && client.getAccessToken() != null) {
            String tokenValue = client.getAccessToken().getTokenValue();
            String preview = tokenValue.length() > 10 ? tokenValue.substring(0, 10) + "..." : tokenValue;
            response.put("accessTokenPreview", preview);
            response.put("accessTokenExpiresAt", client.getAccessToken().getExpiresAt());
        }
        return response;
    }

    @GetMapping("/api/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}
