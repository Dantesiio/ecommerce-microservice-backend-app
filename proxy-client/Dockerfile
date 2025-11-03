# =========================
# Stage 1: Build
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia el pom del root y el del módulo
COPY pom.xml .
COPY proxy-client/pom.xml proxy-client/pom.xml

# Copia los archivos necesarios para resolver dependencias
COPY proxy-client/src proxy-client/src

# Descarga dependencias y construye el JAR del submódulo
RUN mvn -f proxy-client/pom.xml -DskipTests clean package

# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:17-jdk
WORKDIR /home/app

ENV SPRING_PROFILES_ACTIVE=dev

# Copia el jar compilado desde la etapa anterior
COPY --from=build /app/proxy-client/target/*.jar app.jar

EXPOSE 8900
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-Dserver.port=${PORT}", "-jar", "app.jar"]