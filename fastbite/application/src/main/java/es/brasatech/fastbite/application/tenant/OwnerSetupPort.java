package es.brasatech.fastbite.application.tenant;

import java.time.Instant;

/** Platform-scoped persistence; implementations must consume a token and update both users atomically. */
public interface OwnerSetupPort {
    record Invitation(String checkoutId, String tenantId, String username, String fullName, String email, String plan) {}

    /** Create disabled owner accounts, or replace the token for a retry of the same checkout. */
    boolean prepare(Invitation invitation, String tokenHash, Instant expiresAt);

    boolean isValid(String tokenHash, Instant now);

    boolean complete(String tokenHash, String encodedPassword, Instant now);
}
