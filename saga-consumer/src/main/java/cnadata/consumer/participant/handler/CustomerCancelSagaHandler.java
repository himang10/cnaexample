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

package cnadata.consumer.participant.handler;

import cnadata.consumer.participant.data.Customer;
import cnadata.consumer.participant.data.PurchaseOrder;
import cnadata.consumer.saga.receive.StepResult;
import cnadata.outbox.saga.event.SagaEvent;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import java.util.function.Function;

@Slf4j
public class CustomerCancelSagaHandler implements Function<SagaEvent, StepResult>{

    EntityManager entityManager;

    public CustomerCancelSagaHandler(EntityManager entityManger) {
        this.entityManager = entityManger;
    }
    @Override
    public StepResult apply(SagaEvent se) {

        PurchaseOrder order = se.getPayload(PurchaseOrder.class);
        Customer customer = entityManager.find(Customer.class, order.getCustomerId());
        if(customer == null) {
            log.debug("Customer Not Found: " + order.getCustomerId());

            return StepResult.failed(se, order, "등록되지 않은 고객");
        }

        // 정상 처리 요청인가를 확인
        if(se.isRequest()) {
            customer.plusPoint(1000);
            entityManager.persist(customer);

            return StepResult.success(se, customer, "고객 포인트 차감 취소");
        }
        //보상 처리 요청 시
        else if(se.isCompensate()) {
            if (customer.isPoint(1000)) {
                customer.minusPoint(1000);
                entityManager.persist(customer);
                return StepResult.success(se, customer, "고객 포인트 차감");
            }

            return StepResult.failed(se, customer, "404","고객 포인트 차감 시 잔액 부족");
        }

        return StepResult.failed(se, "404","알수 없는 요구 사항");
    }
}