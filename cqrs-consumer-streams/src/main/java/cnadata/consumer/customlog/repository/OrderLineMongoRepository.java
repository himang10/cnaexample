package cnadata.consumer.customlog.repository;

import cnadata.consumer.customlog.data.OrderLine;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderLineMongoRepository extends MongoRepository<OrderLine, Long> {
}
