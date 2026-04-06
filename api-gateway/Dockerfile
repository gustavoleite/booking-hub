FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY review-service/pom.xml review-service/pom.xml
COPY search-service/pom.xml search-service/pom.xml
RUN mvn -pl api-gateway dependency:go-offline -B -q

COPY api-gateway/src api-gateway/src
RUN mvn -pl api-gateway package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /workspace/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
