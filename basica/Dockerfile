FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S -G app app
COPY --from=build /build/target/basica-0.0.1-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=3s --start-period=40s --retries=3 \
	CMD wget -q -O /dev/null http://127.0.0.1:8080/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
