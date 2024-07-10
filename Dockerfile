FROM openjdk:17-slim

WORKDIR /app

COPY target/DiscordBotV3-0.5.2-BETA.jar app.jar

COPY libs/* /app/libs/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]