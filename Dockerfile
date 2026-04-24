# --- Build Stage ---
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Grant execution permissions for the wrapper
RUN chmod +x mvnw

# Download dependencies (this layer will be cached as long as pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code and build the application
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# --- Run Stage ---
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Create a non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Install curl for healthcheck
USER root
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy default configuration and manifests (now including world)
COPY config/ config-defaults/

# Create the actual config directory
RUN mkdir -p config && chown -R spring:spring /app/config /app/config-defaults

# Copy and prepare entrypoint script
COPY entrypoint.sh .
RUN sed -i 's/\r$//' entrypoint.sh && \
    chmod +x entrypoint.sh && \
    chown spring:spring entrypoint.sh

USER spring:spring

# Expose the default Spring Boot port (can be overridden by SERVER_PORT env)
EXPOSE 8080

# Configure healthcheck (overridden by docker-compose)
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

# Run the application via entrypoint script
ENTRYPOINT ["./entrypoint.sh"]
