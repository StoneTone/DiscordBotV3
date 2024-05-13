FROM openjdk:17-slim

WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y maven \
    && mvn clean package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/DiscordBotV3-0.3.0-BETA.jar"]