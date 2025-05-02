# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Set the working directory to the root project
WORKDIR /app

# Copy the entire project context
COPY . /app/

# Navigate to the api-gateway subdirectory for the build
WORKDIR /app/api-gateway

# Package the application
RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: Create a smaller image with the JAR file
FROM eclipse-temurin:21-jre-alpine

# Install additional utilities via APK
RUN apk add --no-cache curl bash

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/api-gateway/target/api-gateway-1.0-SNAPSHOT.jar api-gateway-1.0-SNAPSHOT.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "api-gateway-1.0-SNAPSHOT.jar"]
