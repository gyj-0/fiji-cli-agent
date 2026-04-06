# Fiji CLI Harness Agent

Image processing skill system for Fiji/ImageJ with CLI interface.

## Quick Start

### Requirements

- Java 17+
- Fiji (ImageJ) 2.15.1+ or local Fiji installation
- (Optional) Xvfb for true headless operation

### Build

```bash
# Compile
javac -d bin src/main/java/fiji/agent/*.java

# Or use Docker (recommended for headless environments)
./docker-build.sh
```

### Usage

#### Local Execution

```bash
# Analyze particles
java -cp bin fiji.agent.FijiAgent analyze particles --input test.tif --output ./results

# Gaussian blur
java -cp bin fiji.agent.FijiAgent gaussian blur --input test.tif --sigma 2.0
```

#### Docker Execution

```bash
# Run with Docker
docker run --rm -v $(pwd):/data fiji-cli-agent:latest \
    analyze particles --input /data/test.tif --output /data/results

# Or use local script
./docker-run.sh analyze particles --input test.tif --output ./results
```

## Known Issues

### Analyze Particles Headless Mode

**Problem**: Analyze Particles plugin attempts to show GUI dialogs even in `--headless` mode, causing `HeadlessException` or `NullPointerException`.

**Root Cause**: Fiji/ImageJ 1.x plugin architecture limitation. The plugin calls `showDialog()` during setup.

**Solutions**:

1. **Use Xvfb (Recommended for servers)**
   ```bash
   # Install Xvfb
   sudo apt-get install xvfb
   
   # Run with Xvfb wrapper
   xvfb-run -a -screen 0 1024x768x24 \
       java -cp bin fiji.agent.FijiAgent \
       analyze particles --input test.tif --output ./results
   ```

2. **Use Docker with Xvfb**
   ```bash
   # The Docker image includes Xvfb
   ./docker-build.sh
   ./docker-run.sh analyze particles --input test.tif --output ./results
   ```

3. **Use GUI environment**
   Run on machine with display server (X11/Wayland).

## Architecture

```
CLI Command
    ↓
FijiAgent.main()
    ↓
FijiCommandParser → ParsedCommand
    ↓
SkillRegistry → Skill lookup
    ↓
Scheduler → Execution plan
    ↓
FijiBackendImpl
    ├── Xvfb auto-detection
    └── Runtime.exec("fiji --headless -batch ...")
    ↓
SkillResult
```

## Skill System

### Basic Processing (`basic/`)
- `open_image.ijm` - Open various formats
- `gaussian_blur.ijm` - Gaussian smoothing
- `adjust_contrast.ijm` - Contrast enhancement
- `scale_rotate.ijm` - Geometric transforms

### Core Analysis (`analysis/`)
- `threshold_otsu.ijm` - Auto threshold
- `analyze_particles.ijm` - Particle counting
- `z_project.ijm` - Z-stack projection
- `colocalization.py` - Multi-channel analysis

### Advanced (`advanced/`)
- `bunwarpj_register.py` - Elastic registration
- `trackmate_batch.py` - Object tracking
- `cellpose_segment.py` - Deep learning segmentation

## Docker Deployment

### Build
```bash
./docker-build.sh
```

### Run
```bash
# Analyze particles
docker run --rm -v $(pwd):/data fiji-cli-agent:latest \
    java -cp bin fiji.agent.FijiAgent \
    analyze particles --input /data/test.tif --output /data/results
```

### Dockerfile Features
- Ubuntu 24.04 LTS
- OpenJDK 17
- Fiji 2.15.1 (locked version)
- Xvfb for headless display

## License

MIT
