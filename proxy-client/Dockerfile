# =========================
# Stage 1: Build
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia solo el pom del microservicio
COPY proxy-client/pom.xml .

# Descarga dependencias de este microservicio
RUN mvn dependency:go-offline -B

# Copia el código fuente del microservicio
COPY proxy-client/src ./src

# Construye el JAR solo de este microservicio
RUN mvn -DskipTests clean package

# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:17-jdk
WORKDIR /home/app

# Variable de entorno del perfil
ENV SPRING_PROFILES_ACTIVE=dev

# Copia el jar construido desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto (Railway usa $PORT internamente)
EXPOSE 8900

ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-Dserver.port=${PORT}", "-jar", "app.jar"]