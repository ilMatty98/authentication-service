# Set the builder
FROM ghcr.io/graalvm/graalvm-community:21.0.2-ol9-20240116 AS builder

# Install Maven
RUN microdnf install -y wget tar gzip \
    && wget https://archive.apache.org/dist/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz \
    && tar xzvf apache-maven-3.8.8-bin.tar.gz -C /opt \
    && ln -s /opt/apache-maven-3.8.8/bin/mvn /usr/bin/mvn

# Set the working directory inside the container
WORKDIR /app

# Copy the project to download the dependencies
COPY . .

# Package the application
RUN mvn package -DskipTests

# Expose the port the application will run on
EXPOSE 8080

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]