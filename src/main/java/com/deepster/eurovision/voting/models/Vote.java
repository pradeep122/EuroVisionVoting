package com.deepster.eurovision.voting.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Vote {
    private final String countryFrom;
    private final String votedFor;

    @JsonCreator
    public Vote(@JsonProperty(value = "countryFrom", required = true) String countryFrom, @JsonProperty(value = "votedFor", required = true) String votedFor) {

        this.countryFrom = countryFrom;
        this.votedFor = votedFor;
    }

    public String getCountryFrom() {
        return countryFrom;
    }

    public String getVotedFor() {
        return votedFor;
    }

    @Override
    public String toString() {
        return "Vote{ from='" + countryFrom + '\'' + ", to='" + votedFor + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        if (o==null || getClass()!=o.getClass()) return false;
        Vote vote = (Vote) o;
        return Objects.equal(getCountryFrom(), vote.getCountryFrom()) && Objects.equal(getVotedFor(), vote.getVotedFor());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getCountryFrom(), getVotedFor());
    }
}
