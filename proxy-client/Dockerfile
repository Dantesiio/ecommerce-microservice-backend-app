# ===== Etapa 1: Build con Maven =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos los archivos necesarios para que Maven encuentre el POM padre
COPY ../pom.xml .
COPY ../mvnw ../mvnw.cmd ./
COPY ../.mvn .mvn

# Copiamos el código fuente del proxy-client
COPY . .

# Compilamos
RUN mvn -DskipTests clean package

# ===== Etapa 2: Imagen final =====
FROM eclipse-temurin:17-jdk
WORKDIR /home/app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8900
ENTRYPOINT ["java", "-jar", "app.jar"]