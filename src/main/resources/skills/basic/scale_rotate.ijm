// Skill: scale_rotate
// Description: Geometric transformations - scale, rotate, crop, affine transform
// Params: input_path (string) - Path to input image
//         output_path (string) - Path for output image
//         operation (string) - "scale", "rotate", "crop" (default: scale)
//         scale_x (float) - X scaling factor (default: 1.0)
//         scale_y (float) - Y scaling factor (default: 1.0)
//         angle (float) - Rotation angle in degrees (default: 0)
//         interpolation (string) - "None", "Bilinear", "Bicubic" (default: Bilinear)
//         crop_x, crop_y, crop_w, crop_h - Crop rectangle (for crop operation)
// Returns: Transformed image

macro "Scale and Rotate" {
    // Parse arguments
    args = getArgument();
    if (args == "") {
        print("Error: No arguments provided");
        print("Usage: fiji -batch scale_rotate.ijm \"input_path,output_path,operation,params...\"");
        print("Operations: scale, rotate, crop");
        exit();
    }
    
    argArray = split(args, ",");
    if (argArray.length < 2) {
        print("Error: Need input_path and output_path");
        exit();
    }
    
    inputPath = argArray[0];
    outputPath = argArray[1];
    operation = argArray.length > 2 ? toLowerCase(argArray[2]) : "scale";
    
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
        width = getWidth();
        height = getHeight();
        
        if (operation == "scale") {
            // Scaling operation
            scaleX = argArray.length > 3 ? parseFloat(argArray[3]) : 1.0;
            scaleY = argArray.length > 4 ? parseFloat(argArray[4]) : scaleX;
            interpolation = argArray.length > 5 ? argArray[5] : "Bilinear";
            
            // Convert interpolation to ImageJ format
            if (interpolation == "None") {
                interpStr = "None";
            } else if (interpolation == "Bicubic") {
                interpStr = "Bicubic";
            } else {
                interpStr = "Bilinear";
            }
            
            newWidth = round(width * scaleX);
            newHeight = round(height * scaleY);
            
            run("Scale...", "x=- y=- width=" + newWidth + " height=" + newHeight + " interpolation=" + interpStr + " average create title=Scaled");
            
            print("Scaled to " + newWidth + "x" + newHeight + " (" + scaleX + "x" + scaleY + ")");
            
        } else if (operation == "rotate") {
            // Rotation operation
            angle = argArray.length > 3 ? parseFloat(argArray[3]) : 0;
            interpolation = argArray.length > 4 ? argArray[4] : "Bilinear";
            enlarge = argArray.length > 5 ? argArray[5] : "true";
            
            if (interpolation == "None") {
                interpStr = "None";
            } else if (interpolation == "Bicubic") {
                interpStr = "Bicubic";
            } else {
                interpStr = "Bilinear";
            }
            
            enlargeStr = enlarge == "true" ? " enlarge" : "";
            
            run("Rotate... ", "angle=" + angle + " grid=1 interpolation=" + interpStr + enlargeStr);
            
            print("Rotated by " + angle + " degrees");
            
        } else if (operation == "crop") {
            // Crop operation
            cropX = argArray.length > 3 ? parseInt(argArray[3]) : 0;
            cropY = argArray.length > 4 ? parseInt(argArray[4]) : 0;
            cropW = argArray.length > 5 ? parseInt(argArray[5]) : width;
            cropH = argArray.length > 6 ? parseInt(argArray[6]) : height;
            
            makeRectangle(cropX, cropY, cropW, cropH);
            run("Crop");
            
            print("Cropped to " + cropW + "x" + cropH + " at (" + cropX + "," + cropY + ")");
            
        } else {
            print("Error: Unknown operation '" + operation + "'");
            print("Supported: scale, rotate, crop");
            exit();
        }
        
        // Save result
        saveAs("Tiff", outputPath);
        
        print("Success: " + operation + " completed");
        print("Output: " + outputPath);
        
    } catch (e) {
        print("Error during transformation: " + e);
    } finally {
        close("*");
        setBatchMode(false);
    }
}
