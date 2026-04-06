// Skill: open_image
// Description: Open image files with various formats (TIFF, PNG, JPEG, Bio-Formats)
// Params: input_path (string) - Path to input image file
//         output_info (string) - Optional: path to save image info JSON
//         virtual (boolean) - Use virtual stack mode for large files (default: false)
// Returns: Opens image and optionally saves metadata

macro "Open Image" {
    // Parse arguments
    args = getArgument();
    if (args == "") {
        print("Error: No arguments provided");
        print("Usage: fiji -batch open_image.ijm \"input_path,output_info,virtual\"");
        exit();
    }
    
    argArray = split(args, ",");
    if (argArray.length < 1) {
        print("Error: Need input_path");
        exit();
    }
    
    inputPath = argArray[0];
    outputInfo = argArray.length > 1 ? argArray[1] : "";
    useVirtual = argArray.length > 2 ? argArray[2] : "false";
    
    // Validate input
    if (!File.exists(inputPath)) {
        print("Error: Input file not found: " + inputPath);
        exit();
    }
    
    setBatchMode(true);
    
    try {
        // Check file extension
        ext = toLowerCase(File.getName(inputPath));
        
        // Open with Bio-Formats if needed (composite formats)
        // For now, use standard opener with virtual stack option
        if (useVirtual == "true") {
            // Use virtual stack for large files
            run("TIFF Virtual Stack...", "open=[" + inputPath + "]");
        } else {
            // Standard open
            open(inputPath);
        }
        
        imageTitle = getTitle();
        
        // Get image info
        width = getWidth();
        height = getHeight();
        channels = nChannels();
        slices = nSlices();
        frames = nFrames();
        bitDepth = bitDepth();
        
        info = "Image: " + imageTitle + "\n";
        info = info + "Dimensions: " + width + "x" + height + "\n";
        info = info + "Channels: " + channels + "\n";
        info = info + "Slices (Z): " + slices + "\n";
        info = info + "Frames (T): " + frames + "\n";
        info = info + "Bit Depth: " + bitDepth + "\n";
        
        print(info);
        
        // Save info if requested
        if (outputInfo != "") {
            File.saveString(info, outputInfo);
            print("Info saved to: " + outputInfo);
        }
        
        // Keep image open for subsequent operations
        // Caller should close when done
        print("Success: Image opened - " + imageTitle);
        
    } catch (e) {
        print("Error opening image: " + e);
    } finally {
        // Don't close image - leave it for next operation
        setBatchMode(false);
    }
}
