package com.deepster.eurovision;

import com.deepster.eurovision.voting.dao.RedisVotingDAO;
import com.deepster.eurovision.voting.dao.VotingDAO;
import com.deepster.eurovision.voting.services.RedisVotingService;
import com.deepster.eurovision.voting.services.VotingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class GlobalModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalModule.class);

    @Override
    protected void configure() {
        bind(Config.class).toInstance(ConfigFactory.load());
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
        bind(VotingService.class).to(RedisVotingService.class).in(Scopes.SINGLETON);
        bind(VotingDAO.class).to(RedisVotingDAO.class).in(Scopes.SINGLETON);
        bind(Router.class).toInstance(new Router());

    }

    @Singleton
    @Provides
    RedisCommands<String, String> getClient(final Config conf) {
        final String redisHost = conf.getString("redis.host");
        final int redisPort = conf.getInt("redis.port");
        final int retries = conf.getInt("redis.retry_count");
        RedisURI redisUri = RedisURI.Builder.redis(redisHost, redisPort)
                .withTimeout(Duration.ofSeconds(conf.getInt("redis.timeout_in_seconds")))
                .build();
        RedisClient client = RedisClient.create(redisUri);

        // Exponential backoff while connecting to redis
        RedisCommands<String,String> connection = null;
        long sleepSeconds = 2;
        int remainingTries = retries;
        while(remainingTries > 0){
            remainingTries --;
            try {
                connection = client.connect().sync();
                break;
            } catch (RedisConnectionException ex){
                try{

                    LOG.error(String.format("Failed to connect to Redis at %s:%d , retrying in %d seconds. %d retries left", redisHost, redisPort, sleepSeconds, remainingTries));
                    TimeUnit.SECONDS.sleep(sleepSeconds);
                    sleepSeconds = sleepSeconds * sleepSeconds;
                } catch (InterruptedException iex){
                    LOG.error("Unable to sleep for retries");

                }
            }
        }

        if(connection == null){
            throw new RedisConnectionException(String.format("Unable to connect to Redis after %d retries", retries));
        }

        return connection;
    }


    @Singleton
    @Named("countries")
    @Provides
    Set<String> getCountries(final Config config) {
        return config.getStringList("euro_vision.countries").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

}
