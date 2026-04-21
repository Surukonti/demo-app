# Stage 1: build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY demo .
RUN mvn clean package -DskipTests

# Stage 2: run
FROM eclipse-temurin:21-jdk-jammy
RUN apt-get update && apt-get install -y curl
WORKDIR /app
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["sh", "-c", "echo MONGO_URI=$SPRING_DATA_MONGODB_URI && java -jar app.jar --spring.data.mongodb.uri=$SPRING_DATA_MONGODB_URI"]
HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
 CMD curl -f http://localhost:8081/actuator/health || exit 1