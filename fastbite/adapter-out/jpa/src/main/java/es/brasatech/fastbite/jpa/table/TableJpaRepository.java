package es.brasatech.fastbite.jpa.table;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableJpaRepository extends JpaRepository<TableEntity, String> {
}
