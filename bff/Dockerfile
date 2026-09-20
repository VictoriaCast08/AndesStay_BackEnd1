FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/*.jar app.jar
RUN useradd -m springuser && chown -R springuser /app
USER springuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
