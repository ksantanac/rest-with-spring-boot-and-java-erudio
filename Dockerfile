FROM eclipse-temurin:23
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]