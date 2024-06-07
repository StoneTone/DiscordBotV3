FROM openjdk:17-slim

WORKDIR /app

COPY target/DiscordBotV3-0.5.0-BETA.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]