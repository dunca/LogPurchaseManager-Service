package io.github.dunca.logpurchasemanager.api.route.response.util;

import spark.Response;

public class StatusCode {
    private StatusCode() {

    }

    public static void internalServerError(Response response) {
        response.status(500);
    }

    public static void badRequest(Response response) {
        response.status(400);
    }

    public static void created(Response response) {
        response.status(201);
    }
}
