package io.github.dunca.logpurchasemanager.api.route.response.exceptions.util;

import lombok.Data;

@Data
public class ResponseError {
    private final String exceptionClassName;
    private final String exceptionMessage;

    public ResponseError(RuntimeException exception) {
        exceptionClassName = exception.getClass().getSimpleName();
        exceptionMessage = exception.getMessage();
    }
}
