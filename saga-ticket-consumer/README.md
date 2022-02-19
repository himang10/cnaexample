
## **Participant 서비스 구성하기**



Participant는 Saga invokeParticipant 또는 invokeCompensate를 통해 요청 시 처리 수행에 참여하는 서비스를 의미한다.

### **의존성 주입**

Participant 의 의존성은 Saga Orchestrator 방식과 동일하게 설정하면 된다.

**CNA Data 라이브러리 의존성 주입 방법 **

CNA Data Library는 orchestrator를 위한 필수 cnadata-producer-lib.jar 외에 추가적으로 “cnadata-consumer-lib.jar” 라이브러리를 추가할 필요가 있다.


```exasol
$ mkdir lib
$ cd lib
$ cp ../../cnadata-producer-lib.jar .
$ cp ../../cnadata-consumer-lib.jar .
$ ls
cnadata-producer-lib.jar  cnadata-consumer-lib.jar
```


그리고 이 디렉토리 내에 있는 cnadata-producer-lib.jar와 cnadata-consumer-lib.jar 파일 위치를 다음과 같이 pom.xml에 의존성 주입한다.


```xml
<dependency>
    <groupId>cnadata</groupId>
    <artifactId>outboxlib</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>system</scope>
    <systemPath>${basedir}/lib/cnadata-producer-lib.jar</systemPath>
</dependency>
<dependency>
    <groupId>cnadata</groupId>
    <artifactId>outboxcustomlib</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>system</scope>
    <systemPath>${basedir}/lib/cnadata-consumer-lib.jar</systemPath>
</dependency>
```


### **속성 정의**

속성은 Saga Orchestrator 방식과 동일하게 설정하면 된다.

**Saga Event 처리를 위한 Configuration 설정**

Saga Participant 수신 처리를 위한 Configuration은 다음과 같은 Annotation을 포함하는 Configuration Bean을 생성한다.


```java
@Slf4j
@Configuration
@Import({SagaConsumerConfiguration.class})
@EntityScan(basePackages = {"cnadata","cnadata.outbox"})
public class SagaReceiveConfig {
```




Saga Orchestrator Configuration 과 동일하며, @Import class는 SagaConsumerConfiguration.class를 주입한다. 이 클래스는 Saga Event를 수신하여 처리를 위한 KafkaListener와 ConsumerFactory를 등록하기 위한 Bean를 생성하는 역할을 수행한다.

또한 Sag Orchestrator와 마찬가지로  Participant 간 Outbox Pattern 기반 비동기 통신을 수행하기 위해 SagaEventPublisher와 OutboxEventListener Bean을 다음과 같이 생성한다.


```java
@Bean
public OutboxEventListener listener() {
   return new OutboxEventListener();
}

@Bean
public SagaEventPublisher  sagaEventPublisher() {
   return new SagaEventPublisher();
}
```


**Saga Step 요청 처리를 위한 Receive 처리 Instance 생성 하기**

Saga Receive Instance는 Orchestrator로 부터의 요청을 처리하기 위한 절차를 설정한다.


```
SagaParticipants sagaParticipants = new SagaParticipants()
   .participant("order-placement", "credit-approval")
       .doSaga()
           .onEvent(new CustomerSagaHandler(entityManager))
           .post(se -> log.info("postHandler for APPROVED"))
           .postError(se -> log.info("postHandler for FAILED"))
           .endThen()
       .doCompensate()
           .onEvent(new CustomerSagaCancelHandler(entityManager))
           .post(se -> log.info("postHandler for REJECTED"))
           .postError(se -> log.info("postHandler for REJECTED"))
           .end()
   .participant(order-placement", "payment")
       .doSaga()
           .onEvent(paymentHandler)
           .post(se -> log.info("postHandler for APPROVED"))
           .postError(se -> log.info("postHandler for FAILED"))
           .endThen()
       .doCompensate()
           .onEvent(cancelHandler)
           .post(se -> log.info("postHandler for REJECTED"))
           .postError(se -> log.info("postHandler for REJECTED"))
           .end()
   .build();
```


SagaParticpants는 한 개 이상의 Participant를 등록할 수 있다. 위의 예에서는 Customer와 payment 처리를 위한 Participant 2개를 동시에 등록하는 코드이며, Customer와 Payment를 별도의 서비스로 분리할 경우 각 서비스 별로 별도의 Participant를 생성하면 됩니다.


