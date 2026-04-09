FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copy all pom.xml files so the multi-module parent resolves correctly
COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY search-service/pom.xml search-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
RUN mvn -pl auth-service dependency:go-offline -B -q

COPY auth-service/src auth-service/src
RUN mvn -pl auth-service package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /workspace/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
