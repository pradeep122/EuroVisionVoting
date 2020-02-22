package com.deepster.eurovision;

import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.embeddedserver.EmbeddedServers;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;
import spark.embeddedserver.jetty.JettyServerFactory;


public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    /**
     * This method lets spark create an Embedded Jetty Server with Access Logs enabled. An appropriate Jetty Server factory
     * is created with the necessary setup and passed to Spark , to be used while creating the Embedded Jetty Server
     *
     * @param logger the logger isntance to render the access logs
     */
    public void createServerWithAccessLogging(final Logger logger) {

        final String CUSTOM_NCSA_FORMAT = "%{client}a - %u %t \"%r\" %s %O \"%{Referer}i\" \"%{User-Agent}i\"  {\"latency\": %{ms}T}";
        final CustomRequestLog accessLogger = new CustomRequestLog(logger::info, CUSTOM_NCSA_FORMAT);

        final EmbeddedJettyFactory jettyServerFactory = new EmbeddedJettyFactory(new EmbeddedJettyServerFactory(accessLogger));

        EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, jettyServerFactory);
    }

    /**
     * Creates a Jetty server factory with a custom Request Logger, to log all requests in a modified NCSA format.
     * Adapted from spark.embeddedserver.jetty.JettyServer ( protected class )
     * <p>
     * Copies a Jetty Server implemenation and wraps it with a request logger instance.
     */
    private static class EmbeddedJettyServerFactory implements JettyServerFactory {
        private CustomRequestLog requestLog;

        EmbeddedJettyServerFactory(CustomRequestLog requestLog) {
            this.requestLog = requestLog;
        }

        @Override
        public Server create(final int maxThreads, final int minThreads, final int threadTimeoutMillis) {
            Server server;
            if (maxThreads > 0) {
                int min = minThreads > 0 ? minThreads : 8;
                int idleTimeout = threadTimeoutMillis > 0 ? threadTimeoutMillis:'\uea60';
                server = new Server(new QueuedThreadPool(maxThreads, min, idleTimeout));
            } else {
                server = new Server();
            }

            server.setRequestLog(this.requestLog);
            return server;
        }

        @Override
        public Server create(final ThreadPool threadPool) {
            return new Server(threadPool);
        }
    }
}
