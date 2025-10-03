package org.saved4.saved4;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.postgresql.geometric.PGpoint;
import java.io.IOException;

public class PGpointDeserializer extends JsonDeserializer<PGpoint> {
    @Override
    public PGpoint deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        System.out.println("PGpointDeserializer received: " + text); // <-- debug output

        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        text = text.replace("(", "").replace(")", "");
        String[] parts = text.split(",");
        if (parts.length != 2) {
            throw new IOException("Invalid PGpoint format: " + text);
        }

        try {
            double x = Double.parseDouble(parts[0].trim());
            double y = Double.parseDouble(parts[1].trim());
            return new PGpoint(x, y);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid number in PGpoint: " + text, e);
        }
    }
}
