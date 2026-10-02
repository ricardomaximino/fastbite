package es.brasatech.fastbite.application.tenant;

/** Durable ownership of a registration attempt, serialized across app instances. */
public interface TenantLifecyclePort {
    enum State { PROVISIONING, AWAITING_OWNER, ACTIVE, FAILED }

    /** Completed operations are no-ops; failed or interrupted operations may retry. */
    void register(String tenantId, String operationId, String owner, State successState, Runnable work);
}
