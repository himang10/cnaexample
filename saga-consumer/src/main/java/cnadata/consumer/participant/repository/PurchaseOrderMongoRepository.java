package cnadata.consumer.participant.repository;

import cnadata.consumer.participant.data.PurchaseOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PurchaseOrderMongoRepository extends MongoRepository<PurchaseOrder, Long> {
}
