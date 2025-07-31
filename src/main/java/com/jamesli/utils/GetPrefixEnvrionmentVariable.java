package com.jamesli.utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetPrefixEnvrionmentVariable {
    private static final Logger LOGGER = Logger.getLogger(GetPrefixEnvrionmentVariable.class.getName());
    private String prefix;

    public String getPrefix() {
        return prefix;
    }

    public GetPrefixEnvrionmentVariable(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be null or empty");
        }
        this.prefix = prefix.toLowerCase().trim();
    }

    public JSONObject readEnvironmentVariables(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        return readProperties(filePath, this.prefix);
    }

    public static JSONObject readProperties(String filePath, String prefix){
        validateInputs(filePath, prefix);
        Path path = Paths.get(filePath);

        if (!Files.exists(path)){
            LOGGER.warning("File Does not exist: " + filePath);
            return new JSONObject();
        }
        Map<String, String> properties = new HashMap<>();
        String normalizedPrefix = prefix.toLowerCase().trim();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                processLine(line, normalizedPrefix, properties, lineNumber);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading file: " + filePath, e);
            throw new RuntimeException("Failed to read configuration file", e);
        }

        return new JSONObject(properties);

    }

    private static void processLine(String line, String prefix, Map<String, String> properties, int lineNumber) {
        String trimmedLine = line.trim();

        // Skip empty lines and comments
        if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
            return;
        }

        // Look for export statements: export KEY=value or KEY=value
        String processedLine = trimmedLine;
        if (trimmedLine.startsWith("export ")) {
            processedLine = trimmedLine.substring(7).trim();
        }

        // Parse key=value pairs
        int equalsIndex = processedLine.indexOf('=');
        if (equalsIndex > 0 && equalsIndex < processedLine.length() - 1) {
            String key = processedLine.substring(0, equalsIndex).trim().toLowerCase();
            String value = processedLine.substring(equalsIndex + 1).trim();

            // Remove quotes if present
            value = removeQuotes(value);

            if (key.startsWith(prefix)) {
                properties.put(key, value);
                LOGGER.fine("Found matching variable: " + key + " at line " + lineNumber);
            }
        }
    }

    private static void validateInputs(String filePath, String prefix) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be null or empty");
        }
    }

    private static String removeQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    public static void main(String[] args) {
        String homeDir = System.getProperty("user.home");
        JSONObject result = readProperties(homeDir + "/.zshrc", "postgres");
        System.out.println(result.toString(4));
    }
}
