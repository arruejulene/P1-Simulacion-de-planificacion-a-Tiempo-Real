package proyecto1so.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import proyecto1so.datastructures.SingleLinkedList;
import proyecto1so.model.Process;

public class ProcessLoader {

    public LoadResult loadFromJson(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return new LoadResult(new Process[0], new String[]{"JSON path is empty"});
        }

        SingleLinkedList<Process> processes = new SingleLinkedList<>();
        SingleLinkedList<String> errors = new SingleLinkedList<>();

        try {
            String content = readJsonContent(filePath);
            if (content == null) {
                errors.addLast("JSON file does not exist (filesystem/classpath): " + filePath);
                return toResult(processes, errors);
            }

            JsonElement root = JsonParser.parseString(content);
            if (!root.isJsonArray()) {
                errors.addLast("JSON root must be an array");
                return toResult(processes, errors);
            }

            JsonArray arr = root.getAsJsonArray();
            int n = arr.size();
            for (int i = 0; i < n; i++) {
                JsonElement el = arr.get(i);
                if (!el.isJsonObject()) {
                    errors.addLast("JSON item #" + i + " is not an object");
                    continue;
                }
                Process p = parseJsonObject(el.getAsJsonObject(), i, errors);
                if (p != null) processes.addLast(p);
            }

        } catch (IOException e) {
            errors.addLast("JSON read error: " + e.getMessage());
        } catch (Exception e) {
            errors.addLast("JSON parse error: " + e.getMessage());
        }

        return toResult(processes, errors);
    }

    private String readJsonContent(String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (Files.exists(path)) {
            return Files.readString(path);
        }

        String classpathPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = ProcessLoader.class.getClassLoader();

        InputStream in = cl.getResourceAsStream(classpathPath);
        if (in == null) return null;

        try (InputStream resourceStream = in) {
            byte[] bytes = resourceStream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private Process parseJsonObject(JsonObject o, int index, SingleLinkedList<String> errors) {
        String prefix = "JSON item #" + index + ": ";

        String pid = readRequiredString(o, "pid", prefix, errors);
        Integer burst = readRequiredInt(o, "burst", prefix, true, true, errors);
        Integer arrival = readRequiredInt(o, "arrival", prefix, false, true, errors);
        Integer priority = readRequiredInt(o, "priority", prefix, true, true, errors);
        Integer deadline = readOptionalDeadline(o, "deadline", prefix, errors);

        if (pid == null || burst == null || arrival == null || priority == null || deadline == null) return null;
        return new Process(pid, burst, arrival, priority, deadline);
    }

    private String readRequiredString(JsonObject o, String key, String prefix, SingleLinkedList<String> errors) {
        if (!o.has(key) || o.get(key).isJsonNull()) {
            errors.addLast(prefix + "missing required field '" + key + "'");
            return null;
        }
        try {
            String value = o.get(key).getAsString();
            if (value == null || value.isBlank()) {
                errors.addLast(prefix + "field '" + key + "' is empty");
                return null;
            }
            return value.trim();
        } catch (Exception e) {
            errors.addLast(prefix + "field '" + key + "' must be a string");
            return null;
        }
    }

    private Integer readRequiredInt(JsonObject o,
                                    String key,
                                    String prefix,
                                    boolean mustBePositive,
                                    boolean allowZero,
                                    SingleLinkedList<String> errors) {
        if (!o.has(key) || o.get(key).isJsonNull()) {
            errors.addLast(prefix + "missing required field '" + key + "'");
            return null;
        }
        try {
            int value = o.get(key).getAsInt();
            if (mustBePositive && value <= 0) {
                errors.addLast(prefix + "field '" + key + "' must be > 0");
                return null;
            }
            if (!allowZero && value == 0) {
                errors.addLast(prefix + "field '" + key + "' must not be 0");
                return null;
            }
            if (allowZero && value < 0) {
                errors.addLast(prefix + "field '" + key + "' must be >= 0");
                return null;
            }
            return value;
        } catch (Exception e) {
            errors.addLast(prefix + "field '" + key + "' must be an integer");
            return null;
        }
    }

    private Integer readOptionalDeadline(JsonObject o, String key, String prefix, SingleLinkedList<String> errors) {
        if (!o.has(key) || o.get(key).isJsonNull()) return Integer.MAX_VALUE;
        try {
            int value = o.get(key).getAsInt();
            if (value <= 0) {
                errors.addLast(prefix + "field '" + key + "' must be > 0 when present");
                return null;
            }
            return value;
        } catch (Exception e) {
            errors.addLast(prefix + "field '" + key + "' must be an integer");
            return null;
        }
    }

    private LoadResult toResult(SingleLinkedList<Process> processList, SingleLinkedList<String> errorList) {
        Process[] processes = new Process[processList.size()];
        int i = 0;
        while (!processList.isEmpty()) {
            processes[i++] = processList.removeFirst();
        }

        String[] errors = new String[errorList.size()];
        int j = 0;
        while (!errorList.isEmpty()) {
            errors[j++] = errorList.removeFirst();
        }

        return new LoadResult(processes, errors);
    }
}
