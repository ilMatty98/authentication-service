## Stage 1: with Quarkus image for native compilation
# This image includes GraalVM for compiling the application into a native binary
# https://quay.io/repository/quarkus/ubi-quarkus-native-image
FROM quay.io/quarkus/ubi-quarkus-native-image:22.3-java17 AS build

# Copy the Maven Wrapper script (`mvnw`) to the working directory and set permissions for the quarkus user
COPY --chown=quarkus:quarkus mvnw /code/mvnw
# Copy the .mvn folder required for Maven wrapper
COPY --chown=quarkus:quarkus .mvn /code/.mvn
# Copy the pom.xml file to specify dependencies
COPY --chown=quarkus:quarkus pom.xml /code/

# Set the user to quarkus for security purposes
USER quarkus
# Set the working directory to /code
WORKDIR /code

# List details of the mvnw file to check for any permission issues
RUN ls -latr ./mvnw
# Make mvnw executable to allow Maven commands to run inside the container
RUN chmod u+x ./mvnw
# Download all project dependencies in offline mode to speed up build times
RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline
# Check mvnw file again to ensure it's set up correctly
RUN ls -latr ./mvnw

# Copy the application source code to the working directory
COPY src /code/src

# Build the application in native mode, skipping tests and allocating 7GB of memory for the native build
RUN ./mvnw package -Pnative

## Stage 2 : create the docker final image
# Use a lightweight Quarkus image to run the native application
FROM quay.io/quarkus/quarkus-micro-image:1.0

# Set the working directory for the application binary
WORKDIR /work/

# Copy the native application binary from the build stage
COPY --from=build /code/target/*-runner /work/application

# Set permissions for user '1001' and working directory
RUN chmod 775 /work /work/application \
  && chown -R 1001 /work \
  && chmod -R "g+rwX" /work \
  && chown -R 1001:root /work

# Expose port 8080 for accessing the application
EXPOSE 8080

# Run the application as user 1001 instead of root for security purposes
USER 1001

# Start the native application and configure it to listen on all network interfaces
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]