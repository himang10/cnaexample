package cnadata.consumer.participant.repository;

import cnadata.consumer.participant.data.OrderLine;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderLineMongoRepository extends MongoRepository<OrderLine, Long> {
}
