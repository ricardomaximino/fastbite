package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.tenant.SubscriptionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
public class BillingController {
    private final SubscriptionService subscriptions;
    public BillingController(SubscriptionService subscriptions) { this.subscriptions=subscriptions; }
    @GetMapping("/owner/billing/{tenant}")
    public String show(@PathVariable String tenant, Principal owner, Model model) {
        try { model.addAttribute("billing", subscriptions.account(tenant, owner.getName())); }
        catch (IllegalArgumentException e) { throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND); }
        model.addAttribute("billingConfigured", subscriptions.configured());
        return "fastfood/owner/billing";
    }
    @PostMapping("/owner/billing/{tenant}/{action}")
    public String action(@PathVariable String tenant, @PathVariable String action, Principal owner, RedirectAttributes flash) {
        try {
            return switch (action) {
                case "checkout" -> "redirect:" + subscriptions.checkout(tenant, owner.getName());
                case "portal" -> "redirect:" + subscriptions.portal(tenant, owner.getName());
                case "refresh" -> { subscriptions.refresh(tenant, owner.getName()); yield "redirect:/owner/billing/" + tenant; }
                default -> throw new IllegalArgumentException("Unknown billing action.");
            };
        } catch (IllegalArgumentException | IllegalStateException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/billing/" + tenant;
        }
    }
    @PostMapping("/owner/group-quote")
    public String groupQuote(@RequestParam String email, @RequestParam int locations, Principal owner, RedirectAttributes flash) {
        try { subscriptions.requestGroupQuote(owner.getName(), email, locations); flash.addFlashAttribute("notice", "Your group pricing request has been saved. We will use your email to follow up."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/owner/console";
    }
}
