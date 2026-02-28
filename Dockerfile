# Importing JDK and copying required files
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle gradle
COPY src src

# Set execution permission for the Gradle wrapper
# Copy gradle wrapper properties
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties

# Build with no-daemon for Docker

RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon build
# Stage 2: Create the final Docker image using Java 17 runtime
FROM eclipse-temurin:17-jre-jammy
VOLUME /tmp

# Copy the JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
