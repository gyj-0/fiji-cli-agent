// Skill: gaussian_blur
// Description: Apply Gaussian blur filter for smoothing and noise reduction
// Params: input_path (string) - Path to input image
//         output_path (string) - Path for output image
//         sigma (float) - Standard deviation of Gaussian kernel (default: 2.0)
//         stack (boolean) - Process entire stack if true (default: true)
// Returns: Blurred image saved to output_path

macro "Gaussian Blur" {
    // Parse arguments
    args = getArgument();
    if (args == "") {
        print("Error: No arguments provided");
        print("Usage: fiji -batch gaussian_blur.ijm \"input_path,output_path,sigma,stack\"");
        exit();
    }
    
    argArray = split(args, ",");
    if (argArray.length < 2) {
        print("Error: Need input_path and output_path");
        exit();
    }
    
    inputPath = argArray[0];
    outputPath = argArray[1];
    sigma = argArray.length > 2 ? parseFloat(argArray[2]) : 2.0;
    processStack = argArray.length > 3 ? argArray[3] : "true";
    
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
        
        // Build Gaussian blur command
        sigmaStr = toString(sigma);
        
        if (processStack == "true" && nSlices() > 1) {
            // Process entire stack
            run("Gaussian Blur...", "sigma=" + sigmaStr + " stack");
        } else {
            // Process current slice only
            run("Gaussian Blur...", "sigma=" + sigmaStr);
        }
        
        // Save result
        saveAs("Tiff", outputPath);
        
        print("Success: Gaussian blur applied (sigma=" + sigmaStr + ")");
        print("Output: " + outputPath);
        
    } catch (e) {
        print("Error during Gaussian blur: " + e);
    } finally {
        close("*");
        setBatchMode(false);
    }
}
