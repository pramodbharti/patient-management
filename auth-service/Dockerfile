FROM gradle:8.8-jdk21 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN ./gradlew dependencies --write-locks --no-daemon

# Copy application source
COPY src ./src

# Build the application
RUN ./gradlew clean build --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy built artifact
COPY --from=builder /app/build/libs/auth-service-*.jar ./app.jar

# Production JVM options
#ENV JAVA_OPTS="-XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 4005

ENTRYPOINT ["java", "-jar", "app.jar"]