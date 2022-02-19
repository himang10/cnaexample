package cnadata.bff.data;

import cnadata.outbox.saga.core.SagaStatus;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@Table(name="purchaseorder")
public class PurchaseOrder {

    @Id
    @Column(name = "purchaseOrderId")
    //@Column(name = "purchase_order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long customerId;

    private Instant orderDate;

    private String TicketNumber;

    private String creditCardNo;

    //@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "purchaseOrder")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="purchaseOrderId")
    //@JoinColumn(name="purchase_order_id")
    private List<OrderLine> lineItems;

    // Order의 Saga 실행 상황을 업데이트 (Saga 시작 시 STARTED, 종료 시 COMPLETED or ABORTED로 상태 업데이트
    // saga에 들어가는 DomainEntity는 반드시 @Id와 @SagaStatus를 표시하여 한다.
    @SagaStatus
    private String sagaStatus;



    /**
     * 포함되어 있는 OrderLine 목록에서 동일 ID ORDER LINE을 찾아서 STATUS 를 변경
     * @param orderLineId
     * @param newStatus
     * @return
     */
    public OrderLine updateOrderLine (long orderLineId, OrderLineStatus newStatus) {
        log.info("PurchaseOrder.updateOrderLine: " + orderLineId + " " + newStatus.toString());
        for (OrderLine orderLine : lineItems) {
            if(orderLine.getId() == orderLineId) {
                orderLine.setStatus(newStatus);
                return orderLine;
            }
        }

        throw new EntityNotFoundException("Order does not contain line with id " + orderLineId);
    }

    public BigDecimal getTotalValue () {
        return lineItems.stream().map(OrderLine::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
