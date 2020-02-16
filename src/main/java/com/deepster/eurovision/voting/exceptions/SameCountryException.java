package com.deepster.eurovision.voting.exceptions;

public class SameCountryException extends RuntimeException {

    public SameCountryException(final String country) {
        super(String.format("You cannot choose %s, %s is your home country. Please vote for another country.", country, country));
    }
}
