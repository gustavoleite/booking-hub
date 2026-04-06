FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml
COPY review-service/pom.xml review-service/pom.xml
COPY search-service/pom.xml search-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN mvn -pl search-service dependency:go-offline -B -q

COPY search-service/src search-service/src
RUN mvn -pl search-service package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /workspace/search-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
