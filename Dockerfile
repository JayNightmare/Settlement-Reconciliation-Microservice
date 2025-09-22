# syntax=docker/dockerfile:1

FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /workspace/target/settlement-reconciliation-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
