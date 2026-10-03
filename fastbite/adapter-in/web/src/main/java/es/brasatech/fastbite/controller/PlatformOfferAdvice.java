package es.brasatech.fastbite.controller;
import es.brasatech.fastbite.domain.tenant.SubscriptionPlan;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
@ControllerAdvice(assignableTypes = {HomeController.class, SignupController.class, BillingController.class})
public class PlatformOfferAdvice {
    @ModelAttribute("monthlyPrice") public long monthlyPrice() { return SubscriptionPlan.MONTHLY_CENTS / 100; }
}
