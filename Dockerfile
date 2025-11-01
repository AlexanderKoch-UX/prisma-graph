# Build stage für Prisma Graph Visualizer Plugin
FROM eclipse-temurin:17-jdk-jammy

# Install required tools and Gradle 8.5
RUN apt-get update && apt-get install -y \
    unzip \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Download and install Gradle 8.5
RUN mkdir -p /opt/gradle && \
    wget https://services.gradle.org/distributions/gradle-8.5-bin.zip -O /tmp/gradle-8.5-bin.zip && \
    unzip -q /tmp/gradle-8.5-bin.zip -d /opt/gradle && \
    rm /tmp/gradle-8.5-bin.zip

ENV PATH="/opt/gradle/gradle-8.5/bin:$PATH"

WORKDIR /app

# Copy project files (everything except what's in .dockerignore)
COPY . .

# Build the plugin distribution
RUN gradle clean buildPlugin

# Verify the build
RUN ls -lh build/distributions/ || echo "Build directories not found"