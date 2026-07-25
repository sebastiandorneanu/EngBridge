FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY backend/content-service /app/backend/content-service
WORKDIR /app/backend/content-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/content-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
