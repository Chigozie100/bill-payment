FROM openjdk:11-jre-slim
EXPOSE 80
ADD target/billspaymentservice.jar billspaymentservice.jar
ENTRYPOINT ["java","-jar", "/billspaymentservice.jar"]
