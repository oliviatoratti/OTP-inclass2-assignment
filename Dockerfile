FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B clean verify

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV LANG=C.UTF-8

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]