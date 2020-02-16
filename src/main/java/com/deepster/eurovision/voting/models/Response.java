package com.deepster.eurovision.voting.models;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Response {
    private final String result;
    private final String message;

    public Response(final String result, final String message) {
        this.result = result;
        this.message = message;
    }

    public static Response success(final String message) {
        return new Response("success", message);
    }

    public static Response failure(final String message) {
        return new Response("failure", message);
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

}
