package es.brasatech.fastbite.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping({"/login", "/{tenantId}/login"})
    public String login(@org.springframework.web.bind.annotation.PathVariable(required = false) String tenantId, org.springframework.ui.Model model) {
        if (tenantId == null) {
            tenantId = es.brasatech.fastbite.domain.tenant.TenantContext.getCurrentTenant();
        }
        model.addAttribute("tenantId", tenantId);
        return "fastfood/login";
    }
}
