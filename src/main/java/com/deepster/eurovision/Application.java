package com.deepster.eurovision;


import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;
import spark.Spark;

public final class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private static final Application SINGLETON = new Application();
    private Router router;

    public static Application getInstance() {
        return SINGLETON;
    }

    private Application() {

    }


    public void startup() {

        LOG.info(" Application Starting ... ");
        try {
            Injector injector = Guice.createInjector(new GlobalModule());
            final Logger accessLogger = LoggerFactory.getLogger("access");
            new Utils().createServerWithAccessLogging(accessLogger);

            router = injector.getInstance(Router.class);
            router.configure();

        } catch (Exception ex) {
            LOG.error("UnCaught Exception : ", ex);
        }

    }

    public void shutdown(){

        LOG.info(" Application Shutting Down ... ");
        try{
            router.shutdown();
        } catch (Exception ex) {
            LOG.error("UnCaught Exception : ", ex);
        }

    }

    public static void main(String[] args) {
        Application.getInstance().startup();
    }

}
