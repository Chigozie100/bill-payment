FROM openjdk:11-jre-slim
EXPOSE 8181
ADD target/billspaymentservice.jar billspaymentservice.jar
ENTRYPOINT ["java","-Dspring.profiles.active=test", "-jar", "/billspaymentservice.jar"]
