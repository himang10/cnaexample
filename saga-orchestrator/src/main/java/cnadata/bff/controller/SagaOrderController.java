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

package cnadata.bff.controller;

import cnadata.bff.data.OrderLineStatus;
import cnadata.bff.data.PurchaseOrder;
import cnadata.bff.dto.OrderLineDto;
import cnadata.bff.dto.OrderMessage;
import cnadata.bff.service.SagaOrderService;
import cnadata.outbox.saga.domain.SagaState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/saga", produces = "application/json")
public class SagaOrderController {
    @Autowired
    private SagaOrderService orderService;

    @GetMapping("/saga-states")
    @ResponseStatus(HttpStatus.OK)
    public List<SagaState> getSagaStates (String sagaId) {
        //log.debug("Saga-states {}", id);
        return orderService.getSagaStates(sagaId);
    }


    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public String addOrder(@RequestParam String customerId) {
        //public CustomLogData postLogData(@RequestBody CustomLogData customLogData) {
        //public String postLogData() {
        List<OrderLineDto> lineItems = new ArrayList<>();

        log.debug("SAGA add Order");

        lineItems.add(new OrderLineDto().builder()
                .item("item")
                .totalPrice(BigDecimal.valueOf(100))
                .quantity(2)
                .status(OrderLineStatus.ENTERED)
                .build());

        OrderMessage orderMessage = new OrderMessage().builder()
                .orderDate(Instant.now())
                .customerId(Long.valueOf(customerId))
                .lineItems(lineItems)
                .build();

        System.out.println("Order Message = " + orderMessage.toString());

        PurchaseOrder order = orderMessage.toOrder();

        return orderService.addOrder(order);
    }

    @DeleteMapping("/orders/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteOrderId(@PathVariable long id) {
        log.info("call deleteOrder {} {}", id, 0);
        return orderService.deleteOrder(id, 0L);
    }

}
