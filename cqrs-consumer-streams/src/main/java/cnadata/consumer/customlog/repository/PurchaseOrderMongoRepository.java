package cnadata.consumer.customlog.repository;

import cnadata.consumer.customlog.data.PurchaseOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PurchaseOrderMongoRepository extends MongoRepository<PurchaseOrder, Long> {
}
