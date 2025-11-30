FROM eclipse-temurin:11-jre

WORKDIR /home/app
ENV SPRING_PROFILES_ACTIVE=dev
ARG JAR_FILE=api-gateway/target/*.jar
COPY ${JAR_FILE} api-gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "api-gateway.jar"]
