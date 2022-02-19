package cnadata.consumer.customlog.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
//@Document (collection = "AppPurchaseOrder")
public class PurchaseOrder {
    private long id;

    private long purchaseOrderId;

    private long customerId;

    private Instant orderDate;

    private String sagaStatus;

    public MergedData getMergeData(OrderLine orderLine)
    {
        return new MergedData().builder()
                .id(purchaseOrderId)
                .purchaseOrderId(purchaseOrderId)
                .customerId(customerId)
                .orderDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .item(orderLine.getItem())
                .quantity(orderLine.getQuantity())
                .totalPrice(orderLine.getTotalPrice())
                .status(orderLine.getStatus().toString())
                .build();
    }
}
