FROM openjdk:17-slim

WORKDIR /app

COPY target/DiscordBotV3-0.3.0-BETA.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/DiscordBotV3-0.3.0-BETA.jar"]