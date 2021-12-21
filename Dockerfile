FROM openjdk:11-jre-slim
EXPOSE 80
ADD target/billspaymentservice.jar billspaymentservice.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=staging", "-jar", "/billspaymentservice.jar"]
