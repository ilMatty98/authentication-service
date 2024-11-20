## Stage 1: Build application using Maven and Java 21
# Use an image with Maven and Java 21 installed for building the application
FROM eclipse-temurin:21-jdk AS build

# Set the working directory inside the container
WORKDIR /code

# Copy the Maven Wrapper script (`mvnw`) and set permissions
COPY mvnw /code/mvnw
COPY .mvn /code/.mvn
COPY pom.xml /code/

# Make `mvnw` executable
RUN chmod +x /code/mvnw

# Preload dependencies to speed up build process
RUN ./mvnw dependency:go-offline -B

# Copy the source code to the working directory
COPY src /code/src

# Package the application
RUN ./mvnw clean package -Dquarkus.package.type=uber-jar

## Stage 2: Run the application using a lightweight Java 21 runtime image
# Use a lightweight runtime image with Java 21
FROM eclipse-temurin:21-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the packaged JAR file from the build stage
COPY --from=build /code/target/*.jar /app/application.jar

# Expose the default HTTP port used by Quarkus
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "/app/application.jar", "-Dquarkus.http.host=0.0.0.0"]