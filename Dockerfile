FROM openjdk:17-slim

WORKDIR /app

COPY target/DiscordBotV3-0.4.3-BETA.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]