package es.brasatech.fastbite.application.tenant;

import es.brasatech.fastbite.domain.tenant.BillingAccount;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface BillingPort {
    Optional<BillingAccount> find(String tenant);
    BillingAccount updateLocked(String tenant, UnaryOperator<BillingAccount> update);
    void requestGroupQuote(String owner, String email, int locations);
}
