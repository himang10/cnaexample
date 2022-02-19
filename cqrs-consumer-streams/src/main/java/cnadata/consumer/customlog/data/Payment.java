package cnadata.consumer.customlog.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Payment {
    @Id
    private Long orderId;

    public Long customerId;

    public Long paymentDue;

    public String creditCardNo;
}