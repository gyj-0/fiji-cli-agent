package fiji.agent;

import java.util.*;

/**
 * FijiCommandParser - Parses CLI arguments into structured commands.
 * Source: Doc 4.1.1
 */
public class FijiCommandParser {
    
    // Command aliases mapping
    private static final Map<String, String> ALIASES = new HashMap<>();
    
    static {
        // Basic processing
        ALIASES.put("open", "open_image");
        ALIASES.put("blur", "gaussian_blur");
        ALIASES.put("smooth", "gaussian_blur");
        ALIASES.put("contrast", "adjust_contrast");
        ALIASES.put("scale", "scale_rotate");
        ALIASES.put("rotate", "scale_rotate");
        ALIASES.put("crop", "scale_rotate");
        
        // Analysis
        ALIASES.put("threshold", "threshold_otsu");
        ALIASES.put("segment", "threshold_otsu");
        ALIASES.put("particles", "analyze_particles");
        ALIASES.put("count", "analyze_particles");
        ALIASES.put("zproject", "z_project");
        ALIASES.put("coloc", "colocalization");
        
        // Advanced
        ALIASES.put("register", "bunwarpj_register");
        ALIASES.put("track", "trackmate_batch");
        ALIASES.put("cellpose", "cellpose_segment");
    }
    
    /**
     * Parse command line arguments.
     * Format: fiji-agent <skill> <action> [options]
     * Example: fiji-agent analyze particles --input xxx.tif --output ./results
     */
    public ParsedCommand parse(String[] args) {
        if (args == null || args.length < 1) {
            return ParsedCommand.error("No command provided. Usage: fiji-agent <skill> <action> [options]");
        }
        
        // First tokens form the skill name (e.g., "analyze particles")
        StringBuilder skillBuilder = new StringBuilder();
        int paramStartIndex = 0;
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            // Stop at first flag
            if (arg.startsWith("--")) {
                paramStartIndex = i;
                break;
            }
            
            // Build skill name
            if (skillBuilder.length() > 0) {
                skillBuilder.append("_");
            }
            skillBuilder.append(arg.toLowerCase().replace("-", "_"));
            paramStartIndex = i + 1;
        }
        
        String skillName = skillBuilder.toString();
        
        // Resolve aliases
        String resolvedSkill = ALIASES.getOrDefault(skillName, skillName);
        
        // Parse parameters (--key value)
        Map<String, String> params = new HashMap<>();
        for (int i = paramStartIndex; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.startsWith("--")) {
                String key = arg.substring(2);
                String value = "";
                
                // Check if next arg is a value (not a flag)
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    value = args[i + 1];
                    i++; // Skip the value in next iteration
                }
                
                params.put(key, value);
            }
        }
        
        return new ParsedCommand(resolvedSkill, skillName, params);
    }
    
    /**
     * Structured command representation.
     * S1: Domain model with complete fields and methods.
     */
    public static class ParsedCommand {
        private final String action;      // Resolved skill name (e.g., "analyze_particles")
        private final String original;    // Original command (e.g., "analyze particles")
        private final Map<String, String> params;
        private final boolean valid;
        private final String errorMessage;
        
        public ParsedCommand(String action, String original, Map<String, String> params) {
            this.action = action;
            this.original = original;
            this.params = new HashMap<>(params);
            this.valid = true;
            this.errorMessage = null;
        }
        
        private ParsedCommand(String errorMessage) {
            this.action = null;
            this.original = null;
            this.params = new HashMap<>();
            this.valid = false;
            this.errorMessage = errorMessage;
        }
        
        public static ParsedCommand error(String message) {
            return new ParsedCommand(message);
        }
        
        // Getters
        public String getAction() { return action; }
        public String getOriginal() { return original; }
        public Map<String, String> getParams() { return new HashMap<>(params); }
        public String getParam(String key) { return params.get(key); }
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        
        /**
         * Serialize params for Fiji macro (comma-separated).
         * Order: input, output, then other params.
         */
        public String serializeForMacro() {
            StringBuilder sb = new StringBuilder();
            
            // First: input path
            if (params.containsKey("input")) {
                sb.append(params.get("input"));
            }
            
            // Second: output path
            if (params.containsKey("output")) {
                if (sb.length() > 0) sb.append(",");
                sb.append(params.get("output"));
            }
            
            // Then other params in order
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                if (!key.equals("input") && !key.equals("output")) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(entry.getValue());
                }
            }
            
            return sb.toString();
        }
        
        @Override
        public String toString() {
            return "ParsedCommand{action='" + action + "', params=" + params + "}";
        }
    }
    
    public static void main(String[] args) {
        FijiCommandParser parser = new FijiCommandParser();
        
        // Test parsing
        String[] testArgs = {"analyze", "particles", "--input", "test.tif", "--output", "./results"};
        ParsedCommand cmd = parser.parse(testArgs);
        
        System.out.println("Parsed: " + cmd);
        System.out.println("Action: " + cmd.getAction());
        System.out.println("Input: " + cmd.getParam("input"));
        System.out.println("Output: " + cmd.getParam("output"));
        System.out.println("Serialized: " + cmd.serializeForMacro());
    }
}
