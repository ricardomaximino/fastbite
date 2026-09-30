package es.brasatech.fastbite.domain.settings;

import es.brasatech.fastbite.domain.order.ServiceType;

/**
 * What one restaurant has switched on. The KDS minutes colour a kitchen ticket yellow and then
 * red as it waits; 0 turns the colouring off.
 */
public record RestaurantSettings(boolean dineIn, boolean takeaway, int kdsYellowMinutes, int kdsRedMinutes) {

    public static final RestaurantSettings DEFAULTS = new RestaurantSettings(true, true, 0, 0);

    public RestaurantSettings {
        kdsYellowMinutes = Math.max(0, kdsYellowMinutes);
        kdsRedMinutes = Math.max(0, kdsRedMinutes);
    }

    public boolean offers(ServiceType serviceType) {
        return serviceType == ServiceType.TAKEAWAY ? takeaway : dineIn;
    }
}
