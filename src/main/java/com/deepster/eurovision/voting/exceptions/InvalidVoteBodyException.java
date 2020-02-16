package com.deepster.eurovision.voting.exceptions;

public class InvalidVoteBodyException extends RuntimeException {

    public InvalidVoteBodyException() {
        super("JSON body is invalid. Please send countryFrom and votedFor in your JSON body.");
    }
}
