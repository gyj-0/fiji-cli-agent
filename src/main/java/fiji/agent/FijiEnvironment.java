package fiji.agent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * FijiEnvironment - Detects and configures Fiji installation.
 * S0: Environment detection for CLI execution.
 */
public class FijiEnvironment {
    
    private static final String[] FIJI_CANDIDATES = {
        System.getenv("FIJI_HOME") != null ? System.getenv("FIJI_HOME") + "/fiji" : null,
        System.getProperty("user.home") + "/Fiji/fiji",
        "/home/gyjgyj/Fiji/fiji",
        "/opt/fiji/Fiji.app/fiji",
        "/usr/local/fiji/fiji",
        "fiji"  // Try PATH as last resort
    };
    
    private final String fijiExecutable;
    private final boolean found;
    
    public FijiEnvironment() {
        String detected = detectFiji();
        this.fijiExecutable = detected;
        this.found = detected != null;
    }
    
    public FijiEnvironment(String customPath) {
        if (customPath != null && isValidFiji(customPath)) {
            this.fijiExecutable = customPath;
            this.found = true;
        } else {
            String detected = detectFiji();
            this.fijiExecutable = detected;
            this.found = detected != null;
        }
    }
    
    /**
     * Detect Fiji installation in standard locations.
     */
    private String detectFiji() {
        for (String candidate : FIJI_CANDIDATES) {
            if (candidate == null) continue;
            
            if (isValidFiji(candidate)) {
                System.out.println("[INFO] Fiji found at: " + candidate);
                return candidate;
            }
        }
        
        System.err.println("[ERROR] Fiji not found. Please set FIJI_HOME environment variable");
        System.err.println("        or install Fiji in one of these locations:");
        for (String candidate : FIJI_CANDIDATES) {
            if (candidate != null) {
                System.err.println("          - " + candidate);
            }
        }
        return null;
    }
    
    private boolean isValidFiji(String path) {
        File f = new File(path);
        return f.exists() && f.canExecute();
    }
    
    public boolean isFound() {
        return found;
    }
    
    public String getFijiExecutable() {
        if (!found) {
            throw new IllegalStateException("Fiji not found. Call isFound() first.");
        }
        return fijiExecutable;
    }
    
    /**
     * Check if test image exists.
     */
    public static boolean checkTestImage(String path) {
        if (path == null) return false;
        File f = new File(path);
        boolean exists = f.exists() && f.isFile();
        if (!exists) {
            System.err.println("[ERROR] Test image not found: " + path);
        }
        return exists;
    }
    
    public static void main(String[] args) {
        FijiEnvironment env = new FijiEnvironment();
        if (env.isFound()) {
            System.out.println("Fiji executable: " + env.getFijiExecutable());
        } else {
            System.out.println("Fiji not found");
            System.exit(1);
        }
        
        // Check test image
        String testImage = "test_cells.tif";
        if (checkTestImage(testImage)) {
            System.out.println("Test image found: " + testImage);
        }
    }
}
