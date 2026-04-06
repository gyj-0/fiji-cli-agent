# Skill: cellpose_segment
# Description: Cell segmentation using Cellpose (Python subprocess)
# Params: image_path (string) - Input image path
#         model (string) - "cyto", "cyto2", "cyto3", "nuclei"
#         diameter (float) - Estimated cell diameter (0 for auto)
#         channels (list) - [0,0] for grayscale, [1,2] for green/blue
#         output_dir (string) - Output directory for masks
#         device (string) - "cpu" or "cuda"
# Returns: Segmentation masks and flows
# Dependencies: open_image

def segment_cellpose(image_path, model="cyto3", diameter=30.0,
                     channels=[0,0], output_dir=None, device="cpu"):
    """
    Cellpose segmentation via subprocess call.
    Requires: cellpose Python environment installed
    Returns: (masks_path, flows_path)
    """
    # TODO: Implementation - subprocess.run(["cellpose", ...])
    # Reference: Source doc 3.3.3 - Cellpose batch CLI
    pass

def batch_segment(input_dir, pattern="*.tif", **kwargs):
    """Batch process all images in directory."""
    pass
