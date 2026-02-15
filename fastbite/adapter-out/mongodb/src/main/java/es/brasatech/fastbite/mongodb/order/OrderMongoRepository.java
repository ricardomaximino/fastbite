package es.brasatech.fastbite.mongodb.order;

import es.brasatech.fastbite.domain.order.OrderStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@Profile("mongodb")
public interface OrderMongoRepository extends MongoRepository<OrderDocument, String> {
    List<OrderDocument> findByTableIdAndStatusNotIn(String tableId, Collection<OrderStatus> statuses);
}
