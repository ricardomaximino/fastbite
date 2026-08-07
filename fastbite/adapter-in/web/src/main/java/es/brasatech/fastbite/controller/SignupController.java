package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.tenant.TenantSignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SignupController {

    private final TenantSignupService tenantSignupService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/signup")
    public String signup() {
        return "fastfood/signup";
    }

    @PostMapping("/signup")
    public String registerTenant(
            @RequestParam String tenantId,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            jakarta.servlet.http.HttpServletRequest request,
            Model model) {
        try {
            String encodedPassword = passwordEncoder.encode(password);
            tenantSignupService.registerTenant(tenantId, username, encodedPassword, fullName);
            
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String scheme = request.getScheme();
            String domain = serverName;
            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }
            
            String subdomainUrl;
            if (serverPort == 80 || serverPort == 443) {
                subdomainUrl = scheme + "://" + tenantId + "." + domain + "/login";
            } else {
                subdomainUrl = scheme + "://" + tenantId + "." + domain + ":" + serverPort + "/login";
            }
            
            model.addAttribute("registeredTenantId", tenantId);
            model.addAttribute("subdomainUrl", subdomainUrl);
            model.addAttribute("success", "Restaurant " + tenantId + " has been successfully registered and provisioned! You can now log in.");
            return "fastfood/signup";
        } catch (IllegalArgumentException e) {
            log.warn("Failed tenant registration due to input validation: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "fastfood/signup";
        } catch (Exception e) {
            log.error("Failed tenant registration and self-provisioning: ", e);
            model.addAttribute("error", "An error occurred during provisioning: " + e.getMessage());
            return "fastfood/signup";
        }
    }
}
