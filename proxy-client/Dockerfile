# Etapa 1: Build con Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia todo el proyecto (para incluir el parent pom)
COPY . .

# Entra al módulo y empaqueta
WORKDIR /app/proxy-client
RUN mvn -DskipTests clean package

# Etapa 2: Imagen final
FROM eclipse-temurin:17-jdk
WORKDIR /home/app
COPY --from=build /app/proxy-client/target/*.jar app.jar
EXPOSE 8900
ENTRYPOINT ["java", "-jar", "app.jar"]