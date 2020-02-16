package com.deepster.eurovision.voting.exceptions;

public class InvalidCountryException extends RuntimeException {

    public InvalidCountryException(final String country) {
        super(String.format("You cannot choose %s, %s does not take part in Euro Vision.", country, country));
    }
}
