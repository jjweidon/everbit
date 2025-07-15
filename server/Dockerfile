# server/Dockerfile
# Build stage
FROM amazoncorretto:17-alpine AS builder

WORKDIR /app

# Copy Gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Grant execute permission for gradlew
RUN chmod +x ./gradlew

# Build application
RUN ./gradlew clean bootJar --no-daemon

# Runtime stage
FROM amazoncorretto:17-alpine

WORKDIR /app

# Copy built artifact from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Add wait-for-it script for database connection check
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh .
RUN chmod +x wait-for-it.sh

# Environment variables for JVM tuning
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8080

# Start application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
