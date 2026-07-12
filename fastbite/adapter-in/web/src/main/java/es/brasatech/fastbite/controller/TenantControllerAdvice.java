package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.domain.tenant.TenantContext;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class TenantControllerAdvice {

    @ModelAttribute("tenantId")
    public String getTenantId() {
        return TenantContext.getCurrentTenant();
    }
}
