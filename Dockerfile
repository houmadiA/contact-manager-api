FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline -DskipTests

COPY src src

RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup --system contactmanager && adduser --system --ingroup contactmanager contactmanager
USER contactmanager

COPY --from=build /build/target/contact-manager.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
