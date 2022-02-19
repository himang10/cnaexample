package cnadata.consumer.customlog.config;

import cnadata.consumer.cqrs.stream.CqrsStreams;
import cnadata.consumer.cqrs.stream.CqrsStreamsConfiguration;
import cnadata.consumer.cqrs.stream.StreamData;
import cnadata.consumer.customlog.data.MergedData;
import cnadata.consumer.customlog.data.OrderLine;
import cnadata.consumer.customlog.data.PurchaseOrder;
import cnadata.consumer.customlog.handler.OrderLineMergedConverter;
import cnadata.consumer.customlog.handler.OrderMergedConverter;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaAdmin;

import javax.persistence.EntityManager;
import java.util.function.Function;

@Slf4j
@Configuration
@Import({CqrsStreamsConfiguration.class})
@EntityScan(basePackages = {"cnadata"})
public class CqrsStreamConfig {

    @Autowired
    KafkaAdmin admin;

    EntityManager entityManager;

    @Autowired
    public CqrsStreamConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Bean
    public CqrsStreams cqrsStreams() {
        log.debug("CqrsStreams Bean Instance");


        Function<JsonObject, StreamData> orderConsumed = sd -> {
            MergedData order = MergedData.getPurchaseOrder(sd);
            return new StreamData(order.getClass(), String.valueOf(order.getPurchaseOrderId()), order);
        };

        Function<StreamData, StreamData> orderMapper = sd -> {
            StreamData output = new StreamData(MergedData.class, sd.getKey(), sd.getValue());

            return output;
        };


        Function<JsonObject, StreamData> orderLineConsumed = sd -> {
            MergedData orderLine = MergedData.getOrderLine(sd);

            StreamData output = new StreamData(orderLine.getClass(), String.valueOf(orderLine.getPurchaseOrderId()), orderLine);
            
            return output;
        };


        Function<StreamData, StreamData> orderLineMapper = sd -> {
            StreamData output = new StreamData(MergedData.class, sd.getKey(), sd.getValue());

            return output;
        };


        /**
         * CQRS Streams 파이프라인을 생성한다.
         * PurchaseOrder 객체에 대한 CUD 정보를 MyProject.source.PurchaseOrder topic으로 수신하여
         * consumedHandler --> mapperHandler 로 처리 후
         * 그 결과를 MyProject.sinkPurchaseOrder topic으로 MergedData로 Serializer 전송한다.
         */
        CqrsStreams cqrsStreams = new CqrsStreams()
                .stream(PurchaseOrder.class, "MyProject.PurchaseOrder")
                    .consumed(orderConsumed)
                    .mapValues(orderMapper)
                    .produced("MyProject.stream.MergedData", new OrderMergedConverter())
                    .endThen()
                .stream(OrderLine.class, "MyProject.OrderLine")
                    .consumed(orderLineConsumed)
                    .mapValues(orderLineMapper)
                    .produced("MyProject.stream.MergedData", new OrderLineMergedConverter())
                //.reduce("MyProject.stream.MergedData", new MergedSerializer())
                .build();

        return  cqrsStreams;
    }

}
