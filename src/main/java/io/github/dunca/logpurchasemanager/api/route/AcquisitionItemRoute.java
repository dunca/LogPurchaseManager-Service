package io.github.dunca.logpurchasemanager.api.route;

import io.github.dunca.logpurchasemanager.api.dao.DatabaseHelper;
import io.github.dunca.logpurchasemanager.api.route.interfaces.Route;
import spark.Request;
import spark.Response;

public class AcquisitionItemRoute extends Route {
    public AcquisitionItemRoute(DatabaseHelper databaseHelper) {
        super(databaseHelper);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        return null;
    }
}
