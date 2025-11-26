
FROM eclipse-temurin:11-jdk
ARG PROJECT_VERSION=0.1.0
ENV SPRING_PROFILES_ACTIVE=dev
RUN mkdir -p /home/app
WORKDIR /home/app
COPY user-service/target/*.jar user-service.jar
EXPOSE 8700
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "user-service.jar"]


