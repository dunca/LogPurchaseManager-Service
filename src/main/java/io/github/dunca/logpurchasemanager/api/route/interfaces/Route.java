package io.github.dunca.logpurchasemanager.api.route.interfaces;

import io.github.dunca.logpurchasemanager.api.dao.DatabaseHelper;

public abstract class Route implements spark.Route {
    protected DatabaseHelper dbHelper;

    public Route(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
}
