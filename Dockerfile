FROM maven:3.8.6-openjdk-17-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-slim
ARG MAVEN_REPO=/root/.m2/repository
WORKDIR /app
COPY --from=build /app/target/DiscordBotV3-0.3.0-BETA.jar .
COPY --from=build $MAVEN_REPO $MAVEN_REPO

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "DiscordBotV3-0.3.0-BETA.jar"]