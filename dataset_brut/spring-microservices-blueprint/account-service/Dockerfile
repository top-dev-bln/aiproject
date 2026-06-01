FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy only pom files first to leverage Docker layer caching
COPY pom.xml ./
COPY commons/pom.xml commons/pom.xml
COPY account-service/pom.xml account-service/pom.xml
COPY product-service/pom.xml product-service/pom.xml

RUN mvn -q -DskipTests dependency:go-offline

# Copy source and build
COPY . .
RUN mvn -q -pl account-service -am -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Install curl for health checks
RUN apk add --no-cache curl

COPY --from=build --chown=appuser:appgroup /app/account-service/target/*.jar app.jar

USER appuser

EXPOSE 8088 5005

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8088/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", "/app/app.jar"]