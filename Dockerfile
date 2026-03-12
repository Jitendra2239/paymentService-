# ===== Stage 1: Build the application =====
FROM gradle:8.7-jdk17 AS build

# Set working directory
WORKDIR /app

# Copy Gradle files first (for dependency caching)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the JAR
RUN ./gradlew clean build -x test --no-daemon


# ===== Stage 2: Run the application =====
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8083

# Run application
ENTRYPOINT ["java","-jar","app.jar"]