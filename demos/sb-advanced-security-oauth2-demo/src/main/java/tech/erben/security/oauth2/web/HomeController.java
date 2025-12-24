package tech.erben.security.oauth2.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class HomeController {

    @GetMapping({ "/", "/index" })
    public String index(@AuthenticationPrincipal OAuth2User oauth2User, Model model) {
        if (oauth2User != null) {
            model.addAttribute("attributes", oauth2User.getAttributes());
            model.addAttribute("username", oauth2User.getAttribute("login"));
        } else {
            model.addAttribute("attributes", Map.of());
        }
        return "index";
    }
}
