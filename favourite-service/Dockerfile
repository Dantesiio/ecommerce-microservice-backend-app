
FROM openjdk:11
ARG PROJECT_VERSION=0.1.0
ENV SPRING_PROFILES_ACTIVE=dev
RUN mkdir -p /home/app
WORKDIR /home/app
COPY favourite-service/target/favourite-service-v${PROJECT_VERSION}.jar favourite-service.jar
EXPOSE 8800
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "favourite-service.jar"]


