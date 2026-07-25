FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY backend/quiz-service /app/backend/quiz-service
WORKDIR /app/backend/quiz-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/quiz-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
