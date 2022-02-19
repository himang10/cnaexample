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

package cnadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SagaOrchestratorApplication {

    public static void main(String[] args) {

        //PurchaseOrder createdOrder = new PurchaseOrder();
        //SagaManager sagaManager = new SagaManager();
        /* Create Saga가 시작했다는 것을 의미 */

        //sagaManager.begin(OrderPlacementSaga.class, createdOrder, PurchaseOrder.class);
       //sagaManager.begin(OrderPlacementSaga.class, createdOrder);

        SpringApplication.run(SagaOrchestratorApplication.class, args);
    }

}
