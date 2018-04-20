package io.github.dunca.logpurchasemanager.api.dao;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import io.github.dunca.logpurchasemanager.shared.model.Acquirer;
import io.github.dunca.logpurchasemanager.shared.model.Acquisition;
import io.github.dunca.logpurchasemanager.shared.model.AcquisitionItem;
import io.github.dunca.logpurchasemanager.shared.model.LogPrice;
import io.github.dunca.logpurchasemanager.shared.model.LogQualityClass;
import io.github.dunca.logpurchasemanager.shared.model.Supplier;
import io.github.dunca.logpurchasemanager.shared.model.TreeSpecies;
import io.github.dunca.logpurchasemanager.shared.model.WoodCertification;
import io.github.dunca.logpurchasemanager.shared.model.WoodRegion;
import io.github.dunca.logpurchasemanager.shared.model.interfaces.Model;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseHelper {
    private Logger logger = Logger.getLogger(DatabaseHelper.class.getName());

    private final ConnectionSource connectionSource;

    private RuntimeExceptionDao<Acquirer, Integer> acquirerDao;
    private RuntimeExceptionDao<WoodRegion, Integer> woodRegionDao;
    private RuntimeExceptionDao<TreeSpecies, Integer> woodSpeciesDao;
    private RuntimeExceptionDao<WoodCertification, Integer> woodCertificationDao;
    private RuntimeExceptionDao<LogQualityClass, Integer> logQualityClassDao;
    private RuntimeExceptionDao<Supplier, Integer> supplierDao;
    private RuntimeExceptionDao<Acquisition, Integer> acquisitionDao;
    private RuntimeExceptionDao<AcquisitionItem, Integer> acquisitionItemDao;
    private RuntimeExceptionDao<LogPrice, Integer> logPriceDao;

    public DatabaseHelper(String databaseUrl) throws SQLException {
        connectionSource = new JdbcConnectionSource(databaseUrl);
    }

    public RuntimeExceptionDao<Acquirer, Integer> getAcquirerDao() {
        if (acquirerDao == null) {
            acquirerDao = getDao(Acquirer.class);
        }

        return acquirerDao;
    }

    public RuntimeExceptionDao<WoodRegion, Integer> getWoodRegionDao() {
        if (woodRegionDao == null) {
            woodRegionDao = getDao(WoodRegion.class);
        }

        return woodRegionDao;
    }

    public RuntimeExceptionDao<TreeSpecies, Integer> getTreeSpeciesDao() {
        if (woodSpeciesDao == null) {
            woodSpeciesDao = getDao(TreeSpecies.class);
        }

        return woodSpeciesDao;
    }

    public RuntimeExceptionDao<WoodCertification, Integer> getWoodCertificationDao() {
        if (woodCertificationDao == null) {
            woodCertificationDao = getDao(WoodCertification.class);
        }

        return woodCertificationDao;
    }

    public RuntimeExceptionDao<LogQualityClass, Integer> getLogQualityClassDao() {
        if (logQualityClassDao == null) {
            logQualityClassDao = getDao(LogQualityClass.class);
        }

        return logQualityClassDao;
    }

    public RuntimeExceptionDao<Supplier, Integer> getSupplierDao() {
        if (supplierDao == null) {
            supplierDao = getDao(Supplier.class);
        }

        return supplierDao;
    }

    public RuntimeExceptionDao<Acquisition, Integer> getAcquisitionDao() {
        if (acquisitionDao == null) {
            acquisitionDao = getDao(Acquisition.class);
        }

        return acquisitionDao;
    }

    public RuntimeExceptionDao<AcquisitionItem, Integer> getAcquisitionItemDao() {
        if (acquisitionItemDao == null) {
            acquisitionItemDao = getDao(AcquisitionItem.class);
        }

        return acquisitionItemDao;
    }

    public RuntimeExceptionDao<LogPrice, Integer> getLogPriceDao() {
        if (logPriceDao == null) {
            logPriceDao = getDao(LogPrice.class);
        }

        return logPriceDao;
    }

    /**
     * Creates tables based on model classes
     *
     * @throws SQLException if an underlying SQL related error occurs
     */
    public void createDatabaseTables() throws java.sql.SQLException {
        createDatabaseTable(Acquirer.class);
        createDatabaseTable(Acquisition.class);
        createDatabaseTable(AcquisitionItem.class);
        createDatabaseTable(LogPrice.class);
        createDatabaseTable(LogQualityClass.class);
        createDatabaseTable(Supplier.class);
        createDatabaseTable(TreeSpecies.class);
        createDatabaseTable(WoodCertification.class);
        createDatabaseTable(WoodRegion.class);
    }

    private <T extends Model> void createDatabaseTable(Class<T> modelClass) throws SQLException {
        TableUtils.createTable(connectionSource, modelClass);
    }

    private <T extends Model> RuntimeExceptionDao<T, Integer> getDao(Class<T> modelClass) {
        RuntimeExceptionDao<T, Integer> dao = null;
        
        try {
            dao = RuntimeExceptionDao.createDao(connectionSource, modelClass);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Could not create DAO instance...", e);
            System.exit(-1);
        }

        return dao;
    }

    public void initStaticData() {
        createAcquirers();
        createWoodRegions();
        createTreeSpecies();
        createWoodCertifications();
        createLogQualityClasses();
        createSuppliers();
    }

    private void createAcquirers() {
        Acquirer acquirer1 = new Acquirer("BE", "Eduard", "Banu", "1");
        Acquirer acquirer2 = new Acquirer("MT", "Teodor", "Moldovan", "1");

        getAcquirerDao().create(Arrays.asList(acquirer1, acquirer2));
    }

    private void createWoodRegions() {
        WoodRegion woodRegion1 = new WoodRegion("East of Romania", "RO-E");
        WoodRegion woodRegion2 = new WoodRegion("Germany", "DE");

        getWoodRegionDao().create(Arrays.asList(woodRegion1, woodRegion2));
    }

    private void createTreeSpecies() {
        TreeSpecies treeSpecies1 = new TreeSpecies("bc", "Beech");
        TreeSpecies treeSpecies2 = new TreeSpecies("sp", "Spruce");

        getTreeSpeciesDao().create(Arrays.asList(treeSpecies1, treeSpecies2));
    }

    private void createWoodCertifications() {
        WoodCertification wc1 = new WoodCertification("None");
        WoodCertification wc2 = new WoodCertification("PEFC");

        getWoodCertificationDao().create(Arrays.asList(wc1, wc2));
    }

    private void createLogQualityClasses() {
        LogQualityClass lc1 = new LogQualityClass("HQ", "High Quality");
        LogQualityClass lc2 = new LogQualityClass("LQ", "Low Quality");

        getLogQualityClassDao().create(Arrays.asList(lc1, lc2));
    }

    private void createSuppliers() {
        Supplier supplier1 = new Supplier("SuperWood", "Wood's street", "Germany", "Berlin",
                "1234");
        Supplier supplier2 = new Supplier("UltraWood", "Spruce's street", "Austria", "Vienna",
                "4567");

        getSupplierDao().create(Arrays.asList(supplier1, supplier2));
    }
}
