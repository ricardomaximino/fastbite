package es.brasatech.fastbite.mongodb.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMongoRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByUsername(String username);
}
