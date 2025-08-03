FROM openjdk:17-slim

WORKDIR /app

COPY target/DiscordBotV3-1.0.4-BETA.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]