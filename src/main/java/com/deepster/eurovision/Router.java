package com.deepster.eurovision;

import com.deepster.eurovision.voting.exceptions.InvalidCountryException;
import com.deepster.eurovision.voting.exceptions.InvalidVoteBodyException;
import com.deepster.eurovision.voting.exceptions.InvalidYearException;
import com.deepster.eurovision.voting.exceptions.SameCountryException;
import com.deepster.eurovision.voting.models.Response;
import com.deepster.eurovision.voting.models.Winners;
import com.deepster.eurovision.voting.services.VotingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;

import javax.inject.Named;
import java.util.Set;

import static spark.Spark.*;

/**
 * The router class responsible for starting the Spark Embedded Server, defining routes and attaching appropriate handlers.
 */
public class Router {

    private final Logger LOG = LoggerFactory.getLogger(Router.class);

    @Inject
    private Config config;

    // TODO add a wrapper for a generic json parser
    @Inject
    private ObjectMapper mapper;

    @Inject
    private VotingService votingService;

    @Inject
    @Named("countries")
    private Set<String> countries;

    public Router() {
    }

    public void configure() {

        // initialize server with configuration
        final int port = config.getInt("app.port");
        final int maxThreads = config.getInt("app.max_threads");
        final int minThreads = config.getInt("app.min_threads");
        final int timeOutMillis = config.getInt("app.timeout_millis");
        port(port);
        threadPool(maxThreads, minThreads, timeOutMillis);

        // handle errors during Spark server setup
        initExceptionHandler((ex) -> {
            LOG.error("Application startup failed !", ex);
            System.exit(100);
        });

        // Define routes and handlers

        get("/health", (request, response) -> "EuroVision Voting System is UP!");

        post("/votes/:year", (request, response) -> {
            final Response result = votingService.validateAndCast(request.params("year"), request.body());
            response.type("application/json");
            response.status(201);
            return mapper.writeValueAsString(result);
        });

        get("/votes/:year", (request, response) -> {
            final Winners winners = votingService.validateAndFetchResults(request.params("year"));
            response.type("application/json");
            return mapper.writeValueAsString(winners);
        });

        // Setup gzip compression for all responses

        after((request, response) -> {
            boolean compress = config.getBoolean("http.gzip");
            if (compress) {
                response.header("Content-Encoding", "gzip");
            }
        });

        // Common Exception Handling

        ExceptionHandler<? super RuntimeException> handler = (exception, request, response) -> {
            response.type("application/json");
            response.status(400);
            try {
                response.body(mapper.writeValueAsString(Response.failure(exception.getMessage())));
            } catch (JsonProcessingException e) {
                LOG.error(e.getMessage(), e);
                response.body("Exception Occurred");
            }
        };

        exception(InvalidVoteBodyException.class, handler);
        exception(InvalidYearException.class, handler);
        exception(InvalidCountryException.class, handler);
        exception(SameCountryException.class, handler);

        notFound((req, res) -> {
            res.type("application/json");
            return mapper.writeValueAsString(Response.failure("Request path not found."));
        });

        internalServerError((req, res) -> {
            res.type("application/json");
            return mapper.writeValueAsString(Response.failure("Internal Server Error"));
        });


        // handle successful Spark server setup
        new Thread(() -> {
            awaitInitialization();
            LOG.info("Application stated .. \n Port : {} \n Active Threads : {} ", port(), activeThreadCount());
        }).start();


    }

    public void shutdown(){
        stop();
        // handle Spark server shutdown
        new Thread(() -> {
            awaitStop();
            LOG.info("Application shutdown complete.");
        }).start();
    }
}
