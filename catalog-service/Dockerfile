# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY catalog-service/pom.xml catalog-service/
# gRPC generation depends on catalog-service's src/main/proto
COPY catalog-service/src catalog-service/src
COPY auth-service/pom.xml auth-service/
COPY api-gateway/pom.xml api-gateway/

# Generate gRPC classes and package
RUN mvn clean compile -pl catalog-service -am || true
RUN chmod +x catalog-service/target/protoc-plugins/*.exe || true
RUN mvn package -DskipTests -pl catalog-service -am

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/catalog-service/target/*.jar app.jar

# REST Port
EXPOSE 8083
# gRPC Port
EXPOSE 9091

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
