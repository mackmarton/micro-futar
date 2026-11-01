# Stage 1: Build the Jar
FROM maven:3.9.11-eclipse-temurin-25 AS builder
WORKDIR /build

# Copy and install the shared library first
COPY shared ./shared
WORKDIR /build/shared
RUN mvn clean install -DskipTests

# Now build the payments service
WORKDIR /build/payments
COPY payments ./
RUN mvn clean package -DskipTests

# Stage 2: Run the Jar
FROM eclipse-temurin:25-jdk-jammy
WORKDIR /app
COPY --from=builder /build/payments/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
