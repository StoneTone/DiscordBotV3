FROM openjdk:17-slim

WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y maven \
    && mvn clean package

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/DiscordBotV3-0.2.3-BETA.jar"]