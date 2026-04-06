// Template: cell_counting.ijm
// Description: Complete cell counting pipeline from source doc 4.2.1
// Parameters (injected at runtime):
//   {{INPUT_DIR}} - Directory containing input images
//   {{OUTPUT_DIR}} - Directory for results
//   {{MIN_AREA}} - Minimum particle area
//   {{MAX_AREA}} - Maximum particle area
//   {{THRESHOLD_METHOD}} - Auto-threshold method (Otsu, Li, etc.)

// Get parameters from command line
inputDir = "{{INPUT_DIR}}";
outputDir = "{{OUTPUT_DIR}}";
minArea = {{MIN_AREA}};
maxArea = {{MAX_AREA}};
thresholdMethod = "{{THRESHOLD_METHOD}}";

// Get file list
list = getFileList(inputDir);

setBatchMode(true);  // Accelerate batch processing

for (i = 0; i < list.length; i++) {
    if (!endsWith(list[i], ".tif")) continue;
    
    // Open image
    open(inputDir + list[i]);
    title = getTitle();
    baseName = substring(title, 0, lastIndexOf(title, "."));
    
    // Preprocessing: denoise
    run("Gaussian Blur...", "sigma=2");
    
    // Threshold segmentation
    setAutoThreshold(thresholdMethod + " dark");
    run("Convert to Mask");
    
    // Morphological cleanup
    run("Options...", "iterations=1 count=1 do=Open");
    run("Options...", "iterations=1 count=1 do=Close");
    
    // Particle analysis
    run("Set Measurements...", "area mean min centroid center perimeter fit shape integrated display redirect=None decimal=3");
    run("Analyze Particles...", "size=" + minArea + "-" + maxArea + " circularity=0.50-1.00 show=Outlines display exclude clear summarize");
    
    // Save results
    saveAs("Results", outputDir + baseName + "_measurements.csv");
    saveAs("Tiff", outputDir + baseName + "_outlines.tif");
    
    // Cleanup
    close("*");
    run("Clear Results");
}

setBatchMode(false);

// Summary
print("Processing complete: " + list.length + " images");
