이 코드는 Saga Orchestrator인 Order Service를 위한 샘플코드이다.

이것에 대한 사용 방법은 다음 링크를 참조하면 된다.

[분산 트랜잭션 보장을 위한 Saga 개발 가이드](../docs/SAGA.README.md)

```
❈ 고려 사항

여기서 제공하는 구조는 Kafka/Kafka Connect with Debezium을 이용하여 아키텍처를 구성하고 있으며, 
spring boot 기반으로 Saga Orchestrator와 Participant를 구성을 쉽게 하기 위해 
자체 개발된 CNAData Framework의 “cnadata-producer-lib.jar”와 “cnadata-consumer-lib.jar”를 활용하였다. 
개념에 대한 이해를 위해 활용하고 있으며, 실제 프로젝트 수행 시 다른 프레임워크 또는 언어 등의 환경 구성을 하고자 하는 경우  
여기서 정의하고 있는 구조에 따라 자체적으로 개발하여 구성할 필요가 있다
```