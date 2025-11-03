# =========================
# Stage 1: Build
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia solo el pom del microservicio
COPY pom.xml .
# Descarga dependencias de este módulo
RUN mvn dependency:go-offline -B

# Copia el código fuente del microservicio
COPY src ./src

# Compila solo este microservicio
RUN mvn -DskipTests clean package

# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:17-jdk
WORKDIR /home/app

ENV SPRING_PROFILES_ACTIVE=dev

# Copia el jar generado
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8900
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-Dserver.port=${PORT}", "-jar", "app.jar"]