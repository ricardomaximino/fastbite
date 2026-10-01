package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.tenant.OwnerSetupService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;

@Controller
public class OwnerPasswordSetupController {
    private final OwnerSetupService setup;
    private final PasswordEncoder encoder;

    public OwnerPasswordSetupController(OwnerSetupService setup, PasswordEncoder encoder) {
        this.setup = setup;
        this.encoder = encoder;
    }

    @GetMapping("/set-password")
    public String form(@RequestParam(defaultValue = "") String token, Model model, HttpServletResponse response) {
        protect(response);
        if (!setup.isValid(token)) return invalid(model, response);
        model.addAttribute("token", token);
        return "fastfood/setPassword";
    }

    @PostMapping("/set-password")
    public String submit(@RequestParam(defaultValue = "") String token,
            @RequestParam(defaultValue = "") String password, @RequestParam(defaultValue = "") String confirmation,
            Model model, HttpServletResponse response) {
        protect(response);
        if (!setup.isValid(token)) return invalid(model, response);
        // BCrypt has a 72-byte input limit; enforce it before encoding, including Unicode input.
        if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72 || !password.equals(confirmation)) {
            response.setStatus(400);
            model.addAttribute("token", token);
            model.addAttribute("error", "Use at least 12 characters (at most 72 UTF-8 bytes), and enter the same password twice.");
            return "fastfood/setPassword";
        }
        if (!setup.complete(token, encoder.encode(password))) return invalid(model, response);
        model.addAttribute("complete", true);
        return "fastfood/setPassword";
    }

    private static String invalid(Model model, HttpServletResponse response) {
        response.setStatus(400);
        model.addAttribute("invalid", true);
        return "fastfood/setPassword";
    }

    private static void protect(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
    }
}
