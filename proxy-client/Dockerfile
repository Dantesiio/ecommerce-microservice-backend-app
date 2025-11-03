# ===== Etapa 1: Build con Maven =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos todo el proyecto para que Maven tenga el pom padre y los módulos
COPY . /app

# Compilamos el módulo proxy-client usando su pom.xml
RUN mvn -f proxy-client/pom.xml -DskipTests clean package

# ===== Etapa 2: Imagen final =====
FROM eclipse-temurin:17-jdk
WORKDIR /home/app

# Copiamos el .jar generado desde la etapa de build
COPY --from=build /app/proxy-client/target/*.jar app.jar

EXPOSE 8900
ENTRYPOINT ["java", "-jar", "app.jar"]