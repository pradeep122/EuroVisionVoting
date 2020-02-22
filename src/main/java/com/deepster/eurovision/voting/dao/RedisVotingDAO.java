package com.deepster.eurovision.voting.dao;

import com.deepster.eurovision.voting.dao.VotingDAO;
import com.deepster.eurovision.voting.models.Vote;
import com.deepster.eurovision.voting.models.Winners;
import com.deepster.eurovision.voting.services.VotingService;
import com.google.inject.Inject;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RedisVotingDAO implements VotingDAO {

    private static final Logger LOG = LoggerFactory.getLogger(VotingService.class);
    private final RedisCommands<String, String> connection;

    @Inject
    public RedisVotingDAO(RedisCommands<String, String> connection) {
        this.connection = connection;
    }

    @Override
    public double incrementAggregateForCountry(int year, String country) {
        return this.connection.zincrby(String.format("euro_vision:aggregate:%d", year), 1, country);
    }

    @Override
    public void saveVote(int year, Vote vote) {
        this.connection.hmset(String.format("euro_vision:votes:%d:%d", year, System.currentTimeMillis()), Map.of("countryFrom", vote.getCountryFrom(), "votedFor", vote.getVotedFor()));
    }

    @Override
    public Winners getResults(int year) {
        List<String> result = connection.zrevrange(String.format("euro_vision:aggregate:%d", year), 0, 3);
        LOG.debug(result.toString());
        //TODO handle empty results
         return new Winners(year, result.get(0), result.get(1), result.get(2));

    }
}
