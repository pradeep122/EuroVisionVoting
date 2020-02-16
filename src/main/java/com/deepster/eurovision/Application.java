package com.deepster.eurovision;


import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    // Hidden Constructor
    private Application() {
    }

    public static void main(String[] args) {
        LOG.info(" Application Starting ... ");
        try {
            Injector injector = Guice.createInjector(new GlobalModule());
            final Logger accessLogger = LoggerFactory.getLogger("access");
            new Utils().createServerWithAccessLogging(accessLogger);

            Router router = injector.getInstance(Router.class);
            router.configure();

        } catch (Exception ex) {
            LOG.error("UnCaught Exception : ", ex);
        }

    }

}
