# Etapa 1: Compilación con Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos solo los archivos necesarios
COPY ../pom.xml .
COPY ../.mvn .mvn
COPY ../mvnw .
COPY ../mvnw.cmd .
COPY . ./proxy-client

# Compilamos el proyecto dentro del contenedor
RUN mvn -f proxy-client/pom.xml -DskipTests clean package

# Etapa 2: Imagen final
FROM eclipse-temurin:17-jdk
WORKDIR /home/app

# Copiamos el .jar compilado desde la etapa anterior
COPY --from=build /app/proxy-client/target/*.jar app.jar

EXPOSE 8900
ENTRYPOINT ["java", "-jar", "app.jar"]