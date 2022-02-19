package cnadata.bff.dto;

import cnadata.bff.data.OrderLine;
import cnadata.bff.data.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderMessage {

    private long customerId;
    private Instant orderDate;
    private List<OrderLineDto> lineItems;


    public PurchaseOrder toOrder() {

        List<OrderLine> lines = lineItems.stream()
                .map(ol -> new OrderLine(ol.getItem(), ol.getQuantity(), ol.getTotalPrice()))
                .collect(Collectors.toList());


        return new PurchaseOrder().builder()
                .customerId(customerId)
                .orderDate(orderDate)
                .lineItems(lines)
                .build();
    }

    public static OrderMessage from(PurchaseOrder order) {
        List<OrderLineDto> lines = order.getLineItems()
                .stream()
                .map(ol->new OrderLineDto().builder()
                                .item(ol.getItem())
                                .quantity(ol.getQuantity())
                                .totalPrice(ol.getTotalPrice())
                                .status(ol.getStatus())
                                .build()
                ).collect(Collectors.toList());

        return new OrderMessage().builder()
                .customerId(order.getCustomerId())
                .orderDate(order.getOrderDate())
                .lineItems(lines)
                .build();
    }

}
