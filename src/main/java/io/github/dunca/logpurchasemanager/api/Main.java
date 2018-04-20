package io.github.dunca.logpurchasemanager.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dunca.logpurchasemanager.api.dao.DatabaseHelper;
import io.github.dunca.logpurchasemanager.api.route.AggregationRoute;
import io.github.dunca.logpurchasemanager.api.route.FullAcquisitionRoute;
import io.github.dunca.logpurchasemanager.api.route.StaticDataRoute;
import io.github.dunca.logpurchasemanager.api.route.response.constants.ResponseMessages;
import io.github.dunca.logpurchasemanager.api.route.response.exceptions.InvalidModelException;
import io.github.dunca.logpurchasemanager.api.route.response.exceptions.UnsupportedHttpMethodException;
import io.github.dunca.logpurchasemanager.api.route.response.exceptions.util.ResponseError;
import io.github.dunca.logpurchasemanager.api.route.response.transformers.JsonResponseTransformer;
import io.github.dunca.logpurchasemanager.api.route.response.util.StatusCode;
import org.h2.tools.Server;
import spark.Response;
import spark.Route;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

public class Main {
    private static final Logger L = Logger.getLogger(Main.class.getName());

    private static final int PORT = 80;
    private static final int DB_PORT = 9090;

    private static final String DB_NAME = "log_purchase_manager_database";
    private static final String DB_FOLDER = System.getProperty("user.home");
    private static final String DB_PATH = Paths.get(DB_FOLDER, DB_NAME).toString();

    private static JsonResponseTransformer jsonResponseTransformer;

    private static Server databaseServer;
    private static DatabaseHelper dbHelper;

    public static void main(String[] args) throws SQLException {
        initDb();

        ObjectMapper objectMapper = new ObjectMapper();
        jsonResponseTransformer = new JsonResponseTransformer(objectMapper);

        setupServer();

        registerExceptionClasses();

        registerBeforeFilters();
        registerAfterFilters();

        registerRoutes();
    }

    private static void initDb() throws SQLException {
        databaseServer = Server.createTcpServer("-tcpPort", String.valueOf(DB_PORT)).start();
        dbHelper = new DatabaseHelper("jdbc:h2:tcp://localhost:" + DB_PORT + "/" + DB_PATH);

        if (!(new File(DB_PATH + ".mv.db").isFile())) {
            dbHelper.createDatabaseTables();
            L.info("Database created");

            dbHelper.initStaticData();
            L.info("Database populated with static data");
        } else {
            L.info("Database exists");
        }
    }

    private static void setupServer() {
        port(PORT);
        L.info("Set server port to " + PORT);
    }

    private static void registerExceptionClasses() {
        registerExceptionClass(UnsupportedHttpMethodException.class, StatusCode::badRequest);
        registerExceptionClass(InvalidModelException.class, StatusCode::badRequest);
    }

    private static <T extends RuntimeException> void registerExceptionClass(Class<T> exceptionClass, Consumer<Response> responseStatusSetter) {
        exception(exceptionClass, (exception, request, response) -> {
            ResponseError responseError = new ResponseError(exception);

            String body;

            try {
                body = jsonResponseTransformer.render(responseError);
                responseStatusSetter.accept(response);
            } catch (Exception e) {
                body = ResponseMessages.SERIALIZATION_ERROR;
                StatusCode.internalServerError(response);
            }

            response.body(body);
        });
    }

    private static void registerBeforeFilters() {
        L.info("Registering 'before' filters");

        before((req, res) -> {
            L.info("Incoming request for " + req.pathInfo());
            L.info(req.body());
        });
    }

    private static void registerAfterFilters() {
        L.info("Registering 'after' filters");

        after((req, res) -> {
            res.type("application/json");
        });
    }

    private static void registerRoutes() {
        L.info("Registering routes");

        registerGet("/staticdata", new StaticDataRoute(dbHelper));
        registerPost("/fullacquisition", new FullAcquisitionRoute(dbHelper));
        registerPost("/aggregation", new AggregationRoute(dbHelper));
    }

    private static void registerGet(String path, Route route) {
        get(path, route, jsonResponseTransformer);
    }

    private static void registerPost(String path, Route route) {
        post(path, route, jsonResponseTransformer);
    }
}
