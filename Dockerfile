# Use official OpenJDK runtime as base image
FROM openjdk:17-jre-slim

# Set working directory inside container
WORKDIR /app

# Copy the JAR file from target directory to container
COPY target/*.jar app.jar

# Expose port 8080 (default Spring Boot port)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]