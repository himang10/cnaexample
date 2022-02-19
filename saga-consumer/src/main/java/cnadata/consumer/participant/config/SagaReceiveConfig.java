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

package cnadata.consumer.participant.config;


import cnadata.consumer.participant.data.Payment;
import cnadata.consumer.participant.data.PurchaseOrder;
import cnadata.consumer.participant.handler.CustomerCancelSagaHandler;
import cnadata.consumer.participant.handler.CustomerSagaHandler;
import cnadata.consumer.saga.config.SagaConsumerConfiguration;
import cnadata.consumer.saga.receive.SagaParticipants;
import cnadata.consumer.saga.receive.StepResult;
import cnadata.outbox.publish.OutboxEventListener;
import cnadata.outbox.saga.core.SagaStepStatus;
import cnadata.outbox.saga.event.SagaEvent;
import cnadata.outbox.saga.event.SagaEventType;
import cnadata.outbox.saga.publish.SagaEventPublisher;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.persistence.*;
import java.util.function.Function;

@Slf4j
@Configuration
@Import({SagaConsumerConfiguration.class})
@EntityScan(basePackages = {"cnadata","cnadata.outbox"})
public class  SagaReceiveConfig {

    private final Gson gson = new Gson();

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

        /*
          Payment 처리
          주문에 대한 지불을 승인 (Payment 생성)하고 생성된  Payment를 리턴
         */
        Function<SagaEvent, StepResult> paymentHandler = se -> {
            PurchaseOrder order = se.getPayload(PurchaseOrder.class);
            // Response Event의 Payload에 Payment를 실는다.

            Payment payment = new Payment(order.getId(),
                    order.getCustomerId(),
                    0L,
                    order.getCreditCardNo()
            );

            entityManager.persist(payment);

            return StepResult.success(se, payment, "카드 승인 완료");
        };

        /*
          Payment 보상 처리
          주문과 연결된 지불 등록된 상태를 확인하고, 지물 내역을 취소처리
          만약 지불 내역이 없는 경우 Not found error 리턴
         */
        Function<SagaEvent, StepResult> cancelHandler = se -> {
            PurchaseOrder order = se.getPayload(PurchaseOrder.class);
            Payment payment = entityManager.find(Payment.class, order.getId());
            if(payment != null) {
                entityManager.remove(payment);
                return StepResult.success(se, payment, "카드 승인 보상 처리 완료");
            }
            return StepResult.failed(se, "404","승인된 카드 정보 없읍");
        };

        /**
         * "order-placement" Saga에 대한 Participant 영역의 처리 흐름을 설정.
         * add("{saga name}")를 통해 saga Participant 대생을 등록 시작
         * participant("{step-name}") step 처리 등록 (doSaga: 사가 처리, doCompensate: 보상 요청 처리)
         * add() 과정을 통해 여러 Saga 별 Step 등록 진행
         */
        SagaParticipants sagaRcvInstance = new SagaParticipants().add("order-placement")
                /**
                 * Order-Placement Saga의 Customer Step 처리 Participant 처리 영역
                 * customer step은 고객 포인트를 차감한다.
                 * Step 보상 처리는 차감된 포인트를 복구한다.
                 */
                .participant("customer")
                .doSaga()
                    .onEvent(new CustomerSagaHandler(entityManager))
                    .endThen()
                .doCompensate()
                    .onEvent(new CustomerSagaHandler(entityManager))
                    .end()
                /**
                 * Order-Placement Saga의 Payment Step 처리 Participant 처리 영역
                 * 주문에 대한 결제를 실행한다.
                 * 보상 처리는 없다.
                 */
                .participant("payment")
                .doSaga()
                    .onEvent(paymentHandler)
                    .post(se -> log.info("postHandler for APPROVED"))
                    .postError(se -> log.info("postHandler for FAILED"))
                    .end()
                .build();

        /**
         * "order-cancel" Saga에 대한 Participant 영역의 처리 흐름을 설정.
         * add("{saga name}")를 통해 saga Participant 대생을 등록 시작
         * participant("{step-name}") step 처리 등록 (doSaga: 사가 처리, doCompensate: 보상 요청 처리)
         * add() 과정을 통해 여러 Saga 별 Step 등록 진행
         */
        sagaRcvInstance.add("order-cancel")
                /**
                 * Order-Cancel Saga의 customer Step 처리 Participant 처리 영역
                 * customer Step 처리는 고객 포인트를 복구 한다.
                 * Step 보상 처리는 복구된 포인트를 차감한다.
                 */
                .participant("customer")
                .doSaga()
                    .onEvent(new CustomerSagaHandler(entityManager))
                    .endThen()
                .doCompensate()
                    .onEvent(new CustomerCancelSagaHandler(entityManager))
                    .end()
                /**
                 * Order-Cancel Saga의 Payment Step 처리 Participant 처리 영역'
                 * 주문에 대한 지불 내역을 취소한다.
                 * payment에 대한 보상 처리가 없다.
                 */
                .participant("payment")
                .doSaga()
                    .onEvent(cancelHandler)
                    .end()
                .build();

        log.debug("rcvFactory");

        return  sagaRcvInstance;
    }

}
