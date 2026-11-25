# 1. Build the React page
FROM node:22-alpine AS web
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
# vite writes straight into ../src/main/resources/static
RUN npm run build

# 2. Build the Spring Boot jar with the page inside it
FROM maven:3.9-eclipse-temurin-21 AS jar
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
COPY --from=web /app/src/main/resources/static ./src/main/resources/static
RUN mvn -q -B -DskipTests package

# 3. Run it on a small Java image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=jar /app/target/ats-checker-*.jar app.jar
USER app
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
