package es.brasatech.fastbite.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    /** The tenant comes from TenantContextFilter; the view gets it through TenantControllerAdvice. */
    @GetMapping({"/login", "/{tenantId}/login"})
    public String login() {
        return "fastfood/login";
    }
}
