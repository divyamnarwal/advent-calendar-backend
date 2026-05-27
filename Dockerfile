### Build stage — compile and package the Spring Boot jar
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Cache the local Maven repo across builds via a BuildKit cache mount,
# so dependencies aren't re-downloaded every time.
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -DskipTests

### Runtime stage — slim JRE running the fat jar
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as a non-root user
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/*.jar app.jar
USER app

EXPOSE 8081
# Size the heap from the container's cgroup memory limit (default is only 25%,
# wasteful under our 1g mem_limit). On OOM, exit so Docker's restart policy
# recycles the container rather than leaving it wedged.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
