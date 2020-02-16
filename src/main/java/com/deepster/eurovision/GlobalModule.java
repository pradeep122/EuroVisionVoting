package com.deepster.eurovision;

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
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

// TODO move voting related module to a submodule in voting folder
public final class GlobalModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalModule.class);

    @Override
    protected void configure() {
        bind(Config.class).toInstance(ConfigFactory.load());
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
        bind(VotingService.class).to(RedisVotingService.class).in(Scopes.SINGLETON);
        bind(Router.class).toInstance(new Router());

    }

    @Singleton
    @Provides
    RedisCommands<String, String> getClient(final Config conf) {
        String redisHost = conf.getString("redis.host");
        int redisPort = conf.getInt("redis.port");
        RedisURI redisUri = RedisURI.Builder.redis(redisHost, redisPort)
                .withTimeout(Duration.ofSeconds(conf.getInt("redis.timeout_in_seconds")))
                .build();
        RedisClient client = RedisClient.create(redisUri);
        return client.connect().sync();
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
