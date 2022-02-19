package cnadata.consumer.customlog.data;

import cnadata.outbox.util.JsonObjectWithInit;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.math.BigDecimal;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection="AppMergedData")
/**
 * PurchaseOrder + OderLine을 Merged해서 새로운 MergedData를 생성하고 이를 Sink Connector에 전달하고자 할때
 * 사용하는 클래스 구조
 * Inner Class: PurchaseOrder 와 OrderLine은 MergedData Class 중 변경되거나 생성되는 Field만을 추출해서
 * 동일 Aggregate (MergedData)로 전달하기 위해 사용함
 */
public class MergedData {
    @Id
    private long id;

    private long purchaseOrderId;

    private long customerId;

    private String orderDate;

    private String item;
    private int quantity;
    private BigDecimal totalPrice;
    private String status;



    public static MergedData getOrderLine(
            JsonObject ol
    ) {
        JsonObjectWithInit object = new JsonObjectWithInit (ol);

        return new MergedData().builder()
                .id(object.getAsLong("purchaseOrderId"))
                .purchaseOrderId(object.getAsLong("purchaseOrderId"))
                .item(object.getAsString("item"))
                .quantity(object.getAsInt("quantity"))
                .totalPrice(object.getAsBigDecimal("totalPrice"))
                .status(object.getAsString("status"))
                .build();
    }
    public static MergedData getPurchaseOrder(JsonObject order)
    {
        JsonObjectWithInit object = new JsonObjectWithInit (order);

        return new MergedData().builder()
                .id(object.getAsLong("purchaseOrderId"))
                .purchaseOrderId(object.getAsLong("purchaseOrderId"))
                .customerId(object.getAsLong("customerId"))
                .orderDate(object.getAsString("orderDate"))
                .build();

    }
}
