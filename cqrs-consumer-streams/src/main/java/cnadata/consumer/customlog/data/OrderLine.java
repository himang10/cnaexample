package cnadata.consumer.customlog.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document (collection = "AppOrderLine")
public class OrderLine {
    @Id
    private Long id;

    private Long purchaseOrderId;
    private String item;
    private int quantity;
    private BigDecimal totalPrice;

    /*
     * 기본은 PurchaseOrder와 OrderLine 연결 시 PurchaseOrder에서 @OneToMany만 설정하면 된다.
     * 그러나 여기서 @ManyToOne으로 설정한 이유는 OrderLine 별도로 읽을때 이용할 수 있도록 설정한다.
     * 기본ㄷ은 @ManyToOne을 설정할 필요가 없다.
     * 만약 설정 시 에는 insertable=false, updatable=false로 설정해야 한다.

    @ManyToOne
    @JoinColumn(name = "purchase_order_id", insertable = false, updatable = false)
    private PurchaseOrder purchaseOrder;
*/

    @Enumerated (EnumType.STRING)
    private OrderLineStatus status;

    private String sagaStatus;


    public MergedData getMergeData(PurchaseOrder order)
    {
        return new MergedData().builder()
                .id(purchaseOrderId)
                .purchaseOrderId(purchaseOrderId)
                .customerId(order.getCustomerId())
                .orderDate(order.getOrderDate().toString())
                .item(item)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .status(status.toString())
                .build();
    }
}
