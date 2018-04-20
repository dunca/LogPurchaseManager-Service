package io.github.dunca.logpurchasemanager.api.route.response.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dunca.logpurchasemanager.api.route.response.exceptions.InvalidModelException;

import java.io.IOException;
import java.util.List;

public class DeserializationManager {
    private final ObjectMapper objectMapper;
    private static DeserializationManager instance;

    private DeserializationManager() {
        objectMapper = new ObjectMapper();
    }

    public static DeserializationManager getInstance() {
        if (instance == null) {
            instance = new DeserializationManager();
        }

        return instance;
    }

    public <T, R> R deserialize(String json, Class<T> modelClass, boolean isList) {
        if (json == null) {
            throw new InvalidModelException();
        }

        JavaType javaType;

        if (isList) {
            javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, modelClass);
        } else {
            javaType = objectMapper.getTypeFactory().constructType(modelClass);
        }

        try {
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            e.printStackTrace();
            throw new InvalidModelException();
        }
    }
}
