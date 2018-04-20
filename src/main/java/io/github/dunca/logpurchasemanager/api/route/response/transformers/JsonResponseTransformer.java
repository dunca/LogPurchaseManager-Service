package io.github.dunca.logpurchasemanager.api.route.response.transformers;

import com.fasterxml.jackson.databind.ObjectMapper;
import spark.ResponseTransformer;

public class JsonResponseTransformer implements ResponseTransformer {
    private ObjectMapper objectMapper;

    public JsonResponseTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String render(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
