# Fiji CLI Agent - Image Processing Skills

## Skill Overview

This directory contains Fiji ImageJ macros and Jython scripts for automated image processing and analysis.

## Skill Categories

### 3.1 Basic Image Processing (`basic/`)

| Skill | File | Description | Example Usage |
|-------|------|-------------|---------------|
| Open Image | `open_image.ijm` | Open various image formats (TIFF, PNG, JPEG, Bio-Formats) | `fiji -batch open_image.ijm "input.tif,output_dir"` |
| Gaussian Blur | `gaussian_blur.ijm` | Gaussian blur filter for smoothing | `fiji -batch gaussian_blur.ijm "input.tif,output.tif,2.0,true"` |
| Adjust Contrast | `adjust_contrast.ijm` | Auto contrast, histogram equalization, CLAHE | `fiji -batch adjust_contrast.ijm "input.tif,output.tif,auto,0.35,true"` |
| Scale & Rotate | `scale_rotate.ijm` | Geometric transformations (scale, rotate, crop) | `fiji -batch scale_rotate.ijm "input.tif,output.tif,scale,0.5,0.5"` |

**Parameters:**
- `input_path`: Path to input image file
- `output_path`: Path for output image
- `sigma`: Gaussian blur standard deviation (e.g., 2.0)
- `method`: Contrast method ("auto", "histogram", "clahe")
- `operation`: Transform operation ("scale", "rotate", "crop")

### 3.2 Core Analysis Skills (`analysis/`)

| Skill | File | Description | Example Usage |
|-------|------|-------------|---------------|
| Threshold Otsu | `threshold_otsu.ijm` | Auto-threshold segmentation | `fiji -batch threshold_otsu.ijm "input.tif,mask.tif,Otsu,true,true"` |
| Analyze Particles | `analyze_particles.ijm` | Particle/cell counting and measurement | `fiji -batch analyze_particles.ijm "input.tif,output_dir,0,Infinity,0.50,Otsu"` |
| Z Project | `z_project.ijm` | Z-stack projection (Max, Average, Sum, StdDev) | `fiji -batch z_project.ijm "stack.tif,proj.tif,Max,1,10"` |
| Colocalization | `colocalization.py` | Multi-channel fluorescence colocalization | `jython colocalization.py image.tif 1 2 output_dir` |

**Parameters:**
- `size_min`, `size_max`: Particle size range (pixel²)
- `circularity_min`: 0.0-1.0 (1.0 = perfect circle)
- `method`: Threshold method ("Otsu", "Li", "Triangle", "Huang")
- `projection_method`: "Max", "Average", "Sum", "StdDev", "Median"
- `channel1`, `channel2`: Channel indices (1-based)

### 3.3 Advanced Professional Skills (`advanced/`)

| Skill | File | Description | Example Usage |
|-------|------|-------------|---------------|
| bUnwarpJ Register | `bunwarpj_register.py` | Elastic image registration | `jython bunwarpj_register.py ref.tif moving.tif output.tif` |
| TrackMate Batch | `trackmate_batch.py` | Object tracking in time-lapse | `jython trackmate_batch.py timelapse.tif LoG 2.5` |
| Cellpose Segment | `cellpose_segment.py` | Deep learning cell segmentation | `jython cellpose_segment.py image.tif cyto3 30.0` |

**Requirements:**
- bUnwarpJ: Requires bUnwarpJ plugin installed
- TrackMate: Requires TrackMate plugin installed
- Cellpose: Requires external Python with `pip install cellpose`

## Quick Start

### Test a Basic Skill

```bash
# Gaussian blur with sigma=2.0
fiji --headless -batch gaussian_blur.ijm "input.tif,blurred.tif,2.0,true"
```

### Test Particle Analysis

```bash
# Analyze particles with Otsu threshold, size 10-500 pixels
fiji --headless -batch analyze_particles.ijm "cells.tif,./results,10,500,0.50,Otsu"
```

### Test Z-Projection

```bash
# Maximum intensity projection of slices 1-10
fiji --headless -batch z_project.ijm "stack.tif,max_proj.tif,Max,1,10"
```

## Test Samples

Place test images in `test_samples/`:

```
test_samples/
├── single_cell.tif       # Single channel cell image
├── dual_channel.tif      # Two-channel fluorescence
├── z_stack.tif          # 3D Z-stack (10+ slices)
└── timelapse.tif        # Time-lapse sequence
```

## Output Format

### Analyze Particles Output

- `*_measurements.csv`: Per-particle measurements (area, perimeter, circularity, etc.)
- `*_outlines.tif`: Overlay image showing detected particles
- `*_summary.csv`: Summary statistics

### Measurements Included

- **Geometry**: Area, Perimeter, Circularity, Aspect Ratio, Roundness, Solidity
- **Position**: Centroid, Center of Mass, Bounding Box
- **Shape**: Fit Ellipse (Major/Minor Axis, Angle), Feret's Diameter
- **Intensity**: Mean, StdDev, Min, Max, Integrated Density, Median
- **Advanced**: Skewness, Kurtosis, Area Fraction

## Dependencies

### Required Fiji Plugins

- Bio-Formats (for diverse image formats)
- bUnwarpJ (for elastic registration)
- TrackMate (for object tracking)

### External Dependencies

- **Cellpose**: Install via `pip install cellpose`
- **GPU Support**: For Cellpose GPU acceleration, install CUDA

## Troubleshooting

### "Plugin not found" errors

Install missing plugins via Fiji Update Site:
```
Help > Update > Manage Update Sites
```

### Cellpose not found

Ensure Cellpose is in PATH:
```bash
which cellpose
# or
python -m cellpose --help
```

### Memory errors for large images

Increase Fiji memory:
```bash
fiji -Xmx8g --headless -batch script.ijm "args"
```

## Development Notes

- All IJM macros support `getArgument()` for CLI parameter passing
- All Python scripts can be run standalone or imported as modules
- Batch mode (`setBatchMode(true)`) is enabled for headless operation
- Error handling includes try-catch blocks with informative messages

## References

Based on Fiji CLI Harness Agent project specification:
- Section 3.1: Basic Image Processing
- Section 3.2: Core Analysis Skills
- Section 3.3: Advanced Professional Skills
