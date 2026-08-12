# Multi-stage Docker build for Java 17 Spring Boot Microservices
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy parent pom.xml and source files
COPY pom.xml .
COPY slds-gateway-auth-service ./slds-gateway-auth-service
COPY slds-loan-service ./slds-loan-service

ARG SERVICE_NAME=slds-loan-service
RUN mvn clean package -pl ${SERVICE_NAME} -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ARG SERVICE_NAME=slds-loan-service
COPY --from=builder /app/${SERVICE_NAME}/target/*.jar app.jar

EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
