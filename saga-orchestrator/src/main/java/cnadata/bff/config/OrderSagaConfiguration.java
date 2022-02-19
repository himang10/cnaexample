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

package cnadata.bff.config;

import cnadata.bff.data.Customer;
import cnadata.bff.data.Payment;
import cnadata.bff.data.PurchaseOrder;
import cnadata.bff.data.Ticket;
import cnadata.outbox.publish.OutboxEventListener;
import cnadata.outbox.publish.OutboxEventPublisher;
import cnadata.outbox.saga.config.SagaOrchestratorConfiguration;
import cnadata.outbox.saga.config.StepResponse;
import cnadata.outbox.saga.publish.SagaEventPublisher;
import cnadata.outbox.saga.config.SagaOrchestrator;
import cnadata.outbox.saga.event.StepMessage;
import cnadata.outbox.saga.core.SagaManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;

@Data
@Slf4j
@Configuration
@Import({SagaOrchestratorConfiguration.class})
@EntityScan(basePackages = {"cnadata.bff","cnadata.outbox"})
public class OrderSagaConfiguration {

    private final EntityManager entityManager;

    @Bean
    public OutboxEventListener listener() {
        return new OutboxEventListener();
    }

    @Bean
    public OutboxEventPublisher publisher() {
        return new OutboxEventPublisher();
    }

    @Bean
    public SagaEventPublisher  sagaEventPublisher() {
        return new SagaEventPublisher();
    }

    @Autowired
    public OrderSagaConfiguration(EntityManager  entityManager) {
        this.entityManager = entityManager;
    }

