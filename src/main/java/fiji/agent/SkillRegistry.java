package fiji.agent;

import java.util.*;
import java.nio.file.*;
import java.io.*;

/**
 * SkillRegistry - Manages skill registration and discovery.
 * Source: Doc 4.1.2 - Skill scheduler with dependency graph
 * 
 * Contract:
 * - Read-only metadata registry, does NOT execute skills
 * - Single-threaded, non-thread-safe
 * - Logs errors, does not throw exceptions
 */
public class SkillRegistry {
    
    private final Map<String, Skill> skills;
    private final Map<String, List<String>> dependencyGraph;
    
    public SkillRegistry() {
        this.skills = new HashMap<>();
        this.dependencyGraph = new HashMap<>();
    }
    
    /**
     * Register a skill with its metadata.
     */
    public void register(Skill skill) {
        if (skill == null || skill.getName() == null) {
            System.err.println("Error: Cannot register null skill or skill with null name");
            return;
        }
        skills.put(skill.getName(), skill);
        
        // Build dependency graph entry
        if (skill.getDependencies() != null && !skill.getDependencies().isEmpty()) {
            dependencyGraph.put(skill.getName(), new ArrayList<>(skill.getDependencies()));
        }
    }
    
    /**
     * Scan skills directory and auto-register all skills.
     * S2a: Directory traversal
     * S2b: Header parsing
     */
    public void scanSkillsDirectory(String path) {
        try {
            Path startPath = Paths.get(path);
            if (!Files.exists(startPath)) {
                System.err.println("Error: Skills directory does not exist: " + path);
                return;
            }
            
            // S2a: Directory traversal - find all .ijm and .py files
            List<Path> skillFiles = new ArrayList<>();
            Files.walk(startPath)
                .filter(Files::isRegularFile)
                .filter(p -> {
                    String name = p.toString().toLowerCase();
                    return name.endsWith(".ijm") || name.endsWith(".py");
                })
                .forEach(skillFiles::add);
            
            System.out.println("Found " + skillFiles.size() + " skill files");
            
            // S2b: Parse headers and register
            for (Path file : skillFiles) {
                try {
                    Skill skill = parseSkillHeader(file);
                    if (skill != null) {
                        register(skill);
                        System.out.println("Registered: " + skill.getName() + " (" + skill.getType() + ")");
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Failed to parse " + file + ": " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error scanning directory: " + e.getMessage());
        }
    }
    
    /**
     * S2b: Parse skill header from file.
     * Extracts: Skill name, description, params, dependencies from comments.
     */
    private Skill parseSkillHeader(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        String skillName = fileName.substring(0, fileName.lastIndexOf('.'));
        String type = fileName.endsWith(".ijm") ? "macro" : "jython";
        
        String description = "";
        List<String> dependencies = new ArrayList<>();
        Map<String, String> params = new LinkedHashMap<>();
        
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Parse IJM style comments: // Skill: name
                if (line.startsWith("// Skill:") || line.startsWith("# Skill:")) {
                    String extractedName = extractValue(line);
                    if (!extractedName.isEmpty()) {
                        skillName = extractedName;
                    }
                }
                // Parse Description
                else if (line.startsWith("// Description:") || line.startsWith("# Description:")) {
                    description = extractValue(line);
                }
                // Parse Dependencies
                else if (line.startsWith("// Dependencies:") || line.startsWith("# Dependencies:")) {
                    String deps = extractValue(line);
                    if (!deps.isEmpty() && !deps.equals("None")) {
                        dependencies.addAll(Arrays.asList(deps.split(",\\s*")));
                    }
                }
                // Parse Params (simplified)
                else if (line.startsWith("// Params:") || line.startsWith("# Params:")) {
                    String paramLine = extractValue(line);
                    if (!paramLine.isEmpty()) {
                        // Simple param extraction, can be enhanced
                        String[] parts = paramLine.split("-");
                        if (parts.length >= 2) {
                            params.put(parts[0].trim(), parts[1].trim());
                        }
                    }
                }
                // Stop parsing after header section (empty line or non-comment)
                else if (line.isEmpty() || (!line.startsWith("//") && !line.startsWith("#"))) {
                    if (!description.isEmpty()) {
                        break; // End of header section
                    }
                }
            }
        }
        
        // Only create skill if we found a valid header
        if (description.isEmpty() && dependencies.isEmpty()) {
            // Fall back to filename as skill name
            description = "Skill from " + fileName;
        }
        
        return new Skill(skillName, type, file.toString(), description, params, dependencies);
    }
    
