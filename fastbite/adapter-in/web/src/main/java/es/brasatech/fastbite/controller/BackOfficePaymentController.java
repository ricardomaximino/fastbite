package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.payment.PaymentService;
import es.brasatech.fastbite.domain.payment.PaymentConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/backoffice/payment")
@RequiredArgsConstructor
public class BackOfficePaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @ResponseBody
    public PaymentConfig getConfig() {
        return paymentService.getConfig();
    }

    @PostMapping
    @ResponseBody
    public void updateConfig(@RequestBody PaymentConfig config) {
        paymentService.updateConfig(config);
    }
}
