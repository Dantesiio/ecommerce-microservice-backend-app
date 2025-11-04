# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -DskipTests clean package

# Etapa 2: Runtime
FROM eclipse-temurin:17-jdk
WORKDIR /home/app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8900
ENTRYPOINT ["java", "-jar", "app.jar"]