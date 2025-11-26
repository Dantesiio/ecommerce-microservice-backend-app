
FROM eclipse-temurin:11-jdk
ARG PROJECT_VERSION=0.1.0
ENV SPRING_PROFILES_ACTIVE=dev
RUN mkdir -p /home/app
WORKDIR /home/app
COPY payment-service/target/payment-service-v${PROJECT_VERSION}.jar payment-service.jar
EXPOSE 8400
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "payment-service.jar"]


