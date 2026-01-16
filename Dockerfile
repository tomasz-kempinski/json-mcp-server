# Build stage
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copy gradle files for caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Grant execute permission for gradlew
RUN chmod +x gradlew

# Download dependencies (caching layer)
RUN ./gradlew dependencies --no-daemon

# Copy source and build
COPY src src
RUN ./gradlew bootJar --no-daemon

# Run stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

LABEL org.opencontainers.image.title="json-mcp-server"
LABEL org.opencontainers.image.description="MCP server for JSON files"
LABEL org.opencontainers.image.source="https://github.com/tomaszkempinski/json-mcp-server"

# Default environment variables
ENV SERVER_PORT=3000
ENV MCP_LOG_LEVEL=INFO
ENV SPRING_PROFILES_ACTIVE=default
ENV MCP_TOOLS_FILE_ALLOWED_EXTENSIONS=.json

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar from the build stage
# We use a wildcard to match the versioned jar name
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port (only relevant for web/streaming mode)
EXPOSE 3000

# Volume for mounting local JSON files
VOLUME ["/tmp"]

# Use exec form for ENTRYPOINT to allow signals (like SIGTERM) to reach the application
ENTRYPOINT ["java", "-jar", "app.jar"]
