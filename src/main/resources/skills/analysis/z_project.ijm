// Skill: z_project
// Description: Z-stack projection (Max, Average, Sum, StdDev, Median)
// Params: input_path (string) - Path to Z-stack image
//         output_path (string) - Path for projected output
//         method (string) - Max, Average, Sum, StdDev, Median (default: Max)
//         start_slice (int) - First slice to project (1-based, default: 1)
//         stop_slice (int) - Last slice (default: all)
// Returns: Projected 2D image path

macro "Z Project" {
    // Parse arguments
    args = getArgument();
    if (args == "") {
        print("Error: No arguments provided");
        print("Usage: fiji -batch z_project.ijm \"input_path,output_path,method,start_slice,stop_slice\"");
        exit();
    }
    
    argArray = split(args, ",");
    if (argArray.length < 2) {
        print("Error: Need input_path and output_path");
        exit();
    }
    
    inputPath = argArray[0];
    outputPath = argArray[1];
    method = argArray.length > 2 ? argArray[2] : "Max";
    startSlice = argArray.length > 3 ? parseInt(argArray[3]) : 1;
    stopSlice = argArray.length > 4 ? parseInt(argArray[4]) : -1;
    
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
        
        // Get stack info
        nSlices = nSlices();
        if (nSlices <= 1) {
            print("Error: Image is not a stack (slices: " + nSlices + ")");
            exit();
        }
        
        // Validate slice range
        if (startSlice < 1) startSlice = 1;
        if (stopSlice < 1 || stopSlice > nSlices) stopSlice = nSlices;
        if (startSlice > stopSlice) {
            print("Error: start_slice > stop_slice");
            exit();
        }
        
        // Build Z projection command
        methodMap = newArray("Max Intensity", "Average Intensity", "Sum Slices", "Standard Deviation", "Median");
        methodNames = newArray("Max", "Average", "Sum", "StdDev", "Median");
        
        projectionMethod = "Max Intensity";  // default
        for (i = 0; i < methodNames.length; i++) {
            if (method == methodNames[i]) {
                projectionMethod = methodMap[i];
                break;
            }
        }
        
        // Select slice range if needed
        if (startSlice > 1 || stopSlice < nSlices) {
            run("Make Substack...", "slices=" + startSlice + "-" + stopSlice);
        }
        
        // Run Z projection
        run("Z Project...", "projection=[" + projectionMethod + "]");
        
        // Save result
        saveAs("Tiff", outputPath);
        
        print("Success: Z projection using " + projectionMethod);
        print("Slices: " + startSlice + "-" + stopSlice + " of " + nSlices);
        print("Output: " + outputPath);
        
    } catch (e) {
        print("Error during Z projection: " + e);
    } finally {
        close("*");
        setBatchMode(false);
    }
}
