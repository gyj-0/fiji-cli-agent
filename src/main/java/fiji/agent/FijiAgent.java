package fiji.agent;

/**
 * FijiAgent - Main CLI entry point for Fiji CLI Harness Agent.
 * Implements complete command execution chain.
 */
public class FijiAgent {
    
    private final SkillRegistry registry;
    private final Scheduler scheduler;
    private final FijiCommandParser parser;
    private final FijiEnvironment environment;
    
    public FijiAgent() {
        this.environment = new FijiEnvironment();
        this.registry = new SkillRegistry();
        this.parser = new FijiCommandParser();
        this.scheduler = new Scheduler(registry, new FijiBackendImpl());
    }
    
    /**
     * S3a: Main entry point for CLI execution.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }
        
        FijiAgent agent = new FijiAgent();
        
        // Check environment
        if (!agent.environment.isFound()) {
            System.err.println("[ERROR] Fiji not found. Cannot execute skills.");
            System.exit(1);
        }
        
        // S3b: Scan skills and execute command
        agent.initialize();
        agent.processCommand(args);
    }
    
    /**
     * Initialize: scan skills directory.
     */
    public void initialize() {
        System.out.println("[INFO] Scanning skills directory...");
        // Try multiple possible skill directory locations
        String[] skillPaths = {
            "src/main/resources/skills",                    // Development
            "skills",                                        // Runtime (compiled)
            "/home/gyjgyj/fiji-cli-agent/src/main/resources/skills"  // Absolute
        };
        boolean scanned = false;
        for (String path : skillPaths) {
            java.io.File f = new java.io.File(path);
            if (f.exists() && f.isDirectory()) {
                registry.scanSkillsDirectory(path);
                scanned = true;
                break;
            }
        }
        if (!scanned) {
            System.err.println("[WARN] Could not find skills directory in any standard location");
        }
        System.out.println("[INFO] Registered " + registry.getSkillNames().size() + " skills");
    }
    
    /**
     * S3b: Process a single command.
     */
    public void processCommand(String[] args) {
        // Parse command
        FijiCommandParser.ParsedCommand command = parser.parse(args);
        
        if (!command.isValid()) {
            System.err.println("[ERROR] " + command.getErrorMessage());
            System.exit(1);
        }
        
        System.out.println("[INFO] Executing: " + command.getOriginal() + " -> " + command.getAction());
        
        // Lookup skill
        SkillRegistry.Skill skill = registry.getSkill(command.getAction());
        
        if (skill == null) {
            System.err.println("[ERROR] Skill not found: " + command.getAction());
            System.err.println("[INFO] Available skills: " + registry.getSkillNames());
            System.exit(1);
        }
        
        System.out.println("[INFO] Skill: " + skill.getName() + " (" + skill.getType() + ")");
        System.out.println("[INFO] Path: " + skill.getPath());
        
        // Execute via scheduler
        Scheduler.SkillResult result = scheduler.executeCommand(command, skill);
        
        // Output result
        if (result.isSuccess()) {
            System.out.println("[SUCCESS] Execution complete");
            System.out.println("[OUTPUT] " + result.getOutput());
            System.exit(0);
        } else {
            System.err.println("[ERROR] Execution failed: " + result.getErrorMessage());
            System.exit(1);
        }
    }
    
    /**
     * Plugin entry point placeholder (for future Fiji GUI integration).
     * Currently not used in CLI mode.
     */
    public void run(String arg) {
        System.out.println("Fiji CLI Agent started");
        initialize();
    }
    
    /**
     * S5: Backend implementation for Fiji execution.
     * Auto-detects Xvfb and wraps Fiji call for headless environments.
     */
    private class FijiBackendImpl implements Scheduler.FijiBackend {
        
        // Xvfb screen configuration
        private static final String XVFB_SCREEN = "0 1024x768x24";
        
        /**
         * Check if Xvfb is available in the system.
         */
        private boolean hasXvfb() {
            return java.nio.file.Files.exists(java.nio.file.Paths.get("/usr/bin/xvfb-run")) ||
                   java.nio.file.Files.exists(java.nio.file.Paths.get("/usr/bin/Xvfb"));
        }
        
        @Override
        public String executeMacro(String macroPath, String args) {
            try {
                String fijiPath = environment.getFijiExecutable();
                
                // Build command with Xvfb auto-detection
                java.util.List<String> command = new java.util.ArrayList<>();
                
                if (hasXvfb()) {
                    // Wrap with Xvfb for true headless operation
                    command.add("xvfb-run");
                    command.add("-a");
                    command.add("-screen");
                    command.add(XVFB_SCREEN);
                    System.out.println("[INFO] Xvfb detected, using virtual display");
                }
                
                command.add(fijiPath);
                command.add("--headless");
                command.add("-batch");
                command.add(macroPath);
                command.add(args);
                
                ProcessBuilder pb = new ProcessBuilder(command);
                
                System.out.println("[COMMAND] " + String.join(" ", command));
                
                Process process = pb.start();
                
                // Capture output
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
                );
                java.io.BufferedReader errorReader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getErrorStream())
                );
                
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                
                StringBuilder errors = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errors.append(line).append("\n");
                }
                
                int exitCode = process.waitFor();
                
                if (exitCode != 0) {
                    System.err.println("[ERROR] Fiji exited with code: " + exitCode);
                    System.err.println("[ERROR] Errors: " + errors.toString());
                    return null;  // Return null to indicate failure
                }
                
                if (errors.length() > 0) {
                    System.err.println("[WARN] Fiji stderr: " + errors.toString());
                }
                
                return output.toString();
                
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to execute Fiji: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        
        @Override
        public String executeJython(String scriptPath, java.util.Map<String, Object> params) {
            // For MVP, treat Jython same as macro via subprocess
            // In full implementation, could use Jython interpreter
            StringBuilder args = new StringBuilder();
            args.append(scriptPath);
            for (java.util.Map.Entry<String, Object> entry : params.entrySet()) {
                args.append(",").append(entry.getValue());
            }
            return executeMacro(scriptPath, args.toString());
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage: fiji-agent <skill> <action> [options]");
        System.out.println("");
        System.out.println("Examples:");
        System.out.println("  fiji-agent analyze particles --input test.tif --output ./results");
        System.out.println("  fiji-agent gaussian blur --input test.tif --sigma 2.0");
        System.out.println("  fiji-agent threshold otsu --input test.tif --method Otsu");
        System.out.println("");
        System.out.println("Options:");
        System.out.println("  --input <path>    Input image path");
        System.out.println("  --output <path>   Output directory/path");
        System.out.println("  --sigma <value>   Gaussian blur sigma");
        System.out.println("  --method <name>   Threshold method (Otsu, Li, etc.)");
    }
}
