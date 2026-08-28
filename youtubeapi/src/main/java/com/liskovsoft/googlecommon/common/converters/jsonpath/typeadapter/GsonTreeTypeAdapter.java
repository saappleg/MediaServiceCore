package com.liskovsoft.googlecommon.common.converters.jsonpath.typeadapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.liskovsoft.googlecommon.common.converters.jsonpath.JsonPath;
import com.liskovsoft.googlecommon.common.helpers.ReflectionHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Fast adapter for the simple, field-based JSON paths used by player metadata models.
 * The response is parsed into one Gson tree and every nested model reads from that tree directly.
 */
public final class GsonTreeTypeAdapter<T> {
    private final Class<?> mType;

    public GsonTreeTypeAdapter(Type type) {
        mType = (Class<?>) type;
    }

    @SuppressWarnings("unchecked")
    public T read(String json) {
        if (json == null) {
            return null;
        }

        try {
            return (T) readType(mType, new JsonParser().parse(json));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Object readType(Class<?> type, JsonElement element) {
        if (type == null || element == null || element instanceof JsonNull || !element.isJsonObject()) {
            return null;
        }

        try {
            Constructor<?> constructor = type.getConstructor();
            Object result = constructor.newInstance();
            boolean populated = false;

            for (Field field : ReflectionHelper.getAllFields(type)) {
                String[] paths = getJsonPath(field.getAnnotations());
                if (paths == null) {
                    continue;
                }

                JsonElement value = firstValue(element, paths);
                if (value == null || value instanceof JsonNull) {
                    continue;
                }

                field.setAccessible(true);
                if (setField(field, result, value)) {
                    populated = true;
                }
            }

            return populated ? result : null;
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            return null;
        }
    }

    private boolean setField(Field field, Object target, JsonElement value) throws IllegalAccessException {
        if (value.isJsonArray()) {
            Class<?> itemType = ReflectionHelper.getGenericParamType(field);
            if (itemType == null) {
                return false;
            }

            List<Object> items = new ArrayList<>();
            for (JsonElement child : value.getAsJsonArray()) {
                Object item = child.isJsonPrimitive()
                        ? primitiveValue(child.getAsJsonPrimitive(), itemType)
                        : readType(itemType, child);
                if (item != null) {
                    items.add(item);
                }
            }
            field.set(target, items);
            return true;
        }

        if (value.isJsonObject()) {
            Object nested = readType(field.getType(), value);
            if (nested == null) {
                return false;
            }
            field.set(target, nested);
            return true;
        }

        if (value.isJsonPrimitive()) {
            Object primitive = primitiveValue(value.getAsJsonPrimitive(), field.getType());
            if (primitive == null) {
                return false;
            }
            field.set(target, primitive);
            return true;
        }

        return false;
    }

    private static Object primitiveValue(JsonPrimitive value, Class<?> targetType) {
        if (targetType == String.class) return value.getAsString();
        if (targetType == boolean.class || targetType == Boolean.class) return value.getAsBoolean();
        if (targetType == int.class || targetType == Integer.class) return value.getAsInt();
        if (targetType == long.class || targetType == Long.class) return value.getAsLong();
        if (targetType == float.class || targetType == Float.class) return value.getAsFloat();
        if (targetType == double.class || targetType == Double.class) return value.getAsDouble();
        return null;
    }

    private static JsonElement firstValue(JsonElement root, String[] paths) {
        for (String path : paths) {
            JsonElement value = resolve(root, path);
            if (value != null && !(value instanceof JsonNull)) {
                return value;
            }
        }
        return null;
    }

    /** Supports object fields, numeric array indexes, and a terminal [*]. */
    private static JsonElement resolve(JsonElement root, String path) {
        if (root == null || path == null || path.isEmpty() || path.charAt(0) != '$') {
            return null;
        }

        JsonElement current = root;
        int position = 1;
        while (position < path.length()) {
            char marker = path.charAt(position);
            if (marker == '.') {
                int start = ++position;
                while (position < path.length() && path.charAt(position) != '.' && path.charAt(position) != '[') {
                    position++;
                }
                if (!current.isJsonObject() || start == position) {
                    return null;
                }
                current = current.getAsJsonObject().get(path.substring(start, position));
            } else if (marker == '[') {
                int end = path.indexOf(']', position);
                if (end < 0 || current == null || !current.isJsonArray()) {
                    return null;
                }
                String index = path.substring(position + 1, end);
                if ("*".equals(index)) {
                    if (end != path.length() - 1) {
                        return null;
                    }
                } else {
                    int itemIndex;
                    try {
                        itemIndex = Integer.parseInt(index);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    JsonArray array = current.getAsJsonArray();
                    if (itemIndex < 0 || itemIndex >= array.size()) {
                        return null;
                    }
                    current = array.get(itemIndex);
                }
                position = end + 1;
            } else {
                return null;
            }

            if (current == null || current instanceof JsonNull) {
                return null;
            }
        }

        return current;
    }

    private static String[] getJsonPath(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof JsonPath) {
                return ((JsonPath) annotation).value();
            }
        }
        return null;
    }
}
