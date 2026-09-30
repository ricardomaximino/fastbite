package es.brasatech.fastbite.application.settings;

import es.brasatech.fastbite.domain.settings.RestaurantSettings;

/** Settings of the restaurant the current request is for. */
public interface RestaurantSettingsService {

    /** The saved settings, or {@link RestaurantSettings#DEFAULTS} if the restaurant never saved any. */
    RestaurantSettings get();

    RestaurantSettings save(RestaurantSettings settings);
}
