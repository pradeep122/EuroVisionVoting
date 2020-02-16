package com.deepster.eurovision.voting.exceptions;

import java.time.Year;

public class InvalidYearException extends RuntimeException {

    public InvalidYearException(final String year) {
        super(String.format("%s is an invalid year. Please select a year between 1956 and %d.", year, Year.now().getValue()));
    }
}
