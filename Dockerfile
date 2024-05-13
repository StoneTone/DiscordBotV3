FROM openjdk:17-slim

ARG MAVEN_REPO

COPY --from=maven-repo $MAVEN_REPO /root/.m2/repository

WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y maven \
    && mvn --global-settings=/root/.m2/settings.xml clean package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/DiscordBotV3-0.3.0-BETA.jar"]