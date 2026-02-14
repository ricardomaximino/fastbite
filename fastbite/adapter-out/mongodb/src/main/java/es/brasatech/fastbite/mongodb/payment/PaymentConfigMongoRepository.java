package es.brasatech.fastbite.mongodb.payment;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentConfigMongoRepository extends MongoRepository<PaymentConfigDocument, String> {
}
