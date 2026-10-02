# Stage 1: build my jar with Maven (so I don't need Maven installed locally)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# Stage 2: run only my jar on a small Java runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Render injects DATABASE_URL as postgres://... but JDBC needs jdbc:postgresql://...
# Convert it at container start when SPRING_DATASOURCE_URL is not set directly.
# Docker Compose sets SPRING_DATASOURCE_URL itself, so this step is skipped locally.
ENTRYPOINT ["sh", "-c", "if [ -n \"$DATABASE_URL\" ] && [ -z \"$SPRING_DATASOURCE_URL\" ]; then export SPRING_DATASOURCE_URL=$(echo \"$DATABASE_URL\" | sed 's#^postgres://#jdbc:postgresql://#'); fi; exec java -jar app.jar"]
