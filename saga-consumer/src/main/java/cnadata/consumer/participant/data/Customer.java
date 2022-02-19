package cnadata.consumer.participant.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Customer {
    @Id
    private Long id;

    public long point;

    @Version
    private int version;

    private String creditCardNo;

    public boolean isPoint(long paymentDue) {
        return point - paymentDue > 0;
    }

    public void minusPoint(long paymentDue) {
        point = point - paymentDue;
    }

    public void plusPoint(long paymentDue) {
        point = point + paymentDue;
    }
}