    @Bean
    public SagaManager manager() {
        SagaManager manager = new SagaManager();

        /**
         * "order-placement" Saga에 대한 Orchestrator 영역 설정
         * SagaOrchestrator("{saga-name}")으로 Saga Orchestrator 1개를 생성하며,
         * 개별 step("{step-name}") 을 구성한다.
         * invokeParticipant(필수): step에 대응되는 Participant 서비스를 호출하기 위한 Lamdba 함수를 등록한다
         * - req: SagaMessage (AggregateObject, Saga 처리 상세 정보 : SagaStateDetailed, step 처리 응답: StepResult)
         * - return: StepMessage.send() or StepMessage.replaceAndSend() 선택
         * StepMessage.replaceAndSend()는 step 메시지 전달 시 기존 Aggregate(PurchaseOrder)가 아닌 다른 객체 정보로 바꿔서 전달할때
         * 사용 (이것은 호출 하는 시점에 객체를 Replace 하는 효과가 있으며, 다른 Step에는 영향을 주지 않음)
         *
         * onParticipant(선택): step 처리 결과 응답 처리를 수행하는 Lambda 함수 등록
         * 응답 수신 후 내부 처리를 위한 처리 수행 진행.
         * 내부 처리 로직을 포함해서 응답 받을 값들에 대해 rsp.setSagaCached()를 사용하여 Saga 전체 흐름에서 활용하기 위한
         * Cache 정보를 저장하고 사용 할 수 있음
         * - rsp: SagaMessage
         * - return: void
         *
         * withCompensate(선택): step 보상 처리를 위한 participant 호출하는 함수를 등록
         * Lambda ㄹ함수 사용 방식은 invokeParicipant와 동일
         *
         * onCompensate(선택): step 보상 처리 응답에 대한 처리 Lambda 함수 등록.
         * onParticipant와 동일
         *
         * successSaga(선택): Saga 모두 성공했을 때 후속 처리를 위한 Lambda 함수 등록
         *
         * failedSaga(선택): Saga가 실패(보상 처리 실행)했을 때 후속 처리를 위한 Lambda 함수 등록
         *
         */
        SagaOrchestrator approvalSaga = new SagaOrchestrator("order-placement")
                .step("customer")
                    //1.1. customer participant로 order를 payload에 담아서 요청 처리
                    .invokeParticipant(req -> {
                        log.debug("customer Step Request: sagaObject: {} preStepResult: {} ",
                                req.getSagaCached(PurchaseOrder.class), req.getPreStepResult());
                        return StepMessage.send();
                    })
                    .onParticipant(rsp -> {
                        // Saga Aggregate 정보를 읽어온다.
                        PurchaseOrder order = rsp.getSagaAggregate(PurchaseOrder.class);
                        // Step 에 대한 응답으로 받은 Customer를 읽어온다.
                        Customer customer = rsp.getStepResult().getParticipant(Customer.class);
                        //order 객체에 CreditCardNo를 설정한다.
                        order.setCreditCardNo(customer.getCreditCardNo());
                        // 변경된 Order 정볼를 Saga Flow에 Caching 수행
                        rsp.setSagaCached(order, PurchaseOrder.class);
                    })
                    // ticket Failed or Compensate 시 보상 처리 실행
                    // 기본적으로 SagaAggregate 를 payload에 담아서 전달
                    .withCompensate(req -> StepMessage.send())
                    .end()
                .step("ticket")
                    //1.1. ticket 처리 요청
                    .invokeParticipant(req -> StepMessage.send())
                    //1.2 ticket saga 처리 결과 수신 후 ticket 정보를 SagaState의 Payload에 저장
                    .onParticipant(rsp -> {
                        // 앞의 Step에서 Caching한 Order 를 읽어온다.
                        PurchaseOrder order = rsp.getSagaCached(PurchaseOrder.class);
                        Ticket ticket = rsp.getStepResult().getParticipant(Ticket.class);
                        order.setTicketNumber(ticket.getTicketNumber());
                        rsp.setSagaCached(order, PurchaseOrder.class);
                    })
                    //2.1 Ticket 취소 처리 요청
                    .withCompensate(req -> StepMessage.send())
                    .end()
                .step("payment")
                    //1.1. 결제 처리 요청
                    .invokeParticipant(req-> {
                        PurchaseOrder order = req.getSagaCached(PurchaseOrder.class);

                        return StepMessage.replaceAndSend(order, PurchaseOrder.class);
                    })
                    //1.2. 결제 처리 결과 응답 시 결제 결과를 SagaState에 저장
                    .onParticipant(rsp -> {
                        PurchaseOrder order = rsp.getSagaCached(PurchaseOrder.class);
                        StepResponse response = rsp.getPreStepResult();
                        Payment payment = response.getParticipant(Payment.class);
                        order.setCreditCardNo(payment.getCreditCardNo());

                        rsp.setSagaCached(order, PurchaseOrder.class);

                    })
                    .end()
                .successSaga(detailed -> {
                    PurchaseOrder sagaOrder = detailed.getSagaCached(PurchaseOrder.class);
                    PurchaseOrder order = entityManager.find(PurchaseOrder.class, sagaOrder.getId());
                    order.setCreditCardNo(sagaOrder.getCreditCardNo());
                    order.setTicketNumber(sagaOrder.getTicketNumber());
                    entityManager.persist(order);

                    log.info("Saga Process Status -- Customer Detailed: {} ", detailed.getStepDetailed("customer"));
                    log.info("Saga Process Status -- Ticket Detailed: {} ", detailed.getStepDetailed("ticket"));
                    log.info("Saga Process Status -- Payment Detailed: {} ", detailed.getStepDetailed("payment"));
                    log.info("Saga Process Status -- Cached Data: {} ", detailed.getSagaCached(PurchaseOrder.class));
                    log.info("Saga Process Status -- Saga Aggregate: {} ", detailed.getSagaAggregate(PurchaseOrder.class));

                    return order;
                })
                .failedSaga(state-> {
                    PurchaseOrder order = state.getSagaAggregate(PurchaseOrder.class);
                    order = entityManager.find(PurchaseOrder.class, order.getId());
                    entityManager.remove(order);

                    return order;
                })
                .build();

        manager.putSagaOrchestrator(approvalSaga);

        /**
         * 주문 취소 처리를 위한 Saga Orchestrator 설정
         */
        SagaOrchestrator cancelSaga = new SagaOrchestrator("order-cancel")
                .step("customer")
                    .invokeParticipant(req -> StepMessage.send())
                    .withCompensate(req -> StepMessage.send())
                    .retry(3)
                    .end()
                .step("payment")
                    .invokeParticipant(req -> StepMessage.send())
                    .retry(3)
                    .end()
                .step("ticket")
                    .invokeParticipant(req -> StepMessage.send())
                    .retry(0)
                .end()
                .successSaga(state-> {
                    PurchaseOrder order = entityManager.find(PurchaseOrder.class, state.getSagaCachedId());
                    entityManager.remove(order);

                    return order;
                })
                .build();

        manager.putSagaOrchestrator(cancelSaga);


        return manager;
    }

}
