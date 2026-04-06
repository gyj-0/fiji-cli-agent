// Skill: adjust_contrast
// Description: Adjust brightness/contrast or apply histogram equalization
// Params: input_path (string) - Path to input image
//         output_path (string) - Path for output image
//         method (string) - "auto", "histogram", "clahe" (default: auto)
//         saturate (float) - Saturation percentage for auto (default: 0.35)
//         apply_to_pixels (boolean) - Apply to pixels vs display only (default: true)
// Returns: Contrast-adjusted image

macro "Adjust Contrast" {
    // Parse arguments
    args = getArgument();
    if (args == "") {
        print("Error: No arguments provided");
        print("Usage: fiji -batch adjust_contrast.ijm \"input_path,output_path,method,saturate,apply_to_pixels\"");
        exit();
    }
    
    argArray = split(args, ",");
    if (argArray.length < 2) {
        print("Error: Need input_path and output_path");
        exit();
    }
    
    inputPath = argArray[0];
    outputPath = argArray[1];
    method = argArray.length > 2 ? toLowerCase(argArray[2]) : "auto";
    saturate = argArray.length > 3 ? parseFloat(argArray[3]) : 0.35;
    applyToPixels = argArray.length > 4 ? argArray[4] : "true";
    
    // Validate input
    if (!File.exists(inputPath)) {
        print("Error: Input file not found: " + inputPath);
        exit();
    }
    
    setBatchMode(true);
    
    try {
        // Open image
        open(inputPath);
        imageTitle = getTitle();
        
        if (method == "auto") {
            // Auto contrast enhancement
            saturateStr = toString(saturate);
            run("Enhance Contrast...", "saturated=" + saturateStr + " normalize");
            
        } else if (method == "histogram") {
            // Histogram equalization
            run("Enhance Contrast...", "equalize");
            
        } else if (method == "clahe") {
            // CLAHE (Contrast Limited Adaptive Histogram Equalization)
            blockSize = 127;  // default
            histogramBins = 256;
            maxSlope = 3;
            run("Enhance Local Contrast (CLAHE)", "blocksize=" + blockSize + " histogram=" + histogramBins + " maximum=" + maxSlope + " mask=*None* fast_(less_accurate)");
            
        } else {
            print("Warning: Unknown method '" + method + "', using auto");
            run("Enhance Contrast...", "saturated=0.35 normalize");
        }
        
        // Apply to pixels if requested (not just display LUT)
        if (applyToPixels == "true") {
            run("Apply LUT");
        }
        
        // Save result
        saveAs("Tiff", outputPath);
        
        print("Success: Contrast adjusted (method=" + method + ")");
        print("Output: " + outputPath);
        
    } catch (e) {
        print("Error during contrast adjustment: " + e);
    } finally {
        close("*");
        setBatchMode(false);
    }
}
