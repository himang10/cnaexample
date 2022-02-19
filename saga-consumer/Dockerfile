FROM openjdk:11-jre-slim
VOLUME /tmp
ADD target/saga-consumer-0.0.1-SNAPSHOT.jar app.jar
ADD lib /cnadata-lib
ENV JAVA_OPTS=""
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Djava.library.path=/cnadata-lib","-jar","/app.jar"]
