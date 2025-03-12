FROM amazoncorretto:21 AS build
COPY build.gradle /app/
COPY gradlew /app/
COPY gradlew.bat /app/
COPY gradle/wrapper/gradle-wrapper.jar /app/gradle/wrapper/
COPY gradle/wrapper/gradle-wrapper.properties /app/gradle/wrapper/
COPY src /app/src
WORKDIR /app
RUN ./gradlew clean build

FROM amazoncorretto:21-al2023-headful
COPY --from=build /app/build/libs/webshop.jar /app/webshop.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/webshop.jar"]