// Skill: analyze_particles
// Description: Particle/cell counting and morphological analysis
// Params: input_path, output_dir, size_min, size_max, circularity_min, threshold_method

args = getArgument();
if (args == "") {
    print("Error: No arguments");
    exit();
}

argArray = split(args, ",");
if (argArray.length < 2) {
    print("Error: Need input_path,output_dir");
    exit();
}

inputPath = argArray[0];
outputDir = argArray[1];
sizeMin = 0;
sizeMax = "Infinity";
circMin = 0.50;
threshMethod = "Otsu";

if (argArray.length > 2) sizeMin = parseFloat(argArray[2]);
if (argArray.length > 3) sizeMax = argArray[3];
if (argArray.length > 4) circMin = parseFloat(argArray[4]);
if (argArray.length > 5) threshMethod = argArray[5];

if (!File.exists(inputPath)) {
    print("Error: Input not found: " + inputPath);
    exit();
}

if (!File.exists(outputDir)) {
    File.makeDirectory(outputDir);
}

fileName = File.getName(inputPath);
baseName = substring(fileName, 0, lastIndexOf(fileName, "."));

setBatchMode(true);
open(inputPath);

// Set measurements
run("Set Measurements...", "area mean standard min centroid center of mass bounding box fit shape feret's integrated median skewness kurtosis area_fraction stack display redirect=None decimal=3");

// Threshold
setAutoThreshold(threshMethod + " dark");
run("Convert to Mask");

// Analyze particles - save results directly, no display
run("Analyze Particles...", "size=" + sizeMin + "-" + sizeMax + " circularity=" + circMin + "-1.00 show=Nothing clear summarize");

// Save results
resultsPath = outputDir + "/" + baseName + "_measurements.csv";
saveAs("Results", resultsPath);

// Save summary
selectWindow("Summary");
summaryPath = outputDir + "/" + baseName + "_summary.csv";
saveAs("Results", summaryPath);

print("Success: " + fileName);
print("Results: " + resultsPath);

close("*");
run("Clear Results");
setBatchMode(false);
