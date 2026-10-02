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
# Convert at container start. Handles both env names and both schemes, and
# leaves already-correct jdbc: URLs untouched. Docker Compose sets
# SPRING_DATASOURCE_URL itself in jdbc: form, so this is a no-op locally.
ENTRYPOINT ["sh", "-c", "jdbc_url() { echo \"$1\" | sed -e 's#^postgres://#jdbc:postgresql://#' -e 's#^postgresql://#jdbc:postgresql://#'; }; if [ -n \"$SPRING_DATASOURCE_URL\" ]; then export SPRING_DATASOURCE_URL=$(jdbc_url \"$SPRING_DATASOURCE_URL\"); elif [ -n \"$DATABASE_URL\" ]; then export SPRING_DATASOURCE_URL=$(jdbc_url \"$DATABASE_URL\"); fi; exec java -jar app.jar"]
