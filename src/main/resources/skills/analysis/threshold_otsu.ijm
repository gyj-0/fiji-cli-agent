// Skill: threshold_otsu
// Description: Auto-threshold segmentation using Otsu or other methods
// Params: input_path (string) - Path to input image
//         output_path (string) - Path for output mask image
//         method (string) - Otsu, Li, Triangle, Huang, IsoData (default: Otsu)
//         dark_background (boolean) - true/false (default: true)
//         apply_to_pixels (boolean) - Convert to mask or just set threshold (default: true)
// Returns: Binary mask image path

macro "Threshold Otsu" {
    // Parse arguments
    args = getArgument();
    if (args == "") {
        print("Error: No arguments provided");
        print("Usage: fiji -batch threshold_otsu.ijm \"input_path,output_path,method,dark_background,apply_to_pixels\"");
        exit();
    }
    
    argArray = split(args, ",");
    if (argArray.length < 2) {
        print("Error: Need input_path and output_path");
        exit();
    }
    
    inputPath = argArray[0];
    outputPath = argArray[1];
    method = argArray.length > 2 ? argArray[2] : "Otsu";
    darkBackground = argArray.length > 3 ? argArray[3] : "true";
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
        
        // Build threshold method string
        darkStr = darkBackground == "true" ? " dark" : "";
        
        // Apply auto threshold
        setAutoThreshold(method + darkStr);
        
        if (applyToPixels == "true") {
            // Convert to mask
            run("Convert to Mask");
            
            // Save mask
            saveAs("Tiff", outputPath);
        } else {
            // Just apply threshold display
            run("Apply LUT");
            saveAs("Tiff", outputPath);
        }
        
        print("Success: Threshold applied using " + method);
        print("Output: " + outputPath);
        
    } catch (e) {
        print("Error during thresholding: " + e);
    } finally {
        close("*");
        setBatchMode(false);
    }
}
