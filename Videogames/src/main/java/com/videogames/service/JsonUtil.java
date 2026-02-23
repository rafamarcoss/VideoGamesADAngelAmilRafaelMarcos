package com.videogames.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonUtil {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        // Soporte para LocalDate, LocalDateTime, etc.
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar a JSON: " + e.getMessage(), e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Error al deserializar JSON: " + e.getMessage(), e);
        }
    }

    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json,
                mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new RuntimeException("Error al deserializar lista JSON: " + e.getMessage(), e);
        }
    }

    public static void toJsonFile(Object obj, String filepath) throws IOException {
        mapper.writeValue(new File(filepath), obj);
    }

    public static <T> T fromJsonFile(String filepath, Class<T> clazz) throws IOException {
        return mapper.readValue(new File(filepath), clazz);
    }

    public static <T> List<T> fromJsonFileList(String filepath, Class<T> clazz) throws IOException {
        return mapper.readValue(new File(filepath),
                mapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }
}
