# ─── Stage 1: Build the JAR ────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy dependency descriptors first so Docker caches this layer separately
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build (skip tests for faster image builds)
COPY src ./src
RUN mvn clean package -DskipTests -q

# ─── Stage 2: Minimal runtime image ────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S disasterlink && adduser -S disasterlink -G disasterlink
USER disasterlink

COPY --from=build /app/target/disasterlink-backend-*.jar app.jar

EXPOSE 8080

# FIX L7: JVM memory tuning flags added.
# Without these, the JVM defaults to using 25% of host RAM for heap, which
# can cause OOM kills in constrained containers.
# -XX:MaxRAMPercentage=75.0  → use up to 75% of container's memory limit for heap
# -XX:+UseG1GC               → G1 garbage collector (optimal for latency + throughput)
# -XX:+ExitOnOutOfMemoryError → fail fast on OOM rather than limping in a broken state
# Use the exec form so signals (SIGTERM from docker stop) reach the JVM
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "app.jar"]