package edu.psu.processing;

import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * reads .html ticket file and builds
 *
 * format:
 *   type (Software/Hardware)
 *   title
 *   description
 *   priority (1-10)
 *
 *   Hardware-only fields (ignored for Software tickets):
 *   serial
 *   make_model
 *   location
 *   failure
 *   warranty (true/false0
 */
public class WebFormSource extends AbsTicketSource implements ParsingStrategyIF {

    public WebFormSource() {
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
            // Reuse the same Key: Value parser from EmailSource
            fields = EmailSource.parseKeyValueText(raw);
        } else if (data instanceof Map<?, ?> map) {
            fields = new java.util.HashMap<>();
            map.forEach((k, v) -> fields.put(k.toString().toLowerCase().trim(), v.toString().trim()));
        } else {
            throw new IllegalArgumentException(
                "WebFormSource expects a String or Map, got: "
                + (data == null ? "null" : data.getClass().getSimpleName()));
        }

        EmailSource.applyToBuilder(fields, builder, "Web Form");
    }
}
