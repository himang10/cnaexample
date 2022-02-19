
<h1>Actuator의 Readiness & Liveness 설정</h1>


<p>with spring boot 2.3.3.RELEASE 이상

## Overview




본 가이드는 spring boot 기반의 k8s의 Pod Lifecycle을 관리를 위한 Liveness & Readiness 구성을 위한 방법을 가이드 한다. 일반적으로 아래와 같이 가이드에 따라 수행할 수 있으며, ADMP를 적용 시 k8s yaml, pom.xml, application.yaml 관련 내용을 자동으로 주입하여 제공하므로 특별하게 추가적인 코딩 작업은 필요없으나, 외부 연동 되는 서비스 또는 상태를 좀더 정교하게 관리하기 위해서는 spring boot에서 제공하는 State를 변경하는 기능을 customization함으로써 Application 특성에 따른 Lifecycle을 보다 정교하고 쉽게 관리할 수 있다. 

## **Kubernetes 의 Pod Lifecycle 관리 (AMDP 자동 주입)**




kubernetes는 Container의 준비 상태를 확인하고 준비가 되어 있지 않으면 Traffic이 유입되지 않도록 Application의 Readiness State 확인할 수 있는 인터페이스를 제공한다. 또한 Application이 정상적으로 동작하는지를 지속적으로 확인하기 위해 Liveness State를 지속적으로 확인하여 정상적이지 않을때 shutdown & restart 할 수 있도록 한다. 

이를 위해 다음과 같이 Pod 내 배포 설정에 liveness 와 Readiness를 주입해야 한다. 

⨁ Readiness 는 일반적으로 Application과 관련된 외부 서비스의 상태를 확인해서 정상적으로 서비스가 불가 한 경우 트래픽 유입을 차단하며, (k8s 내에서 endpoint 제거). 정상화될 경우 다시 활성화한다


```yaml
ports:
- name: probe-port
  containerPort: 8080
  hostPort: 8080

livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: probe-port
  failureThreshold: 2
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: probe-port
  failureThreshold: 30
  periodSeconds: 10

startupProbe:
  httpGet:
    path: /actuator/health/liveness
    port: probe-port
  failureThreshold: 2
  periodSeconds: 10
```



<table>
  <tr>
   <td>참고
   </td>
  </tr>
  <tr>
   <td>
<ul>

<li><code>livenessProbe</code>: 컨테이너가 동작 중인지 여부를 나타낸다. 만약 활성 프로브(liveness probe)에 실패한다면, kubelet은 컨테이너를 죽이고, 해당 컨테이너는 <a href="https://kubernetes.io/ko/docs/concepts/workloads/pods/pod-lifecycle/#restart-policy">재시작 정책</a>의 대상이 된다. 만약 컨테이너가 활성 프로브를 제공하지 않는 경우, 기본 상태는 <code>Success</code>이다.

<li><code>readinessProbe</code>: 컨테이너가 요청을 처리할 준비가 되었는지 여부를 나타낸다. 만약 준비성 프로브(readiness probe)가 실패한다면, 엔드포인트 컨트롤러는 파드에 연관된 모든 서비스들의 엔드포인트에서 파드의 IP주소를 제거한다. 준비성 프로브의 초기 지연 이전의 기본 상태는 <code>Failure</code>이다. 만약 컨테이너가 준비성 프로브를 지원하지 않는다면, 기본 상태는 <code>Success</code>이다.

<li><code>startupProbe</code>: 컨테이너 내의 애플리케이션이 시작되었는지를 나타낸다. 스타트업 프로브(startup probe)가 주어진 경우, 성공할 때까지 다른 나머지 프로브는 활성화되지 않는다. 만약 스타트업 프로브가 실패하면, kubelet이 컨테이너를 죽이고, 컨테이너는 <a href="https://kubernetes.io/ko/docs/concepts/workloads/pods/pod-lifecycle/#restart-policy">재시작 정책</a>에 따라 처리된다. 컨테이너에 스타트업 프로브가 없는 경우, 기본 상태는 <code>Success</code>이다

<li>failure Threashold: probe 실패시 재 실행 횟수

<li>periodSeconds: 재실행 주기 
</li>
</ul>
   </td>
  </tr>
</table>


## **Maven Dependency (ADMP 자동 주입)**



spring boot은 2.3.0 부터 k8s liveness 와 readiness probe를 지원하기 위한 기능을 제공한다. 그러나 정확하게 사용하기 위해서는 2.3.2.RELEASE 이상 버전을 권고한다. 


```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>2.3.2.RELEASE</version>
  <relativePath/>
```


**Application Property 설정 (AMDP 자동 주입)**



applicaton.yaml 설정은 아래와 같다


```yaml
management.endpoints.web.exposure.include: '*'
management.endpoint.health.show-details: always

management.health.diskspace.enabled: true
management.health.circuitbreakers.enabled: true

management.endpoint.health.probes.enabled: true
management.health.livenessState.enabled: true
management.health.readinessstate.enabled: true
```


기본적으로 management.endpoint.health.probes.enabled, management.health.livenessState.enabled, management.health.readinessState.enabled를 설정하면 되지만, 실제 readiness, liveness를 확인 시 circuit breaker, diskspace 상태 등을 함께 고려하고자 하는 경우에는 이 들을 활성화 할 필요가 있다. 

## **수동으로 Liveness & Readiness 설정 변경  (개발자 영역)**



일반적으로 actuator가 제공하는 기본적인 상태를 확인 시에는 별도 상태 변경을 수동을 할 필요가 없다

다음은 각 상태 값을 표시한다.


```
상태 표시
- Readiness 상태: CORRECT or BROKEN
- Liveness 상태: ACCEPTING_TRAFFIC or REFUSING_TRAFFIC 
으로 구분되며
정상 일때는 200 OK, 비정상 일때는 503 Return
```


그러나, Application 개발 시 상태를 수동으로 변경하고자 하는 경우에는 다음과 같이 진행하면 된다. 

ApplicationAvailability 와 ApplicationEventPublisher Bean을 주입해야 하며, 


```java
@Autowired
private ApplicationAvailability applicationAvailability;
@Autowired
private ApplicationEventPublisher eventPublisher;
```


아래와 같이 필요에 따라 상태값을 변경하면 된다.


```java
@GetMapping("/readiness/accepting")
public String markReadinesAcceptingTraffic() {
   AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);
   return applicationAvailability.getReadinessState().toString();
}

@GetMapping("/readiness/refusing")
public String markReadinesRefusingTraffic() {
   AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.REFUSING_TRAFFIC);
   return applicationAvailability.getReadinessState().toString();
}
@GetMapping("/liveness/correct")
public String markLivenessCorrect() {
   AvailabilityChangeEvent.publish(eventPublisher, this, LivenessState.CORRECT);
   return applicationAvailability.getLivenessState().toString();
}
@GetMapping("/liveness/broken")
public String markLivenessBroken() {
   AvailabilityChangeEvent.publish(eventPublisher, this, LivenessState.BROKEN);
   return applicationAvailability.getLivenessState().toString();
}
```


