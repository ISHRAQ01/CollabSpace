# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
