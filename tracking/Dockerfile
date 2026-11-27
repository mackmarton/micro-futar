# Stage 1: Build the Jar
FROM maven:3.9.11-eclipse-temurin-25 AS builder
WORKDIR /build

COPY shared ./shared
WORKDIR /build/shared
RUN mvn clean install -DskipTests

WORKDIR /build/tracking
COPY tracking/pom.xml ./
RUN mvn dependency:go-offline --batch-mode
COPY tracking ./
RUN mvn clean package -DskipTests

# Stage 2: Run the Jar
FROM eclipse-temurin:25-jre-jammy
RUN groupadd --system --gid 1000 app \
 && useradd --system --uid 1000 --gid 1000 --no-create-home \
    --home-dir /nonexistent --shell /usr/sbin/nologin app
WORKDIR /app
RUN chown 1000:1000 /app
COPY --from=builder --chown=1000:1000 /build/tracking/target/*.jar app.jar
USER 1000:1000
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
