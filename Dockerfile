FROM eclipse-temurin:21-jre
RUN useradd -r -u 10001 -g root appuser
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} /app.jar
USER 10001
ENTRYPOINT ["java", "-jar", "/app.jar"]