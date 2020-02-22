package com.deepster.eurovision.voting.dao;

import com.deepster.eurovision.voting.models.Vote;
import com.deepster.eurovision.voting.models.Winners;

public interface VotingDAO {

    /**
     * Increment aggregate votes for given country in the given year.
     * @param year Year of the Competition
     * @param country
     * @return total votes cast for specified country in specified year
     */
    double incrementAggregateForCountry(int year, String country);

    /**
     * Save the vote for posterity.
     * @param year Year of the competition
     * @param vote Vote object
     */
    void saveVote(int year, Vote vote);

    /**
     * Get the top 3 winners for a given year
     * @param year Year of the competition
     * @return Winners object representing the Results of the specified year
     */
    Winners getResults(int year);
}
