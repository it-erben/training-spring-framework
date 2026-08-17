# Schlankes Laufzeit-Image: nur die JRE, kein JDK, kein Maven.
# Voraussetzung: das Fat-JAR liegt bereits unter target/ —
# vorher also `mvn package` ausführen.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/sb-basics-operations-demo-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
