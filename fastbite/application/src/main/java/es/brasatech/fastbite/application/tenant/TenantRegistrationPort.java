package es.brasatech.fastbite.application.tenant;

/** Both owner copies and the location must commit together. */
public interface TenantRegistrationPort {
    void requireAvailableUsername(String tenant, String username);
    void createOwner(String tenant, String username, String encodedPassword, String fullName);
    void requireActiveOwner(String username);
    void createLocation(String tenant, String owner, String plan);
}