    private String extractValue(String line) {
        int colonIndex = line.indexOf(':');
        if (colonIndex > 0 && colonIndex < line.length() - 1) {
            return line.substring(colonIndex + 1).trim();
        }
        return "";
    }
    
    /**
     * Get skill by name.
     */
    public Skill getSkill(String name) {
        return skills.get(name);
    }
    
    /**
     * Get all registered skill names.
     */
    public Set<String> getSkillNames() {
        return new HashSet<>(skills.keySet());
    }
    
    /**
     * S4a: Get dependencies for a skill.
     */
    public List<String> getDependencies(String skillName) {
        List<String> deps = dependencyGraph.get(skillName);
        return deps != null ? new ArrayList<>(deps) : new ArrayList<>();
    }
    
    /**
     * S4b: FROZEN - Topological sort not implemented in MVP.
     * Reserved for future use when Scheduler needs execution order.
     */
    public List<String> getExecutionOrder(String targetSkill) {
        // FROZEN: Implement when Scheduler requires dependency-ordered execution
        // This would use Kahn's algorithm or DFS-based topological sort
        // with cycle detection.
        System.err.println("Warning: getExecutionOrder() is FROZEN in MVP. " +
                          "Use getDependencies() to build order manually.");
        return Collections.emptyList();
    }
    
    /**
     * Inner class representing a skill.
     * Immutable - all fields are final.
     */
    public static class Skill {
        private final String name;
        private final String type;  // "macro", "jython", "plugin"
        private final String path;
        private final String description;
        private final Map<String, String> params;
        private final List<String> dependencies;
        
        public Skill(String name, String type, String path, String description,
                     Map<String, String> params, List<String> dependencies) {
            this.name = Objects.requireNonNull(name, "Skill name cannot be null");
            this.type = type != null ? type : "unknown";
            this.path = path != null ? path : "";
            this.description = description != null ? description : "";
            this.params = params != null ? new LinkedHashMap<>(params) : new LinkedHashMap<>();
            this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
        public String getPath() { return path; }
        public String getDescription() { return description; }
        public Map<String, String> getParams() { return new LinkedHashMap<>(params); }
        public List<String> getDependencies() { return new ArrayList<>(dependencies); }
        
        @Override
        public String toString() {
            return "Skill{name='" + name + "', type='" + type + "', deps=" + dependencies + "}";
        }
    }
    
    /**
     * S5: Main method for testing.
     */
    public static void main(String[] args) {
        SkillRegistry registry = new SkillRegistry();
        
        // Test scanning
        String skillsPath = "src/main/resources/skills";
        System.out.println("Scanning skills directory: " + skillsPath);
        registry.scanSkillsDirectory(skillsPath);
        
        // Test retrieval
        System.out.println("\nRegistered skills:");
        for (String name : registry.getSkillNames()) {
            Skill skill = registry.getSkill(name);
            System.out.println("  - " + skill);
        }
        
        // Test dependency lookup
        Skill analyzeParticles = registry.getSkill("analyze_particles");
        if (analyzeParticles != null) {
            System.out.println("\nDependencies for 'analyze_particles':");
            System.out.println("  " + registry.getDependencies("analyze_particles"));
        }
    }
}
