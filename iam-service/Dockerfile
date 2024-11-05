FROM gradle:8.8.0-jdk21 AS build

WORKDIR /app

# copy root build files and settings
COPY ../gradlew /app/
COPY ../gradlew.bat /app/
COPY ../gradle /app/gradle/
COPY ../build.gradle /app/
COPY ../settings.gradle /app/


COPY ../iam-service/src /app/iam-service/src
COPY ../iam-service/build.gradle /app/iam-service/
COPY ../dictionary-service/build.gradle /app/dictionary-service/
COPY ../dictionary-service/src /app/dictionary-service/src
COPY ../security-module/build.gradle /app/security-module/
COPY ../security-module/src /app/security-module/src

# grant permissions for the gradle project
RUN chmod +x gradlew

# build the entire multi-module project
RUN ./gradlew :iam-service:build --no-daemon


# image for JDK
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=build /app/iam-service/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
