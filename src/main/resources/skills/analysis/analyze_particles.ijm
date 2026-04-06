// Skill: analyze_particles
// Description: Particle/cell counting and morphological analysis
// Params: input_path (string) - Path to input image file
//         output_dir (string) - Directory for output files
//         size_min (float) - Minimum particle area in pixel^2 (default: 0)
//         size_max (float) - Maximum particle area (default: Infinity)
//         circularity_min (float) - 0.0-1.0 (default: 0.50)
//         threshold_method (string) - Otsu, Li, Triangle (default: Otsu)
// Returns: ResultsTable with measurements saved to CSV
// Dependencies: None (self-contained)

macro "Analyze Particles" {
    // Parse arguments
    args = getArgument();
    if (args == "") {
        print("Error: No arguments provided");
        print("Usage: fiji -batch analyze_particles.ijm \"input_path,output_dir,size_min,size_max,circularity_min,threshold_method\"");
        exit();
    }
    
    // Split arguments
    argArray = split(args, ",");
    if (argArray.length < 2) {
        print("Error: Insufficient arguments. Need at least input_path and output_dir");
        exit();
    }
    
    inputPath = argArray[0];
    outputDir = argArray[1];
    sizeMin = argArray.length > 2 ? parseFloat(argArray[2]) : 0;
    sizeMax = argArray.length > 3 ? argArray[3] : "Infinity";
    circMin = argArray.length > 4 ? parseFloat(argArray[4]) : 0.50;
    threshMethod = argArray.length > 5 ? argArray[5] : "Otsu";
    
    // Validate input
    if (!File.exists(inputPath)) {
        print("Error: Input file not found: " + inputPath);
        exit();
    }
    
    // Ensure output directory exists
    if (!File.exists(outputDir)) {
        File.makeDirectory(outputDir);
    }
    
    // Extract filename for output
    fileName = File.getName(inputPath);
    baseName = substring(fileName, 0, lastIndexOf(fileName, "."));
    
    // Enable batch mode
    setBatchMode(true);
    
    try {
        // Open image
        open(inputPath);
        imageTitle = getTitle();
        
        // Set measurements (20+ metrics)
        run("Set Measurements...", "area mean standard min centroid center of mass bounding box fit shape feret's integrated median skewness kurtosis area_fraction stack display redirect=None decimal=3");
        
        // Auto threshold
        setAutoThreshold(threshMethod + " dark");
        run("Convert to Mask");
        
        // Optional: morphological cleanup
        run("Options...", "iterations=1 count=1 do=Open");
        run("Options...", "iterations=1 count=1 do=Close");
        
        // Build analyze particles command
        sizeParam = "size=" + sizeMin + "-" + sizeMax;
        circParam = "circularity=" + circMin + "-1.00";
        options = "show=Outlines display exclude clear summarize " + sizeParam + " " + circParam;
        
        // Analyze particles
        run("Analyze Particles...", options);
        
        // Save results
        resultsPath = outputDir + "/" + baseName + "_measurements.csv";
        saveAs("Results", resultsPath);
        
        // Save outlines/mask
        selectWindow("Drawing of " + imageTitle);
        outlinesPath = outputDir + "/" + baseName + "_outlines.tif";
        saveAs("Tiff", outlinesPath);
        
        // Save summary
        selectWindow("Summary");
        summaryPath = outputDir + "/" + baseName + "_summary.csv";
        saveAs("Results", summaryPath);
        
        print("Success: Analyzed " + fileName);
        print("Results: " + resultsPath);
        print("Outlines: " + outlinesPath);
        print("Summary: " + summaryPath);
        
    } catch (e) {
        print("Error during analysis: " + e);
    } finally {
        // Cleanup
        close("*");
        run("Clear Results");
        setBatchMode(false);
    }
}
