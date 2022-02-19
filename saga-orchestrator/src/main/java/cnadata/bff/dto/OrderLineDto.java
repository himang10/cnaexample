package cnadata.bff.dto;

import cnadata.bff.data.OrderLineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineDto {
    private String item;
    private int quantity;
    private BigDecimal totalPrice;
    private OrderLineStatus status;
}
