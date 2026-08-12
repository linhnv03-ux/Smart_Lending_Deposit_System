# Stage 1: Build Maven Application
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY slds-gateway-auth-service/pom.xml slds-gateway-auth-service/
COPY slds-loan-service/pom.xml slds-loan-service/
COPY slds-deposit-service/pom.xml slds-deposit-service/
COPY slds-credit-assessment-service/pom.xml slds-credit-assessment-service/
COPY slds-core-banking-adapter-service/pom.xml slds-core-banking-adapter-service/

RUN mvn dependency:go-offline -B || true

COPY slds-gateway-auth-service/src slds-gateway-auth-service/src
RUN mvn clean package -pl slds-gateway-auth-service -am -DskipTests

# Stage 2: Runtime Image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/slds-gateway-auth-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
