package es.brasatech.fastbite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Principal principal, Model model) {
        String currentTenant = es.brasatech.fastbite.domain.tenant.TenantContext.getCurrentTenant();
        if (currentTenant != null) {
            return "redirect:/menu";
        }
        if (principal != null) {
            model.addAttribute("ownerUsername", principal.getName());
        }
        return "fastfood/index";
    }
}
