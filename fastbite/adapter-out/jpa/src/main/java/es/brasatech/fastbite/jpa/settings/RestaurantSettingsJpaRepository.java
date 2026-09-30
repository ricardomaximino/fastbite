package es.brasatech.fastbite.jpa.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantSettingsJpaRepository extends JpaRepository<RestaurantSettingsEntity, Integer> {
}
