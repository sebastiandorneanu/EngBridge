FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY backend/auth-contract /app/backend/auth-contract
WORKDIR /app/backend/auth-contract
RUN mvn install -DskipTests

COPY backend/api-gateway /app/backend/api-gateway
WORKDIR /app/backend/api-gateway
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/api-gateway/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
