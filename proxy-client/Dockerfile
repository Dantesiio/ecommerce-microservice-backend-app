# ===== Etapa 1: Build con Maven =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos el parent POM y el wrapper de Maven
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn

# Copiamos solo el módulo proxy-client (código fuente + su pom)
COPY proxy-client ./proxy-client

# Compilamos el módulo proxy-client
RUN mvn -f proxy-client/pom.xml -DskipTests clean package

# ===== Etapa 2: Imagen final =====
FROM eclipse-temurin:17-jdk
WORKDIR /home/app

# Copiamos el JAR generado desde la etapa de build
COPY --from=build /app/proxy-client/target/*.jar app.jar

EXPOSE 8900
ENTRYPOINT ["java", "-jar", "app.jar"]