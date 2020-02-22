package com.deepster.eurovision;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static io.restassured.RestAssured.given;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;
import redis.embedded.RedisServer;


class APITests {

    private static final Logger LOG = LoggerFactory.getLogger(APITests.class);
    private final String base = "http://localhost:8080";
    //TODO use docker compose and a grale plugin to start a redis server
    private static RedisServer redisServer;

    private String vote(String from, String to) {
        return String.format("{\"countryFrom\" : \"%s\", \"votedFor\" : \"%s\"}", from, to);
    }

    @BeforeAll
    static void beforeAll() {
        Application.getInstance().startup();
    }

    @AfterAll
    static void afterAll() {
        Application.getInstance().shutdown();
    }

    @Test
    @DisplayName("Health Check")
    void check1() {
        when().
                get(base + "/health").
                then().
                assertThat().
                statusCode(200).
                and().
                body(equalTo("EuroVision Voting System is UP!"));

    }

    @Test
    @DisplayName("Reject unknown paths")
    void check2() {
        when().
                get(base + "/home").
                then().
                assertThat().
                statusCode(404).
                and().
                body("result", equalTo("failure"), "message",
                        equalTo("Request path not found."));
    }

    @Test
    @DisplayName("User can vote for any country")
    void voteTest1() {

        given().
                body(vote("Netherlands", "Belgium")).
                when().
                post(base + "/votes/2020").
                then().
                assertThat().
                statusCode(201).
                and().
                body("result", equalTo("success"), "message",
                        containsString("Your vote is recorded."));


        given().
                body(vote("Belgium", "Spain")).
                when().
                post(base + "/votes/2019").
                then().
                assertThat().
                statusCode(201).
                and().
                body("result", equalTo("success"), "message",
                        containsString("Your vote is recorded."));

    }

    @Test
    @DisplayName("User cannot vote for his own country")
    void voteTest2() {

        given().
                body(vote("Belgium", "Belgium")).
                when().
                post(base + "/votes/2020").
                then().
                assertThat().
                statusCode(400).
                and().
                body("result", equalToObject("failure"), "message",
                        containsString("Please vote for another country."));
    }

    @Test
    @DisplayName("User can only vote for a eurovision country")
    void voteTest3() {

        given().
                body(vote("Belgium", "India")).
                when().
                post(base + "/votes/2020").
                then().
                assertThat().
                statusCode(400).
                and().
                body("result", equalToObject("failure"), "message",
                        equalTo("You cannot choose India, India does not take part in Euro Vision."));
    }

    @Test
    @DisplayName("User can only vote from a eurovision country")
    void voteTest4() {

        given().
                body(vote("India", "Belgium")).
                when().
                post(base + "/votes/2020").
                then().
                assertThat().
                statusCode(400).
                and().
                body("result", equalToObject("failure"), "message",
                        equalTo("You cannot choose India, India does not take part in Euro Vision."));
    }

    @Test
    @DisplayName("User can only vote for a valid year")
    void voteTest5() {

        given().
                body(vote("Netherlands", "Belgium")).
                when().
                post(base + "/votes/2022").
                then().
                assertThat().
                statusCode(400).
                and().
                body("result", equalToObject("failure"), "message",
                        equalTo("2022 is an invalid year. Please select a year between 1956 and 2020."));
        given().
                body(vote("Netherlands", "Belgium")).
                when().
                post(base + "/votes/1900").
                then().
                assertThat().
                statusCode(400).
                and().
                body("result", equalToObject("failure"), "message",
                        equalTo("1900 is an invalid year. Please select a year between 1956 and 2020."));

        given().
                body(vote("Netherlands", "Belgium")).
                when().
                post(base + "/votes/sfdwef23").
                then().
                assertThat().
                statusCode(400).
                and().
                body("result", equalToObject("failure"), "message",
                        equalTo("sfdwef23 is an invalid year. Please select a year between 1956 and 2020."));

    }

    @Test
    @DisplayName("User should send valid json")
    void voteTest6() {

        given().
                body("{\"from\" : \"Belgium\", \"to\" : \"Spain\"}").
                when().
                post(base + "/votes/2020").
                then().
                assertThat().
                statusCode(400).
                and().
                body("result", equalToObject("failure"), "message",
                        equalTo("JSON body is invalid. Please send countryFrom and votedFor in your JSON body."));

        given().
                body("{\"countryFrom\" : \"Belgium\", \"to\" : \"Spain\"}").
                when().
                post(base + "/votes/2020").
                then().
                assertThat().
                statusCode(400).
                and().
                body("result", equalToObject("failure"), "message",
                        equalTo("JSON body is invalid. Please send countryFrom and votedFor in your JSON body."));
        given().
                body("{\"from\" : \"Belgium\", \"votedFor\" : \"Spain\"}").
                when().
                post(base + "/votes/2020").
                then().
                assertThat().
                statusCode(400).
                and().
                body("result", equalToObject("failure"), "message",
                        equalTo("JSON body is invalid. Please send countryFrom and votedFor in your JSON body."));

    }

    @Test
    @DisplayName("User can see top 3 countries by votes for any year")
    void resultTest1() {

        final List<String> votes = List.of(
                vote("Netherlands", "Belgium"),
                vote("Netherlands", "Spain"),
                vote("Netherlands", "France"),
                vote("Spain", "Belgium"),
                vote("Spain", "Netherlands"),
                vote("Spain", "Ireland"),
                vote("Spain", "Netherlands"),
                vote("France", "Belgium"),
                vote("France", "Spain"),
                vote("France", "Netherlands"),
                vote("Belgium", "Netherlands"),
                vote("Belgium", "Netherlands"),
                vote("Belgium", "Spain"));

        votes.parallelStream().
                forEach(voteBody -> {
                    given().
                        body(voteBody).
                        post("/votes/2002").
                        then().
                        assertThat().
                        statusCode(201).
                        body("result", equalTo("success")).
                        and().
                        body("message", containsString("Your vote is recorded."));
                });

        when().
                get("/votes/2002").
                then().
                assertThat().
                statusCode(200).
                body("first", equalTo("Netherlands")).
                and().
                body("second", equalTo("Spain")).
                and().
                body("third", equalTo("Belgium"));


    }

    @Test
    @DisplayName("User can only see results for a valid year")
    void resultTest2() {
        when().
                get(base + "/votes/2022").
                then().
                assertThat().
                statusCode(400).
                and().
                body("result", equalToObject("failure"), "message",
                        equalTo("2022 is an invalid year. Please select a year between 1956 and 2020."));


        when().
                get(base + "/votes/dsfd").
                then().
                assertThat().
                statusCode(400).
                and().
                body("result", equalToObject("failure"), "message",
                        equalTo("dsfd is an invalid year. Please select a year between 1956 and 2020."));
    }

}