package com.deepster.eurovision.voting.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Winners {

    @JsonProperty(required = true)
    private final int year;
    // TODO make it extendable
    private final String first;
    private final String second;
    private final String third;

    public Winners(int year, String first, String second, String third) {
        this.year = year;
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public int getYear() {
        return year;
    }

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }

    public String getThird() {
        return third;
    }

    @Override
    public String toString() {
        return "Winners{" + "year=" + year + ", first='" + first + '\'' + ", second='" + second + '\'' + ", thrid='" + third + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        if (o==null || getClass()!=o.getClass()) return false;
        Winners winners = (Winners) o;
        return getYear()==winners.getYear() && Objects.equal(getFirst(), winners.getFirst()) && Objects.equal(getSecond(), winners.getSecond()) && Objects.equal(getThird(), winners.getThird());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getYear(), getFirst(), getSecond(), getThird());
    }
}
