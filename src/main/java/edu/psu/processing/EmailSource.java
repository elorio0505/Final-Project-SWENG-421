package edu.psu.processing;

import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
/**
 * reads .eml ticket file and builds
 * format:

 *   type (hardware/software)
 *   title
 *   description
 *   priority(1-10)
 *
 *   Hardware-only fields (ignored for Software tickets):
 *   serial
 *   make_model
 *   location
 *   failure
 *   warranty (true/false)
 */
public class EmailSource extends AbsTicketSource implements ParsingStrategyIF {

    public EmailSource() {
        super(null);
        this.setStrategy(this);
    }

    public void loadFromFile(Path filePath, AbsTicketBuilder builder) throws IOException {
        String raw = Files.readString(filePath);
        parseRaw(raw, builder);
    }
        
    @Override
    public void parseRaw(Object data, AbsTicketBuilder builder) {
        Map<String, String> fields;

        if (data instanceof String raw) {
            fields = parseKeyValueText(raw);
        } else if (data instanceof Map<?, ?> map) {
            fields = new HashMap<>();
            map.forEach((k, v) -> fields.put(k.toString().toLowerCase().trim(), v.toString().trim()));
        } else {
            throw new IllegalArgumentException(
                "EmailSource expects a String or Map, got: "
                + (data == null ? "null" : data.getClass().getSimpleName()));
        }

        applyToBuilder(fields, builder, "Email");
    }
    
    static Map<String, String> parseKeyValueText(String raw) {
        Map<String, String> fields = new HashMap<>();
        for (String line : raw.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            int colon = trimmed.indexOf(':');
            if (colon <= 0) continue;
            String key   = trimmed.substring(0, colon).trim().toLowerCase();
            String value = trimmed.substring(colon + 1).trim();
            fields.put(key, value);
        }
        return fields;
    }
    
    static void applyToBuilder(Map<String, String> fields, AbsTicketBuilder builder, String sourceName) {
        String title       = require(fields, "title",       sourceName);
        String description = require(fields, "description", sourceName);
        int    priority    = requireInt(fields, "priority", sourceName);
        String type        = fields.getOrDefault("type", "Software").trim();

        builder.setBasics(title, description);
        builder.setMetaData(sourceName, priority);

        if ("Hardware".equalsIgnoreCase(type) && builder instanceof HardwareTicketBuilder hwb) {
            hwb.setHardwareDetails(
                fields.getOrDefault("serial",     ""),
                fields.getOrDefault("make_model", ""),
                fields.getOrDefault("location",   ""),
                fields.getOrDefault("failure",    ""),
                Boolean.parseBoolean(fields.getOrDefault("warranty", "false"))
            );
        }
    }
    
    // Helpers
    static String require(Map<String, String> fields, String key, String source) {
        String val = fields.get(key);
        if (val == null || val.isBlank())
            throw new IllegalArgumentException(source + " file is missing required field: \"" + key + "\"");
        return val.trim();
    }

    static int requireInt(Map<String, String> fields, String key, String source) {
        String val = require(fields, key, source);
        try {
            int n = Integer.parseInt(val);
            return Math.max(1, Math.min(10, n));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(source + " field \"" + key + "\" must be a number 1–10, got: " + val);
        }
    }
}