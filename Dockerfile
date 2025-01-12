FROM openjdk:17-slim

WORKDIR /app

COPY target/DiscordBotV3-0.6.0-BETA.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]