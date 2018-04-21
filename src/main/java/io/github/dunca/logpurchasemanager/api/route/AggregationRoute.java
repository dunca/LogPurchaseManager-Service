package io.github.dunca.logpurchasemanager.api.route;

import io.github.dunca.logpurchasemanager.api.dao.DatabaseHelper;
import io.github.dunca.logpurchasemanager.api.route.constants.RequestMethods;
import io.github.dunca.logpurchasemanager.api.route.interfaces.Route;
import io.github.dunca.logpurchasemanager.api.route.response.exceptions.UnsupportedHttpMethodException;
import io.github.dunca.logpurchasemanager.api.route.response.util.DeserializationManager;
import io.github.dunca.logpurchasemanager.shared.model.Acquisition;
import io.github.dunca.logpurchasemanager.shared.model.AcquisitionItem;
import io.github.dunca.logpurchasemanager.shared.model.LogPrice;
import io.github.dunca.logpurchasemanager.shared.model.constants.CommonFieldNames;
import io.github.dunca.logpurchasemanager.shared.model.custom.FullAggregation;
import spark.Request;
import spark.Response;

public class AggregationRoute extends Route {
    public AggregationRoute(DatabaseHelper databaseHelper) {
        super(databaseHelper);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (request.requestMethod().equals(RequestMethods.POST)) {
            return handlePost(request, response);
        }

        throw new UnsupportedHttpMethodException();
    }

    private FullAggregation handlePost(Request request, Response response) {
        FullAggregation aggregation = DeserializationManager.getInstance().deserialize(request.body(), FullAggregation.class, false);

        for (Acquisition acquisition : aggregation.getAcquisitionList()) {
            acquisition.setId(acquisition.getServerAllocatedId());
            dbHelper.getAcquisitionDao().update(acquisition);
        }

        for (AcquisitionItem acquisitionItem : aggregation.getAcquisitionItemList()) {
            acquisitionItem.setId(acquisitionItem.getServerAllocatedId());

            // update the acquisition reference so that it's id matches the one on the server
            Acquisition acquisition = getAcquisitionByAppAllocatedId(acquisitionItem.getAcquisition().getId());
            acquisitionItem.setAcquisition(acquisition);

            dbHelper.getAcquisitionItemDao().update(acquisitionItem);
        }

        for (LogPrice logPrice : aggregation.getLogPriceList()) {
            logPrice.setId(logPrice.getServerAllocatedId());

            // update the acquisition reference so that it's id matches the one on the server
            Acquisition acquisition = getAcquisitionByAppAllocatedId(logPrice.getAcquisition().getId());
            logPrice.setAcquisition(acquisition);

            dbHelper.getLogPriceDao().update(logPrice);
        }

        return aggregation;
    }

    private Acquisition getAcquisitionByAppAllocatedId(int id) {
        return dbHelper.getAcquisitionDao().queryForEq(CommonFieldNames.APP_ALLOCATED_ID, id).get(0);
    }
}
