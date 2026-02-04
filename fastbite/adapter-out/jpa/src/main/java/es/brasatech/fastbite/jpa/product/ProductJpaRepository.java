package es.brasatech.fastbite.jpa.product;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for Product entities.
 */
@Repository
@Profile("jpa")
public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {
}
