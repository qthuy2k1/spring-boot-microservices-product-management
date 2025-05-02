# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Set the working directory to the root project
WORKDIR /app

# Copy the entire project context
COPY . /app/

# Navigate to the payment-service subdirectory for the build
WORKDIR /app/payment-service

# Package the application
RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: Create a smaller image with the JAR file
FROM eclipse-temurin:21-jre-alpine

# Install additional utilities via APK
RUN apk add --no-cache curl bash

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/payment-service/target/payment-service-1.0-SNAPSHOT.jar payment-service-1.0-SNAPSHOT.jar

# Expose the application port
EXPOSE 9093

# Run the application
ENTRYPOINT ["java", "-jar", "payment-service-1.0-SNAPSHOT.jar"]
