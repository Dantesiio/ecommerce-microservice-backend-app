FROM eclipse-temurin:17-jdk
WORKDIR /home/app
COPY proxy-client/target/*.jar app.jar
EXPOSE 8900
ENTRYPOINT ["java", "-jar", "app.jar"]