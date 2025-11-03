# ===== Etapa 1: Build con Maven =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia el POM raíz (importante para parent)
COPY ../pom.xml .
COPY ../mvnw ../mvnw.cmd ./
COPY ../.mvn .mvn

# Copia solo este módulo
COPY . .

# Compila
RUN mvn -DskipTests clean package

# ===== Etapa 2 =====
FROM eclipse-temurin:17-jdk
WORKDIR /home/app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8900
ENTRYPOINT ["java", "-jar", "app.jar"]