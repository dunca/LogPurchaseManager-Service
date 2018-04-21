package io.github.dunca.logpurchasemanager.api.route;

import com.j256.ormlite.dao.RuntimeExceptionDao;
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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AggregationRoute extends Route {
    public static final Logger L = Logger.getLogger(AggregationRoute.class.getName());

    private final RuntimeExceptionDao<Acquisition, Integer> acquisitionDao;
    private final RuntimeExceptionDao<AcquisitionItem, Integer> acquisitionItemDao;
    private final RuntimeExceptionDao<LogPrice, Integer> logPriceDao;

    private Map<Integer, Acquisition> originalIdToAcquisitionMap;

    private static final String statisticsMessageTemplate = "Inserted %d new %s instances and updated %d existing ones";

    public AggregationRoute(DatabaseHelper dbHelper) {
        super(dbHelper);

        acquisitionDao = dbHelper.getAcquisitionDao();
        acquisitionItemDao = dbHelper.getAcquisitionItemDao();
        logPriceDao = dbHelper.getLogPriceDao();

        originalIdToAcquisitionMap = new HashMap<>();
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (request.requestMethod().equals(RequestMethods.POST)) {
            return handlePost(request, response);
        }

        throw new UnsupportedHttpMethodException();
    }

    private FullAggregation handlePost(Request request, Response response) {
        FullAggregation aggregation = DeserializationManager.getInstance().deserialize(request.body(),
                FullAggregation.class, false);

        int newAcquisitionCount = 0;
        int updatedAcquisitionCount = 0;
        for (Acquisition acquisition : aggregation.getAcquisitionList()) {
            if (wasPreviouslySynced(acquisition)) {
                /*
                this Acquisition was previously persisted in the local db, simply update its id, to the one originally
                given to it by the local db. If we don't do this, we'll update the wrong instance, since ids on the local
                db could be different from those in the app
                */
                acquisition.setId(acquisition.getServerAllocatedId());
                acquisitionDao.update(acquisition);

                updatedAcquisitionCount++;
            } else {
                /*
                 this Acquisition hasn't been persisted in the local db yet. Persist it as is, then update its
                 server allocated id property, so that we can later update it. Also store it using its original id in
                 a map. The map is queried later, when we try to persist AcquisitionItem and LogPrice instances that have
                 never been persisted, as we need to update their Acquisition reference before we create them
                 */
                originalIdToAcquisitionMap.put(acquisition.getId(), acquisition);

                acquisitionDao.create(acquisition);
                acquisition.setServerAllocatedId(acquisition.getId());

                newAcquisitionCount++;
            }
        }

        L.info(String.format(statisticsMessageTemplate, newAcquisitionCount, Acquisition.class.getSimpleName(), updatedAcquisitionCount));


        int newAcquisitionItemCount = 0;
        int updatedAcquisitionItemCount = 0;
        for (AcquisitionItem acquisitionItem : aggregation.getAcquisitionItemList()) {
            if (wasPreviouslySynced(acquisitionItem)) {
                Acquisition acquisition = getAcquisitionByAppAllocatedId(acquisitionItem.getAcquisition().getId());
                acquisitionItem.setAcquisition(acquisition);

                acquisitionItem.setId(acquisitionItem.getServerAllocatedId());

                acquisitionItemDao.update(acquisitionItem);

                updatedAcquisitionItemCount++;
            } else {
                Acquisition acquisition = getAcquisitionByOriginalId(acquisitionItem.getAcquisition().getId());
                acquisitionItem.setAcquisition(acquisition);
                acquisitionItemDao.create(acquisitionItem);

                acquisitionItem.setServerAllocatedId(acquisitionItem.getId());

                newAcquisitionItemCount++;
            }
        }

        L.info(String.format(statisticsMessageTemplate, newAcquisitionItemCount, AcquisitionItem.class.getSimpleName(), updatedAcquisitionItemCount));


        int newLogPriceCount = 0;
        int updatedLogPriceCount = 0;
        for (LogPrice logPrice : aggregation.getLogPriceList()) {
            if (wasPreviouslySynced(logPrice)) {
                Acquisition acquisition = getAcquisitionByAppAllocatedId(logPrice.getAcquisition().getId());
                logPrice.setAcquisition(acquisition);

                logPrice.setId(logPrice.getServerAllocatedId());

                logPriceDao.update(logPrice);

                updatedLogPriceCount++;
            } else {
                Acquisition acquisition = getAcquisitionByOriginalId(logPrice.getAcquisition().getId());
                logPrice.setAcquisition(acquisition);
                logPriceDao.create(logPrice);

                logPrice.setServerAllocatedId(logPrice.getId());

                newLogPriceCount++;
            }
        }

        L.info(String.format(statisticsMessageTemplate, newLogPriceCount, LogPrice.class.getSimpleName(), updatedLogPriceCount));

        originalIdToAcquisitionMap.clear();

        return aggregation;
    }

    private Acquisition getAcquisitionByAppAllocatedId(int id) {
        return dbHelper.getAcquisitionDao().queryForEq(CommonFieldNames.APP_ALLOCATED_ID, id).get(0);
    }

    private boolean wasPreviouslySynced(Acquisition acquisition) {
        return acquisition.getServerAllocatedId() > 0;
    }

    private boolean wasPreviouslySynced(AcquisitionItem acquisitionItem) {
        return acquisitionItem.getServerAllocatedId() > 0;
    }

    private boolean wasPreviouslySynced(LogPrice logPrice) {
        return logPrice.getServerAllocatedId() > 0;
    }

    private Acquisition getAcquisitionByOriginalId(int id) {
        if (originalIdToAcquisitionMap.containsKey(id)) {
            return originalIdToAcquisitionMap.get(id);
        }

        throw new IllegalStateException(String.format("Found no acquisition with id %d", id));
    }
}
