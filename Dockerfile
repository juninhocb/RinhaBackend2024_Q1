FROM openjdk:21

WORKDIR /app

COPY target/rbe2024q1-0.0.1-SNAPSHOT.jar /app/simple-mvc-java.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "simple-mvc-java.jar"]