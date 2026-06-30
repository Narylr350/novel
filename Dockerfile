# Frontend build
FROM node:20-alpine AS frontend-builder

WORKDIR /frontend

COPY free-novel-web/package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

COPY free-novel-web/ ./

RUN npm run build

# Backend build
FROM maven:3.9-eclipse-temurin-21 AS backend-builder

WORKDIR /app

COPY novel/pom.xml ./
RUN mvn dependency:go-offline -B

COPY novel/src ./src

RUN mkdir -p src/main/resources/static
COPY --from=frontend-builder /frontend/dist/ ./src/main/resources/static/

RUN mvn clean package -DskipTests -Pdev

FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=backend-builder /app/target/*.jar app.jar

RUN mkdir -p /app/logs && \
    mkdir -p /app/tmp && \
    mkdir -p /app/file

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

ENV TZ=Asia/Shanghai

ENV JAVA_OPTS="-Xms512m -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
