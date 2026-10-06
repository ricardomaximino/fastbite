package es.brasatech.fastbite.application.tenant;

public interface AppearancePort {
    String ownerTheme(String owner);
    String locationTheme(String tenant);
    String effectiveTheme(String tenant);
    void saveOwnerTheme(String owner, String theme);
    void saveLocationTheme(String tenant, String theme);
}
