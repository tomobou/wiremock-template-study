package net.tomobou.extensions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.List;

public class JsonNodeTransformUtil {

    public static JsonNode nodeTransform(JsonNode current, List<String> path, String operation) {
        if (current.isArray()) {
            for (Iterator<JsonNode> it = ((ArrayNode) current).elements(); it.hasNext(); ) {
                nodeTransform(it.next(), path, operation);
            }
        } else {
            if (path.size() == 1) {
                if (operation.startsWith("!DELETE!")) {
                    ((ObjectNode) current).remove(path.get(0));
                } else if (operation.startsWith("!INTEGER!")) {
                    ((ObjectNode) current).put(path.get(0), Integer.parseInt(operation.replace("!INTEGER!", "")));
                } else if (operation.startsWith("!DOUBLE!")) {
                    ((ObjectNode) current).put(path.get(0), Double.parseDouble(operation.replace("!DOUBLE!", "")));
                } else if (operation.startsWith("!ARRAY!")) {
                    var array = ((ObjectNode) current).putArray(path.get(0));
                    var addElementCount = Integer.parseInt(operation.replace("!ARRAY!", ""));
                    for (int i = 0; i < addElementCount; i++) {
                        array.addObject();
                    }
                } else {
                    ((ObjectNode) current).put(path.get(0), operation);
                }
            } else {
                var nextPath = path.subList(1, path.size());
                nodeTransform(current.get(path.get(0)), nextPath, operation);
            }
        }
        return current;
    }
}
