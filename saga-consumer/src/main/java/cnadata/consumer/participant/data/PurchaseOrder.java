package cnadata.consumer.participant.data;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document (collection = "AppPurchaseOrder")
public class PurchaseOrder {
    @Id
    private long id;

    private long customerId;

    private Instant orderDate;

    private String TicketNumber;

    private String creditCardNo;
}
