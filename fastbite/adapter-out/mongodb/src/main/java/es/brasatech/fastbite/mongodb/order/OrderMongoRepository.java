package es.brasatech.fastbite.mongodb.order;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongodb")
public interface OrderMongoRepository extends MongoRepository<OrderDocument, String> {
}