<table>
  <tr>
   <td><strong>method 명</strong>
   </td>
   <td><strong>설명</strong>
   </td>
   <td><strong>참고</strong>
   </td>
  </tr>
  <tr>
   <td>participant
   </td>
   <td>saga name과 step name을 입력하며, 이를 기반으로 하나의 participant 를 생성한다
   </td>
   <td>
   </td>
  </tr>
  <tr>
   <td>doSaga()
   </td>
   <td>처리 요청을 받기 위한 프로세스를 생성한다.
   </td>
   <td>
   </td>
  </tr>
  <tr>
   <td>doCompensate()
   </td>
   <td>보상처리 요청을 받은 후 처리를 위한 프로세스를 등록한다. 
   </td>
   <td>
   </td>
  </tr>
  <tr>
   <td>onEvent()
   </td>
   <td>saga 요청 시 처리를 위한 Lambda 함수 또는 클래스를 정의한다
   </td>
   <td>
   </td>
  </tr>
  <tr>
   <td>post()
   </td>
   <td>정상 처리 후 후속 처리를 위한 Lambda 함수를 등록한다. return 은 없다.
   </td>
   <td>
   </td>
  </tr>
  <tr>
   <td>postError()
   </td>
   <td>처리 실패 시 후속 처리를 위한 Lambda 함수를 등록한다. 
   </td>
   <td>
   </td>
  </tr>
</table>


Saga 또는 Compensate Request 처리 함수

Saga Request 또는 Compensate Request 처리 함수는 다음과 같이 구성 할 수 있다.


```java
Function<SagaEvent, SagaStepStatus> paymentHandler = se -> {
   PurchaseOrder order = new Gson().fromJson(se.getPayload(), PurchaseOrder.class);
   Payment payment = new Payment(order.getId(),
           order.getCustomerId(),
           0L,
           "99999-0000000"
   );

   SagaStepStatus status;
   entityManager.persist(payment);
   // Response Event의 Payload에 Payment를 실는다.
   // 만약 설정하지 않으면 앞전에 전달했던 Request payload 값이 그대로 넘어간다.
   se.setPayload(new Gson().toJson(payment));

   return SagaStepStatus.SUCCEEDED;
   //return SagaStepStatus.FAILED;
};

Function<SagaEvent, SagaStepStatus> cancelHandler = se -> {
   PurchaseOrder order = new Gson().fromJson(se.getPayload(), PurchaseOrder.class);
   Payment payment = new Payment(order.getId(),
           order.getCustomerId(),
           0L,
           "99999-0000000"
   );

   entityManager.detach(payment);
   se.setPayload(new Gson().toJson(payment));

   return SagaStepStatus.COMPENSATED;
};
```


기본 Lambda 함수의 Input은 SagaEvent 이며, 응답 결과는 SagaStepStatus로 리턴한다.


```java
public class SagaEvent {
   private String eventId;  // message Id 이며, OutboxEvent.id (UUID String value)
   private String sagaId; // OutboxEvent.aggregateId (UUID.now().toString())
   private String payloadType; // payload class name
   private String sagaStepStatus;  //요청은 STARTED
   private String payload; // 요청에 대한 Entity 여기서는 PurchaseOrder Json객체
   private String currentStep; // 현지 진행되고 있는 step Name
   private String eventType; // Request or Cancel
   private String timestamp; 
}
```


함수 실행 시 주로 사용하는 것은 payload 객체이며, 처리 된 결과는 각 Step에 따라 다르게 리턴한다.

리턴 타입은 아래의 SagaStepStatus이며, Participant는 다음 2개중 한가지만 사용한다.


```java
public enum SagaStepStatus {
   FAILED,
   SUCCEEDED,
   COMPENSATED
}
```


Saga 처리 시에는 성공 시 SUCCEEDED, 실패 시 FAILED이며,

Compensate 시에는 성공 시 COMPENSATE, 실패 시 FAILED로 리턴한다.


## **Saga Orchestrator 시작 요청**



Saga 실행을 위해서는 다음과 같이 SagaManager를 실행 해야 한다.

호출 서비스에서는 sagaManager bean을 주입하고,


```java
private PurchaseOrderRepository purchaseOrderRepository;

@Autowired
public SagaOrderService(SagaManager sagaManager, PurchaseOrderRepository purchaseOrderRepository)  {
   this.sagaManager = sagaManager;
   this.purchaseOrderRepository = purchaseOrderRepository;
}
```


그리고, 서비스 호출 시 sagaManager.begin()을 호출해서 Saga Orchestrator 실행을 요청한다.


```java
PurchaseOrder createdOrder = purchaseOrderRepository.save(order);
/* Create Saga가 시작했다는 것을 의미 */

//sagaManager.begin("order-placement", OrderPlacementSaga.class, createdOrder);
if(sagaManager.begin("order-placement", createdOrder) == null) {
   log.info("order-placement saga is processing and you can't add this saga");
}
```





