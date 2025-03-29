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
RUN mkdir -p /app/uploads && chmod -R 777 /app/uploads
COPY --from=build /app/build/libs/webshop.jar /app/webshop.jar
WORKDIR /app
EXPOSE 8888
ENV UPLOAD_DIR=/app/uploads/
ENTRYPOINT ["java", "-jar", "/app/webshop.jar"]