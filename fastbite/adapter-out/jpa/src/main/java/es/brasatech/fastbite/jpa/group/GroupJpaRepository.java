package es.brasatech.fastbite.jpa.group;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for Group entities.
 */
@Repository
@Profile("jpa")
public interface GroupJpaRepository extends JpaRepository<GroupEntity, String> {
}
