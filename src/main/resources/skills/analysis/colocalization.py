# Skill: colocalization
# Description: Multi-channel fluorescence colocalization analysis
# Requires: Fiji with Coloc 2 plugin or manual calculation

from ij import IJ, ImagePlus, ImageStack
from ij.measure import ResultsTable
from ij.plugin import ImageCalculator
import json
import os

def analyze_colocalization(image_path, channel1, channel2, roi_path=None, methods=None, output_dir=None):
    """
    Perform colocalization analysis on two channels.
    
    Args:
        image_path: Path to multi-channel image
        channel1: First channel index (1-based)
        channel2: Second channel index (1-based)
        roi_path: Optional ROI file path
        methods: List of methods ["pearson", "manders", "li", "icq"]
        output_dir: Directory for output files
    
    Returns:
        dict: Colocalization coefficients
    """
    if methods is None:
        methods = ["pearson", "manders"]
    
    if output_dir is None:
        output_dir = os.path.dirname(image_path)
    
    results = {
        "image": image_path,
        "channel1": channel1,
        "channel2": channel2,
        "methods": methods
    }
    
    try:
        # Open image
        imp = IJ.openImage(image_path)
        if imp is None:
            raise Exception("Failed to open image: " + image_path)
        
        # Check number of channels
        channels = imp.getNChannels()
        if channels < max(channel1, channel2):
            raise Exception("Image has " + str(channels) + " channels, requested channel " + 
                           str(max(channel1, channel2)))
        
        # Extract single channels
        imp.setC(channel1)
        ch1 = imp.getProcessor().convertToFloatProcessor()
        ch1_title = "C" + str(channel1) + "-" + os.path.basename(image_path)
        
        imp.setC(channel2)
        ch2 = imp.getProcessor().convertToFloatProcessor()
        ch2_title = "C" + str(channel2) + "-" + os.path.basename(image_path)
        
        # Create ImagePlus for each channel
        imp_ch1 = ImagePlus(ch1_title, ch1)
        imp_ch2 = ImagePlus(ch2_title, ch2)
        
        # Load ROI if provided
        if roi_path and os.path.exists(roi_path):
            IJ.open(roi_path)
            roi = IJ.getImage().getRoi()
            imp_ch1.setRoi(roi)
            imp_ch2.setRoi(roi)
        
        # Get pixel arrays
        pixels1 = ch1.getPixels()
        pixels2 = ch2.getPixels()
        
        # Calculate Pearson's correlation coefficient
        if "pearson" in methods:
            pearson_r = calculate_pearson(pixels1, pixels2)
            results["pearson_r"] = pearson_r
        
        # Calculate Manders' M1 and M2
        if "manders" in methods:
            m1, m2 = calculate_manders(pixels1, pixels2)
            results["manders_m1"] = m1
            results["manders_m2"] = m2
        
        # Calculate Li's ICQ
        if "li" in methods or "icq" in methods:
            icq = calculate_icq(pixels1, pixels2)
            results["icq"] = icq
        
        # Save results to JSON
        base_name = os.path.splitext(os.path.basename(image_path))[0]
        results_path = os.path.join(output_dir, base_name + "_colocalization.json")
        with open(results_path, 'w') as f:
            json.dump(results, f, indent=2)
        
        results["results_file"] = results_path
        IJ.log("Colocalization analysis complete")
        IJ.log("Results: " + results_path)
        
        return results
        
    except Exception as e:
        IJ.log("Error in colocalization: " + str(e))
        results["error"] = str(e)
        return results

def calculate_pearson(pixels1, pixels2):
    """Calculate Pearson's correlation coefficient."""
    n = len(pixels1)
    
    # Calculate means
    mean1 = sum(pixels1) / n
    mean2 = sum(pixels2) / n
    
    # Calculate numerator and denominators
    numerator = 0
    sum_sq1 = 0
    sum_sq2 = 0
    
    for i in range(n):
        diff1 = pixels1[i] - mean1
        diff2 = pixels2[i] - mean2
        numerator += diff1 * diff2
        sum_sq1 += diff1 * diff1
        sum_sq2 += diff2 * diff2
    
    denominator = (sum_sq1 * sum_sq2) ** 0.5
    
    if denominator == 0:
        return 0
    
    return numerator / denominator

def calculate_manders(pixels1, pixels2):
    """Calculate Manders' M1 and M2 coefficients."""
    # Threshold pixels (non-zero considered as signal)
    threshold = 0
    
    sum_ch1 = 0
    sum_ch2 = 0
    sum_overlap1 = 0
    sum_overlap2 = 0
    
    for i in range(len(pixels1)):
        p1 = pixels1[i]
        p2 = pixels2[i]
        
        sum_ch1 += p1
        sum_ch2 += p2
        
        if p2 > threshold:
            sum_overlap1 += p1
        if p1 > threshold:
            sum_overlap2 += p2
    
    m1 = sum_overlap1 / sum_ch1 if sum_ch1 > 0 else 0
    m2 = sum_overlap2 / sum_ch2 if sum_ch2 > 0 else 0
    
    return m1, m2

def calculate_icq(pixels1, pixels2):
    """Calculate Li's Intensity Correlation Quotient (ICQ)."""
    n = len(pixels1)
    mean1 = sum(pixels1) / n
    mean2 = sum(pixels2) / n
    
    positive = 0
    total = 0
    
    for i in range(n):
        diff1 = pixels1[i] - mean1
        diff2 = pixels2[i] - mean2
        product = diff1 * diff2
        
        if product > 0:
            positive += 1
        total += 1
    
    # ICQ = (proportion of positive products) - 0.5
    icq = (positive / total) - 0.5 if total > 0 else 0
    
    return icq

# Main execution if called directly
if __name__ == "__main__":
    import sys
    
    if len(sys.argv) < 4:
        print("Usage: jython colocalization.py <image_path> <channel1> <channel2> [output_dir]")
        sys.exit(1)
    
    image_path = sys.argv[1]
    channel1 = int(sys.argv[2])
    channel2 = int(sys.argv[3])
    output_dir = sys.argv[4] if len(sys.argv) > 4 else None
    
    results = analyze_colocalization(
        image_path, channel1, channel2,
        methods=["pearson", "manders", "icq"],
        output_dir=output_dir
    )
    
    print(json.dumps(results, indent=2))
