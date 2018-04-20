package io.github.dunca.logpurchasemanager.api.route;

import io.github.dunca.logpurchasemanager.api.dao.DatabaseHelper;
import io.github.dunca.logpurchasemanager.api.route.response.exceptions.UnsupportedHttpMethodException;
import io.github.dunca.logpurchasemanager.api.route.constants.RequestMethods;
import io.github.dunca.logpurchasemanager.api.route.interfaces.Route;
import io.github.dunca.logpurchasemanager.shared.model.Acquirer;
import io.github.dunca.logpurchasemanager.shared.model.LogQualityClass;
import io.github.dunca.logpurchasemanager.shared.model.Supplier;
import io.github.dunca.logpurchasemanager.shared.model.TreeSpecies;
import io.github.dunca.logpurchasemanager.shared.model.WoodCertification;
import io.github.dunca.logpurchasemanager.shared.model.WoodRegion;
import io.github.dunca.logpurchasemanager.shared.model.custom.StaticData;
import spark.Request;
import spark.Response;

import java.util.List;

public class StaticDataRoute extends Route {
    public StaticDataRoute(DatabaseHelper dbHelper) {
        super(dbHelper);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (request.requestMethod().equals(RequestMethods.GET)) {
            return handleGet(request, response);
        }

        throw new UnsupportedHttpMethodException();
    }

    private Object handleGet(Request request, Response response) {
        List<Acquirer> acquirers = dbHelper.getAcquirerDao().queryForAll();
        List<LogQualityClass> logQualityClasses = dbHelper.getLogQualityClassDao().queryForAll();
        List<Supplier> suppliers = dbHelper.getSupplierDao().queryForAll();
        List<TreeSpecies> treeSpecies = dbHelper.getTreeSpeciesDao().queryForAll();
        List<WoodCertification> woodCertifications = dbHelper.getWoodCertificationDao().queryForAll();
        List<WoodRegion> woodRegions = dbHelper.getWoodRegionDao().queryForAll();

        return new StaticData(acquirers, logQualityClasses, suppliers, treeSpecies, woodCertifications, woodRegions);
    }
}
