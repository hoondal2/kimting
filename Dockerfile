# Stage 1: Frontend build
FROM node:20-alpine AS frontend-build
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci --ignore-scripts
COPY frontend/ ./
RUN npm run build

# Stage 2: Backend build
FROM eclipse-temurin:21-jdk-jammy AS backend-build
WORKDIR /app
COPY . .
COPY --from=frontend-build /app/dist src/main/resources/static
RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=backend-build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
