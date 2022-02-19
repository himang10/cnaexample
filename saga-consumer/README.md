
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


```java
SagaParticipants sagaParticipants = new SagaParticipants()
   .add("order-placement") // Saga Name 등록
   .participant("credit-approval") // 수신 받을 Step Name
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
   .participant("payment")
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
   <td><strong>필수</strong>
   </td>
  </tr>
  <tr>
   <td>add
   </td>
   <td>추가 할 Saga Name을 등록한다. 
<p>
여기서 한개 이상의 Saga를 추가할 수 있다. 
   </td>
   <td>O
   </td>
  </tr>
  <tr>
   <td>participant
   </td>
   <td>step name을 입력하며, 이를 기반으로 하나의 participant 를 생성한다
   </td>
   <td>O
   </td>
  </tr>
  <tr>
   <td>doSaga()
   </td>
   <td>처리 요청을 받기 위한 프로세스를 생성한다.
   </td>
   <td>O
   </td>
  </tr>
  <tr>
   <td>onEvent()
   </td>
   <td>Saga 처리 요청을 Listening 하는 Lambda 함수를 등록한다. 
<p>
Function&lt;SagaEvent, StepResult>
   </td>
   <td>O
   </td>
  </tr>
  <tr>
   <td>doCompensate()
   </td>
   <td>보상처리 요청을 받은 후 처리를 위한 프로세스를 생성한다. 
   </td>
   <td>
   </td>
  </tr>
  <tr>
   <td>onEvent()
   </td>
   <td>saga 요청 시 처리를 위한 Lambda 함수를 등록한다
<p>
Function&lt;SagaEvent, StepResult>
   </td>
   <td>
   </td>
  </tr>
  <tr>
   <td>post()
   </td>
   <td>정상 처리 후 후속 처리를 위한 Lambda 함수를 등록한다. return 은 없다.
<p>
Consumer&lt;SagaEvent>
   </td>
   <td>
   </td>
  </tr>
  <tr>
   <td>postError()
   </td>
   <td>처리 실패 시 후속 처리를 위한 Lambda 함수를 등록한다. 
<p>
onsumer&lt;SagaEvent>
   </td>
   <td>
   </td>
  </tr>
</table>


**SagaEvent**


```java
public class SagaEvent {
   public <T> T getPayload(Class<T> cls);
   public boolean isCompensate();
   public boolean isRequest();
```



<table>
  <tr>
   <td><strong>method 명</strong>
   </td>
   <td><strong>설명</strong>
   </td>
  </tr>
  <tr>
   <td>getPayload
   </td>
   <td>SagaOrchestrator에서 전달한 SagaAggregate Object 객체를 리턴한다.
<p>
Orchestrator의 invokeParticipant에서 호출한 replaceAndSend()를 통해 발송한 객체가 여기서 읽어들일 수 있다
   </td>
  </tr>
  <tr>
   <td>isCompensate
   </td>
   <td>전달된 요청이 Compensate인지 확인한다.
   </td>
  </tr>
  <tr>
   <td>isRequest
   </td>
   <td>전달된 요청이 Saga Request인지 확인한다.
   </td>
  </tr>
</table>


isCompensate 또는 isRequest 는 동일 Function&lt;SagaEvent, StepResult> 클래스를 하나로 만들 경우 내부에서 요청 메시지 종류를 구분하기 위해 활용 할 수 있다

StepResult


```java
public class StepResult {
   static StepResult success(SagaEvent se, String reason);
   static StepResult success(SagaEvent se, String returnCode, String reason);
   static StepResult success(SagaEvent se, Object participant, String reason);
   static StepResult success(SagaEvent se, Object participant, String returnCode, String reason);
   public static StepResult failed(SagaEvent se, String reason) 
   public static StepResult failed(SagaEvent se, String returnCode, String reason);
   public static StepResult failed(SagaEvent se, Object participant, String reason);
   public static StepResult failed(SagaEvent se, Object participant, String returnCode, String reason);
}
```


처리 결과 성공적이면 success를 호출하고 실패인 경우 failed를 호출한다.

**onEvent 등록 Lambda 함수**

invoke Participant 호출에 대해 실해되는 Listener 함수는 다음과 같다.


```java
/*
 Payment 처리
 주문에 대한 지불을 승인 (Payment 생성)하고 생성된  Payment를 리턴
*/
Function<SagaEvent, StepResult> paymentHandler = se -> {
   PurchaseOrder order = se.getPayload(PurchaseOrder.class);
   // Response Event의 Payload에 Payment를 실는다.

   Payment payment = new Payment(order.getId(),
           order.getCustomerId(),
           0L,
           order.getCreditCardNo()
   );

   entityManager.persist(payment);

   return StepResult.success(se, payment, "카드 승인 완료");
};
```


SagaEvent::getPayload()를 호출하여 전달 받은 객체 정보를 추출하며, 이를 기반으로 payment 처리를 실행한다. 그리고 그 결과는 StepResult.suucess() 메소드를 호출 하여 전달한 payment 객체와 reason string을 입력한다. 이것은 default로 retCode=”200”으로 전달된다.

**만약 실패 인 경우 StepResult.failed() method를 호출하게 되는데 이러한 경우 Saga 처리를 보상 처리 실행이 기동되며, Rollback 처리가 된다.**

다음 Lambda 함수는 주문 취소 처리 Saga (“order-cancel”)를 위한 step payment 처리 함수이다.


```java
/*
 Payment 보상 처리
 주문과 연결된 지불 등록된 상태를 확인하고, 지물 내역을 취소처리
 만약 지불 내역이 없는 경우 Not found error 리턴
*/
Function<SagaEvent, StepResult> cancelHandler = se -> {
   PurchaseOrder order = se.getPayload(PurchaseOrder.class);
   Payment payment = entityManager.find(Payment.class, order.getId());
   if(payment != null) {
       entityManager.remove(payment);
       return StepResult.success(se, payment, "카드 취소 처리 완료");
   }
   return StepResult.failed(se, "404","승인된 카드 정보 없음");
};
```


승인된 카드에 대해 취소 처리를 수행한다. 이때 취소 처리 대상이 없으면 “404” 에러와 Reason을 리턴한다. 리턴된 결과는 SagaState의 SagaStateDetailed에서 상세 확인할 수 있다.

