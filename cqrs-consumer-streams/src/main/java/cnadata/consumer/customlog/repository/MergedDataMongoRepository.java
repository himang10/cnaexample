package cnadata.consumer.customlog.repository;

import cnadata.consumer.customlog.data.MergedData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MergedDataMongoRepository extends MongoRepository<MergedData, Long> {
}
