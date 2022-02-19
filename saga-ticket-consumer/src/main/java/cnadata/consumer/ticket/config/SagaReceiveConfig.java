/**
 * This example is a code explaining how to implement the Saga Architecture Pattern
 * using the CNA Data Platform. This example can be used by referring
 * to coaching or development using the CNA Data Platform.
 * (CNA: Cloud Native Application )
 *
 * @author Yong Woo Yi
 * @version 1.0
 * @since 2022
 */

package cnadata.consumer.ticket.config;


import cnadata.consumer.ticket.data.PurchaseOrder;
import cnadata.consumer.ticket.data.Ticket;
import cnadata.consumer.saga.config.SagaConsumerConfiguration;
import cnadata.consumer.saga.receive.SagaParticipants;
import cnadata.consumer.saga.receive.StepResult;
import cnadata.outbox.publish.OutboxEventListener;
import cnadata.outbox.saga.core.SagaStepStatus;
import cnadata.outbox.saga.publish.SagaEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Configuration
@Import({SagaConsumerConfiguration.class})
@EntityScan(basePackages = {"cnadata","cnadata.outbox"})
public class  SagaReceiveConfig {

    EntityManager entityManager;

    @Bean
    public OutboxEventListener listener() {
        return new OutboxEventListener();
    }


    @Bean
    public SagaEventPublisher sagaEventPublisher() {
        return new SagaEventPublisher();
    }
    @Autowired
    public SagaReceiveConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Bean
    public SagaParticipants sagaRcvInstance() {

        SagaParticipants sagaRcvInstance = new SagaParticipants().add("order-placement")
                /**
                 * Order-Placement 처리 구조
                 */
                .participant("ticket")
                    .doSaga()
                        .onEvent(se -> {
                            PurchaseOrder order = se.getPayload(PurchaseOrder.class);
                            Ticket ticket = new Ticket( order.getId(),
                                    UUID.randomUUID().toString(),
                                    LocalDateTime.now().toString(),
                                    "order {}: ticket 발급 완료");

                            entityManager.persist(ticket);

                            return StepResult.success(se, ticket, "Ticket 발급 완료");
                        })
                    .endThen()
                    .doCompensate()
                    .onEvent(se -> {
                        PurchaseOrder order = se.getPayload(PurchaseOrder.class);
                        Ticket ticket = entityManager.find(Ticket.class, order.getCustomerId());
                        if(ticket == null) {
                            log.debug("Customer Not Found: " + order.getCustomerId());

                            return StepResult.failed(se, order, "404", "등록된 Ticket을 찾을 수 없음");
                        }

                        entityManager.remove(ticket);

                        return StepResult.success(se, ticket, "발급 Ticket 취소 완료");
                    })
                    .end()
                .build();
                /**
                 * Order-Cancel 처리 구조
                 */
        sagaRcvInstance.add("order-cancel")
                .participant("ticket")
                    .doSaga()
                    .onEvent(se -> {
                        PurchaseOrder order = se.getPayload(PurchaseOrder.class);
                        Ticket ticket = entityManager.find(Ticket.class, order.getId());
                        if(ticket == null) {
                            log.debug("Customer Not Found: " + order.getCustomerId());

                            return StepResult.failed(se, order, "404", "등록된 Ticket을 찾을 수 없음");
                        }

                        entityManager.remove(ticket);

                        return StepResult.success(se, ticket, "발급 Ticket 취소 완료");
                    })
                    .end()
                .build();



        log.debug("rcvFactory");

        return  sagaRcvInstance;
    }

}
