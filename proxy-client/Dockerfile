
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app


COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw.cmd ./


COPY proxy-client ./proxy-client

RUN ./mvnw -q -f proxy-client/pom.xml -DskipTests -Dmaven.test.skip=true clean package

FROM eclipse-temurin:17-jdk
WORKDIR /home/app
COPY --from=build /app/proxy-client/target/*.jar app.jar
EXPOSE 8900
ENTRYPOINT ["java","-jar","app.jar"]