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
public class OrderMergedConverter extends CqrsConverter {

    @Override
    public ConvertedData convert(String topicName, StreamData data) {

        return new ConvertedData(
                data.getType().getSimpleName(),
                keySerialize(topicName, data.getValue()),
                valueSerialize(topicName, data.getValue()));

    }


    private CqrsProduceData valueSerialize(String topicName, Object object) {

        /**
         * Object 중 실제 Sink에 전달 후 사용하기 위한 parameter 만을 여기에 등록
         * 예를들어, MergedData에서 Object로 전달 시 Schema의 field에 포함하지 않을 경우 Field는 제외된다.
         */
        return super.createElementsForValue(topicName,
                object,
                SchemaElement.ValueOf("id", SchemaType.LONG, false),
                SchemaElement.ValueOf("customerId", SchemaType.LONG,false),
                SchemaElement.ValueOf("orderDate", SchemaType.STRING, true)
        );
    }

    private CqrsProduceData keySerialize(String topicName, Object object)
    {
        MergedData order = (MergedData) object;
        return super.createElementsForKey(topicName,
                SchemaElement.KeyOf("id",String.valueOf(order.getPurchaseOrderId()), SchemaType.LONG,false)
        );
    }
}

