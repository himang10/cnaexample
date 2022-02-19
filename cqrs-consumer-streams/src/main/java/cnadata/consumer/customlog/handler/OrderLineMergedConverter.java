package cnadata.consumer.customlog.handler;

import cnadata.consumer.cqrs.schema.ConvertedData;
import cnadata.consumer.cqrs.schema.CqrsProduceData;
import cnadata.consumer.cqrs.schema.SchemaElement;
import cnadata.consumer.cqrs.schema.SchemaType;
import cnadata.consumer.cqrs.stream.CqrsConverter;
import cnadata.consumer.cqrs.stream.StreamData;
import cnadata.consumer.customlog.data.MergedData;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class OrderLineMergedConverter extends CqrsConverter {

    @Override
    public ConvertedData convert(String topicName, StreamData data) {

        return new ConvertedData(
                data.getType().getSimpleName(),
                keySerialize(topicName, data.getValue()),
                valueSerialize(topicName, data.getValue()));

    }


    private CqrsProduceData valueSerialize(String topicName, Object object) {

        return super.createElementsForValue(
                topicName,
                object,
                SchemaElement.ValueOf("id", SchemaType.LONG, false),
                SchemaElement.ValueOf("item", SchemaType.STRING,true),
                SchemaElement.ValueOf("quantity", SchemaType.INT, true),
                SchemaElement.ValueOf("status", SchemaType.STRING, true),
                SchemaElement.ValueOf("totalPrice", SchemaType.LONG, true)
        );
    }

    private CqrsProduceData keySerialize(String topicName, Object object)
    {
        MergedData order = (MergedData) object;
        return super.createElementsForKey(
                topicName,
                SchemaElement.KeyOf("id",String.valueOf(order.getPurchaseOrderId()), SchemaType.LONG, false)
        );
    }
}

