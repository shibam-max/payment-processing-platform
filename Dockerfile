# Multi-stage build for Payment Processing Platform
FROM openjdk:17-jdk-slim as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jre-slim

# Create non-root user for security
RUN groupadd -r payment && useradd -r -g payment payment

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar payment-processing-platform.jar

# Create logs directory
RUN mkdir -p logs && chown -R payment:payment /app

# Switch to non-root user
USER payment

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/payments/health || exit 1

# Expose port
EXPOSE 8080

# JVM optimization for containers
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar payment-processing-platform.jar"]