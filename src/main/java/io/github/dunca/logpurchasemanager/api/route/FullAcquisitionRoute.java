package io.github.dunca.logpurchasemanager.api.route;

import io.github.dunca.logpurchasemanager.api.dao.DatabaseHelper;
import io.github.dunca.logpurchasemanager.api.route.constants.RequestMethods;
import io.github.dunca.logpurchasemanager.api.route.interfaces.Route;
import io.github.dunca.logpurchasemanager.api.route.response.exceptions.UnsupportedHttpMethodException;
import io.github.dunca.logpurchasemanager.api.route.response.util.StatusCode;
import io.github.dunca.logpurchasemanager.shared.model.Acquisition;
import io.github.dunca.logpurchasemanager.shared.model.AcquisitionItem;
import io.github.dunca.logpurchasemanager.shared.model.LogPrice;
import io.github.dunca.logpurchasemanager.shared.model.custom.FullAcquisition;
import spark.Request;
import spark.Response;

import java.util.List;

public class FullAcquisitionRoute extends Route {
    public FullAcquisitionRoute(DatabaseHelper databaseHelper) {
        super(databaseHelper);
    }

    @Override
    public Object handle(Request request, Response response) {
        if (request.requestMethod().equals(RequestMethods.POST)) {
            return handlePost(request, response);
        }

        throw new UnsupportedHttpMethodException();
    }

    private Object handlePost(Request request, Response response) {

        List<FullAcquisition> fullAcquisitionList
                = DeserializationManager.getInstance().deserialize(request.body(), FullAcquisition.class, true);

        for (FullAcquisition fullAcquisition : fullAcquisitionList) {
            persistFullAcquisition(fullAcquisition);
        }

        StatusCode.created(response);
        return fullAcquisitionList;
    }

    private void persistFullAcquisition(FullAcquisition fullAcquisition) {
        Acquisition acquisition = fullAcquisition.getAcquisition();
        List<AcquisitionItem> acquisitionItemList = fullAcquisition.getAcquisitionItemList();
        List<LogPrice> logPriceList = fullAcquisition.getLogPriceList();

        // update the reference, since deserialization creates new objects for each reference
        for (AcquisitionItem acquisitionItem : acquisitionItemList) {
            acquisitionItem.setAcquisition(acquisition);
        }

        for (LogPrice logPrice : logPriceList) {
            logPrice.setAcquisition(acquisition);
        }

        dbHelper.getAcquisitionDao().create(acquisition);
        dbHelper.getAcquisitionItemDao().create(acquisitionItemList);
        dbHelper.getLogPriceDao().create(logPriceList);
    }
}
