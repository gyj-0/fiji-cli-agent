package fiji.agent;

import java.util.*;

/**
 * Scheduler - Manages skill execution lifecycle.
 */
public class Scheduler {
    
    private final SkillRegistry registry;
    private final FijiBackend backend;
    
    public Scheduler(SkillRegistry registry, FijiBackend backend) {
        this.registry = registry;
        this.backend = backend;
    }
    
    /**
     * S4: Execute a parsed command using a skill.
     */
    public SkillResult executeCommand(FijiCommandParser.ParsedCommand command, 
                                       SkillRegistry.Skill skill) {
        
        if (command == null || !command.isValid()) {
            return SkillResult.error("Invalid command");
        }
        
        if (skill == null) {
            return SkillResult.error("Skill not found: " + command.getAction());
        }
        
        try {
            // Serialize parameters for macro
            String args = command.serializeForMacro();
            
            System.out.println("[INFO] Args: " + args);
            
            // Execute via backend
            String output;
            if ("macro".equals(skill.getType())) {
                output = backend.executeMacro(skill.getPath(), args);
            } else if ("jython".equals(skill.getType())) {
                Map<String, Object> params = new HashMap<>();
                params.putAll(command.getParams());
                output = backend.executeJython(skill.getPath(), params);
            } else {
                return SkillResult.error("Unknown skill type: " + skill.getType());
            }
            
            if (output == null) {
                return SkillResult.error("Execution returned null output");
            }
            
            // Parse output path from result (if applicable)
            String outputPath = extractOutputPath(output, command.getParam("output"));
            
            return SkillResult.success(outputPath, output);
            
        } catch (Exception e) {
            return SkillResult.error("Execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract output path from Fiji output or use provided default.
     */
    private String extractOutputPath(String output, String defaultOutput) {
        // Look for "Results: " or "Output: " in Fiji output
        if (output != null) {
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.contains("Results:") || line.contains("Output:")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        return parts[1].trim();
                    }
                }
            }
        }
        return defaultOutput != null ? defaultOutput : "output";
    }
    
    /**
     * SkillResult - Container for execution results.
     */
    public static class SkillResult {
        private final boolean success;
        private final String output;
        private final String errorMessage;
        private final Map<String, Object> data;
        
        private SkillResult(boolean success, String output, String errorMessage) {
            this.success = success;
            this.output = output;
            this.errorMessage = errorMessage;
            this.data = new HashMap<>();
        }
        
        public static SkillResult success(String output) {
            return new SkillResult(true, output, null);
        }
        
        public static SkillResult success(String output, String rawOutput) {
            SkillResult result = new SkillResult(true, output, null);
            result.data.put("rawOutput", rawOutput);
            return result;
        }
        
        public static SkillResult error(String message) {
            return new SkillResult(false, null, message);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getOutput() { return output; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getData() { return new HashMap<>(data); }
        
        @Override
        public String toString() {
            return success ? "SUCCESS: " + output : "ERROR: " + errorMessage;
        }
    }
    
    /**
     * Backend interface for Fiji execution.
     */
    public interface FijiBackend {
        String executeMacro(String macroPath, String args);
        String executeJython(String scriptPath, Map<String, Object> params);
    }
}
