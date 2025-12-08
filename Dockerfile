# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-23 AS builder

WORKDIR /build

# Copy the project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:23-jre

WORKDIR /app

# Install necessary libraries for JavaFX
RUN apt-get update && apt-get install -y \
    libgl1-mesa-glx \
    libxrender1 \
    libxrandr2 \
    libxi6 \
    && rm -rf /var/lib/apt/lists/*

# Copy the built application from builder stage
COPY --from=builder /build/target/*.jar ./app.jar

# Expose the port if needed (adjust based on your application)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

