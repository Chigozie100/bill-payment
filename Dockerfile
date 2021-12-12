FROM openjdk:11-jre-slim
EXPOSE 8181
ADD target/billspaymentservice.jar billspaymentservice.jar
ENTRYPOINT ["java","-Dspring.profiles.active=dev", "-jar", "/billspaymentservice.jar"]
