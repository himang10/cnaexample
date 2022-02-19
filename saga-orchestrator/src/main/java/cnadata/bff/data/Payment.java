package cnadata.bff.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    private Long orderId;

    public Long customerId;

    public Long paymentDue;

    public String creditCardNo;
}