package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.payment.PaymentService;
import es.brasatech.fastbite.domain.payment.PaymentConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/backoffice/payment")
@RequiredArgsConstructor
public class BackOfficePaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public String getConfig(Model model) {
        model.addAttribute("paymentConfig", paymentService.getConfig());
        return "fastfood/backOffice :: payment-section"; // This might need a new fragment
    }

    @PostMapping
    @ResponseBody
    public void updateConfig(@RequestBody PaymentConfig config) {
        paymentService.updateConfig(config);
    }
}
