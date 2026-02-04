package es.brasatech.fastbite.application.office;

import es.brasatech.fastbite.domain.group.Group;
import es.brasatech.fastbite.domain.group.GroupI18n;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing groups in the BackOffice system.
 * Implementations can use different persistence strategies (in-memory, MongoDB,
 * JPA).
 */
public interface GroupService {

    /**
     * Get all groups
     */
    List<Group> findAll();

    /**
     * Find group by ID
     */
    Optional<Group> findById(String id);

    /**
     * Create a new group
     */
    Group create(Group group);

    /**
     * Update an existing group
     */
    Optional<Group> update(String id, Group group);

    /**
     * Delete a group
     */
    boolean delete(String id);

    /**
     * Clear all groups (for testing)
     */
    void clear();

    // ===== I18n Methods =====

    /**
     * Get group with i18n data (all translations)
     */
    Optional<GroupI18n> findI18nById(String id);

    /**
     * Update group translations
     */
    Optional<GroupI18n> updateI18n(String id, GroupI18n groupI18n);

    /**
     * Get group in specific locale with fallback to default
     */
    Optional<Group> findByIdInLocale(String id, String locale);

    /**
     * Get all groups in specific locale with fallback to default
     */
    List<Group> findAllInLocale(String locale);
}
