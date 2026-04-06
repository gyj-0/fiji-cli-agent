#!/bin/bash
# Docker Build Script for Fiji CLI Agent
# Usage: ./docker-build.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "======================================"
echo "  Fiji CLI Agent - Docker Build"
echo "======================================"

# Check Docker
echo "[1/4] Checking Docker..."
if ! command -v docker &> /dev/null; then
    echo "[ERROR] Docker not found. Please install Docker."
    exit 1
fi

docker --version

# Build image
echo ""
echo "[2/4] Building Docker image..."
docker build -t fiji-cli-agent:latest -t fiji-cli-agent:2.15.1 .

if [ $? -ne 0 ]; then
    echo "[ERROR] Docker build failed"
    exit 1
fi

echo "[SUCCESS] Docker image built"

# Verify image
echo ""
echo "[3/4] Verifying image..."
docker images | grep fiji-cli-agent

# Test run
echo ""
echo "[4/4] Testing container..."
docker run --rm fiji-cli-agent:latest java -version 2>&1 | head -1
docker run --rm fiji-cli-agent:latest fiji --version 2>&1 | head -1

echo ""
echo "======================================"
echo "  Build Complete!"
echo "======================================"
echo ""
echo "Usage:"
echo "  docker run --rm fiji-cli-agent:latest <command>"
echo ""
echo "Examples:"
echo "  docker run --rm fiji-cli-agent:latest java -cp bin fiji.agent.FijiAgent --help"
echo "  docker run --rm -v \$(pwd):/data fiji-cli-agent:latest analyze particles --input /data/test_cells.tif --output /data/results"
echo ""
