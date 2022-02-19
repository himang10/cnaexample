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

package cnadata.bff.service;

import cnadata.bff.data.PurchaseOrder;
import cnadata.bff.repository.PurchaseOrderRepository;
import cnadata.outbox.saga.domain.SagaState;
import cnadata.outbox.saga.core.SagaBase;
import cnadata.outbox.saga.core.SagaManager;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SagaOrderService {

    private SagaManager sagaManager;
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    public SagaOrderService(SagaManager sagaManager, PurchaseOrderRepository purchaseOrderRepository)  {
        this.sagaManager = sagaManager;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }
    /**
     *
     * @param order
     * @return createOrder
     */
    @Transactional
    public String addOrder(PurchaseOrder order) {

        log.debug("SagaOrderService.addOrder");
        log.debug("PurchaseOrder : " + new Gson().toJson(order));

        PurchaseOrder createdOrder = purchaseOrderRepository.save(order);
        /* Create Saga가 시작했다는 것을 의미 */

        SagaBase saga = sagaManager.begin("order-placement", createdOrder);
        if(saga == null) {
            log.debug("order-placement saga is processing and you can't add this saga");
            return "order-placement saga is processing and you can't add this saga";
        }

        return saga.getState().getId();
    }

    /**
     *
     * @param orderId
     * @return
     */
    @Transactional
    public String deleteOrder(Long orderId, Long customerId) {
        log.debug("call deleteOrder (orderId): " + orderId);

        Optional<PurchaseOrder> op = purchaseOrderRepository.findById(orderId);
        if(op.isEmpty()) {
            return "failed";
        }

        if(sagaManager.begin("order-cancel", op.get()) == null) {
            log.debug("order-placement saga is processing and you can't delete this saga");

            return "Let's try to delete after just completes the saga in processing ";
        }

        return "deleted order";
    }


    public List<SagaState> getSagaStates(String sagaId) {
        if(sagaId == null) {
            return sagaManager.getSagaStates();
        }

        List<SagaState> states = new ArrayList<>();
        states.add(sagaManager.getSagaState(sagaId));

        return states;
    }
}
