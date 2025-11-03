# =========================
# Stage 1: Build
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia pom.xml y descarga dependencias (cache eficiente)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el código fuente
COPY src ./src

# Compila el proyecto
RUN mvn -DskipTests clean package

# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:17-jdk
WORKDIR /home/app

# Variable de entorno para el perfil de Spring
ENV SPRING_PROFILES_ACTIVE=dev

# Copia solo el .jar ya compilado
COPY --from=build /app/target/*.jar app.jar

# Puerto que usará la app
EXPOSE 8900

# Comando de arranque
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-Dserver.port=${PORT}", "-jar", "app.jar"]