package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.office.UserService;
import es.brasatech.fastbite.application.tenant.TenantLocationService;
import es.brasatech.fastbite.domain.tenant.TenantContext;
import es.brasatech.fastbite.domain.user.Role;
import es.brasatech.fastbite.domain.user.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/owner/locations/{tenantId}/users")
@RequiredArgsConstructor
@Slf4j
public class OwnerStaffController {

    private final UserService userService;
    private final TenantLocationService tenantLocationService;
    private final PasswordEncoder passwordEncoder;

    private void checkOwnership(Principal principal, String tenantId) {
        if (principal == null || !tenantLocationService.isOwnerOf(principal.getName(), tenantId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not own this location.");
        }
    }

    @GetMapping
    public String listUsers(
            @PathVariable String tenantId,
            Principal principal,
            Model model) {
        checkOwnership(principal, tenantId);

        try {
            TenantContext.setCurrentTenant(tenantId);
            List<UserDto> users = userService.findAll();
            model.addAttribute("users", users);
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("roles", Role.values());
            return "fastfood/owner/fragments :: staff-list";
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping
    public String createUser(
            @PathVariable String tenantId,
            @RequestParam String username,
            @RequestParam String fullName,
            @RequestParam String password,
            @RequestParam Role role,
            Principal principal,
            Model model) {
        checkOwnership(principal, tenantId);

        try {
            TenantContext.setCurrentTenant(tenantId);
            
            UserDto newUser = new UserDto(
                    null,
                    username,
                    passwordEncoder.encode(password),
                    fullName,
                    Set.of(role),
                    true,
                    tenantId
            );
            userService.save(newUser);
            
            // Reload list
            List<UserDto> users = userService.findAll();
            model.addAttribute("users", users);
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("roles", Role.values());
            model.addAttribute("success", "User '" + username + "' created successfully!");
            return "fastfood/owner/fragments :: staff-list";
        } catch (Exception e) {
            log.error("Failed to create staff user for tenant " + tenantId, e);
            model.addAttribute("error", e.getMessage());
            
            TenantContext.setCurrentTenant(tenantId);
            List<UserDto> users = userService.findAll();
            model.addAttribute("users", users);
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("roles", Role.values());
            return "fastfood/owner/fragments :: staff-list";
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/delete")
    public String deleteUser(
            @PathVariable String tenantId,
            @RequestParam String userId,
            Principal principal,
            Model model) {
        checkOwnership(principal, tenantId);

        try {
            TenantContext.setCurrentTenant(tenantId);
            userService.delete(userId);
            
            // Reload list
            List<UserDto> users = userService.findAll();
            model.addAttribute("users", users);
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("roles", Role.values());
            model.addAttribute("success", "User deleted successfully!");
            return "fastfood/owner/fragments :: staff-list";
        } finally {
            TenantContext.clear();
        }
    }
}
