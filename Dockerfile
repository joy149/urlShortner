# Importing JDK and copying required files
FROM openjdk:17-jdk AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle gradle
COPY src src

# Set execution permission for the Gradle wrapper
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# Stage 2: Create the final Docker image using OpenJDK 17
FROM openjdk:17-jdk
VOLUME /tmp

# Copy the JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
