package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.tenant.OwnerStaffService;
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

@Controller
@RequestMapping("/owner/locations/{tenantId}/users")
@RequiredArgsConstructor
@Slf4j
public class OwnerStaffController {

    private final OwnerStaffService ownerStaffService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String listUsers(
            @PathVariable String tenantId,
            Principal principal,
            Model model) {
        
        log.info("Request to listUsers for tenantId: {}. Principal: {}", tenantId, principal);
        if (principal instanceof org.springframework.security.core.Authentication auth) {
            log.info("User authorities: {}", auth.getAuthorities());
        }
        
        String ownerUsername = principal != null ? principal.getName() : null;
        List<UserDto> users = ownerStaffService.listStaff(ownerUsername, tenantId);
        model.addAttribute("users", users);
        model.addAttribute("tenantId", tenantId);
        model.addAttribute("roles", Role.values());
        return "fastfood/owner/fragments :: staff-list";
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

        String ownerUsername = principal != null ? principal.getName() : null;
        try {
            String encodedPassword = passwordEncoder.encode(password);
            List<UserDto> users = ownerStaffService.createStaff(ownerUsername, tenantId, username, fullName, encodedPassword, role);
            model.addAttribute("users", users);
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("roles", Role.values());
            model.addAttribute("success", "User '" + username + "' created successfully!");
        } catch (Exception e) {
            log.error("Failed to create staff user for tenant " + tenantId, e);
            model.addAttribute("error", e.getMessage());
            
            List<UserDto> users = ownerStaffService.listStaff(ownerUsername, tenantId);
            model.addAttribute("users", users);
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("roles", Role.values());
        }
        return "fastfood/owner/fragments :: staff-list";
    }

    @PostMapping("/delete")
    public String deleteUser(
            @PathVariable String tenantId,
            @RequestParam String userId,
            Principal principal,
            Model model) {

        String ownerUsername = principal != null ? principal.getName() : null;
        List<UserDto> users = ownerStaffService.deleteStaff(ownerUsername, tenantId, userId);
        model.addAttribute("users", users);
        model.addAttribute("tenantId", tenantId);
        model.addAttribute("roles", Role.values());
        model.addAttribute("success", "User deleted successfully!");
        return "fastfood/owner/fragments :: staff-list";
    }
}
