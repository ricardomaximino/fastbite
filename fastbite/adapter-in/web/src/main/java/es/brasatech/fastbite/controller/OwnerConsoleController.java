package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.tenant.TenantLocationService;
import es.brasatech.fastbite.application.tenant.TenantSignupService;
import es.brasatech.fastbite.domain.tenant.TenantLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OwnerConsoleController {

    private final TenantLocationService tenantLocationService;
    private final TenantSignupService tenantSignupService;
    private final es.brasatech.fastbite.config.TenantRoutingResolver tenantResolver;

    @GetMapping("/owner/console")
    public String getOwnerDashboard(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        String ownerUsername = principal.getName();
        List<TenantLocation> locations = tenantLocationService.getLocationsByOwner(ownerUsername);
        
        model.addAttribute("ownerUsername", ownerUsername);
        model.addAttribute("locations", locations);
        return "fastfood/owner/console";
    }

    @PostMapping("/owner/add-location")
    public String addLocation(
            @RequestParam String tenantId,
            @RequestParam(defaultValue = "Free Demo") String plan,
            Principal principal,
            Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        String ownerUsername = principal.getName();
        try {
            tenantSignupService.registerAdditionalLocation(tenantId, ownerUsername, plan);
            return "redirect:/owner/console";
        } catch (Exception e) {
            log.error("Failed to add location: " + tenantId, e);
            model.addAttribute("error", e.getMessage());
            // Reload dashboard attributes for error view
            List<TenantLocation> locations = tenantLocationService.getLocationsByOwner(ownerUsername);
            model.addAttribute("ownerUsername", ownerUsername);
            model.addAttribute("locations", locations);
            return "fastfood/owner/console";
        }
    }

    @PostMapping("/owner/delete-location")
    public String deleteLocation(
            @RequestParam String tenantId,
            Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String ownerUsername = principal.getName();
        if (tenantLocationService.isOwnerOf(ownerUsername, tenantId)) {
            tenantLocationService.removeLocation(tenantId);
            log.info("Deleted location mapping for: {} by owner: {}", tenantId, ownerUsername);
        }
        return "redirect:/owner/console";
    }

    @PostMapping("/owner/bind-domain")
    public String bindDomain(
            @RequestParam String tenantId,
            @RequestParam(required = false) String customDomain,
            Principal principal,
            Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        String ownerUsername = principal.getName();
        try {
            // Evict old cache key
            tenantLocationService.getLocation(tenantId).ifPresent(loc -> {
                if (loc.customDomain() != null) {
                    tenantResolver.evictCache(loc.customDomain());
                }
            });
            
            // Bind domain
            tenantLocationService.bindCustomDomain(ownerUsername, tenantId, customDomain);
            
            // Evict new cache key just in case
            if (customDomain != null) {
                tenantResolver.evictCache(customDomain);
            }
            
            return "redirect:/owner/console";
        } catch (Exception e) {
            log.error("Failed to bind custom domain: " + customDomain, e);
            model.addAttribute("error", e.getMessage());
            List<TenantLocation> locations = tenantLocationService.getLocationsByOwner(ownerUsername);
            model.addAttribute("ownerUsername", ownerUsername);
            model.addAttribute("locations", locations);
            return "fastfood/owner/console";
        }
    }
}
