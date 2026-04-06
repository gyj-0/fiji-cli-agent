# Test Samples for Fiji CLI Agent Skills

## Required Test Images

### 1. single_cell.tif
- **Purpose**: Test basic processing skills (blur, contrast)
- **Format**: Single channel, 8-bit or 16-bit TIFF
- **Size**: 512x512 pixels
- **Content**: Fluorescence microscopy image of cells

**Generate synthetically:**
```python
# Using Python + NumPy + OpenCV or ImageJ macro
```

### 2. dual_channel.tif
- **Purpose**: Test colocalization analysis
- **Format**: Two-channel image (e.g., GFP + DAPI)
- **Size**: 512x512 pixels
- **Content**: Two overlapping fluorescence channels

### 3. z_stack.tif
- **Purpose**: Test Z-projection
- **Format**: Multi-slice TIFF stack
- **Slices**: 10-20 Z planes
- **Content**: 3D volume of cells or beads

### 4. timelapse.tif
- **Purpose**: Test TrackMate tracking
- **Format**: Multi-frame TIFF
- **Frames**: 20+ time points
- **Content**: Moving cells or particles

### 5. beads_or_particles.tif
- **Purpose**: Test particle analysis
- **Format**: Single channel
- **Content**: Fluorescent beads or stained nuclei

## Download Public Test Data

### ImageJ Sample Images
```bash
# Available in Fiji: File > Open Samples
# Or download from:
# https://imagej.nih.gov/ij/images/
```

### Cellpose Test Images
```bash
# Cellpose provides sample images:
# https://cellpose.readthedocs.io/en/latest/api.html#example-data
```

### TrackMate Example Data
```bash
# Available in Fiji via: File > Open Samples > Tracks for TrackMate
```

## Creating Synthetic Test Data

### Using Fiji Macro

```javascript
// Create test stack
newImage("Test Stack", "8-bit black", 512, 512, 10);

// Add noise and features
for (i = 1; i <= 10; i++) {
    setSlice(i);
    run("Add Noise");
    makeOval(100+i*10, 100+i*10, 50, 50);
    setColor(255);
    fill();
}

saveAs("Tiff", "test_z_stack.tif");
```

## Validation Tests

### Test 1: Gaussian Blur
```bash
fiji --headless -batch ../basic/gaussian_blur.ijm \
    "single_cell.tif,test_blur.tif,2.0,true"
```
**Expected**: Output image smoothed, no errors

### Test 2: Particle Analysis
```bash
fiji --headless -batch ../analysis/analyze_particles.ijm \
    "beads.tif,./test_output,10,1000,0.5,Otsu"
```
**Expected**: CSV files with particle measurements

### Test 3: Z Projection
```bash
fiji --headless -batch ../analysis/z_project.ijm \
    "z_stack.tif,test_proj.tif,Max,1,10"
```
**Expected**: Single 2D image, max intensity projection

## Automated Test Script

```bash
#!/bin/bash
# run_tests.sh - Automated skill validation

SKILLS_DIR=".."
TEST_DIR="."
OUTPUT_DIR="./test_results"

mkdir -p $OUTPUT_DIR

echo "Testing Basic Skills..."
fiji --headless -batch $SKILLS_DIR/basic/gaussian_blur.ijm \
    "$TEST_DIR/single_cell.tif,$OUTPUT_DIR/blur_test.tif,2.0,true"

echo "Testing Analysis Skills..."
fiji --headless -batch $SKILLS_DIR/analysis/analyze_particles.ijm \
    "$TEST_DIR/beads.tif,$OUTPUT_DIR/particles,10,1000,0.5,Otsu"

echo "Tests complete. Check $OUTPUT_DIR for results."
```

## Notes

- Test images should be representative of real-world data
- Include edge cases: noisy images, uneven illumination, overlapping objects
- Keep test images small for faster CI/CD execution
