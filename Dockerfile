# Fiji CLI Agent - Docker Image with Xvfb
# Ubuntu 24.04 + OpenJDK 17 + Fiji 2.15.1 + Xvfb

FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=UTC

# Install dependencies
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    xvfb \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Download Fiji 2.15.1 (locked version)
ENV FIJI_VERSION=2.15.1
ENV FIJI_HOME=/opt/fiji/Fiji.app

RUN wget -q https://downloads.imagej.net/fiji/archive/${FIJI_VERSION}/fiji-linux64.zip \
    && unzip -q fiji-linux64.zip -d /opt \
    && rm fiji-linux64.zip \
    && chmod +x ${FIJI_HOME}/ImageJ-linux64 \
    && ln -sf ${FIJI_HOME}/ImageJ-linux64 /usr/local/bin/fiji

ENV PATH=${PATH}:${FIJI_HOME}

# Create app directory
WORKDIR /app

# Copy application source
COPY src /app/src
COPY test_cells.tif /app/test_cells.tif

# Compile Java application
RUN mkdir -p bin \
    && javac -d bin src/main/java/fiji/agent/*.java

# Set environment for Xvfb
ENV DISPLAY=:99
ENV XVFB_SCREEN="0 1024x768x24"

# Default command: show versions and help
CMD ["java", "-cp", "bin", "fiji.agent.FijiAgent", "--help"]
