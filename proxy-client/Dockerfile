# ===== Etapa 1: Build con Maven =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos el POM raíz y el módulo proxy-client para que Maven encuentre el parent
COPY pom.xml .
COPY proxy-client ./proxy-client

# Copiamos el wrapper de Maven y la carpeta .mvn (necesarios para builds limpios)
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn

# Compilamos el módulo proxy-client usando el pom del submódulo
RUN mvn -f proxy-client/pom.xml -DskipTests clean package

# ===== Etapa 2: Imagen final =====
FROM eclipse-temurin:17-jdk
WORKDIR /home/app
COPY --from=build /app/proxy-client/target/*.jar app.jar
EXPOSE 8900
ENTRYPOINT ["java", "-jar", "app.jar"]