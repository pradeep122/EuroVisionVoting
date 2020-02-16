package com.deepster.eurovision.voting.services;

import com.deepster.eurovision.voting.models.Response;
import com.deepster.eurovision.voting.models.Vote;
import com.deepster.eurovision.voting.models.Winners;

/**
 * Interface for a Voting Service.
 */
public interface VotingService {

    /**
     * Given a year and a Vote (countryFrom and votedFor), a vote is cast.
     *
     * @param year eurovision year
     * @param vote the Vote object
     * @return total votes for {@code votedFor} country
     */
    long cast(int year, Vote vote);

    /**
     * A wrapper for {@link #cast(int, Vote)} which takes string representations of
     * year and vote and validates them before calling the {@link #cast(int, Vote)} method.
     *
     * @param year     year as a string
     * @param voteJson JSON string
     * @return {@link Response} object encapsulating success and failure
     */
    Response validateAndCast(String year, String voteJson);

    /**
     * Given a year returns the 3 countries with the most votes.
     *
     * @param year eurovision year
     * @return {@link Winners} object encapsulation the result
     */
    Winners getResults(int year);


    /**
     * A wrapper for {@link #getResults(int)} which takes string value of
     * year and validates it before calling the {@link #getResults(int)} method.
     *
     * @param year year as a string
     * @return {@link Response} object encapsulating success and failure
     */
    Winners validateAndFetchResults(String year);

}
