
# Eurovision Voting System

## Scope

#### Included in Scope

- REST API for Adding Votes and Fetching results
- Validation for countries and year.
- Robust Application to handle moderate traffic spikes

```text
    Functional Requirements
    
    --------------------------------------
    Feature: Voting for a Country
        User can vote for any country
        User cannot vote for his own country
        User can only vote for a eurovision country
        User can only vote from a eurovision country
          
    ------------------------------------          
    Feature: Fetching Results
        User can see top 3 countries by votes for any year
``` 

I chose a lightweight Web Framework ( Spark ), backed by a Redis database.  Redis is configured to write to disk on every write to handle data consistency. In case of failure, the votes being processed will be lost. This setup provides scalability at the cost of consistency.
 
If data consistency is a priority , we could go for a relational database with read replicas to handle more traffic.
        
#### Out of Scope 

 - Authentication / Authorization
 - User Accounts
 - SSL / HTTPS ( within the cluster and outside)
 - Rate limiting
 - Fraud detection 
        
## Features 

```text

```

## Tech Stack

- Git 
- Java 11
- Gradle
- Spark
- Redis
- Docker 
- docker-compose
- Loggly for Access Logs 

 
 ## Redis Schema
     
 Goals
 - Store just enough information required to compute the top 3 countries
 - Optionally store everyVote for posterity
    
```text
//User 1 SortedSet per year to store total votes for a country
//Add a vote for Netherlands in 2020
>> ZINCR euro_vision:aggregate:2020 Netherlands 1

// Retrieve top 3 Votes for 2020
>> ZREVRANGE euro_vision:aggregate:2020 0 3 WITHSCORES 

// For Auditing purpose, store all votes as HSET
>> HSET euro_vision:votes:2020:<timestamp> countryFrom Belgium votedFor Netherlands

```
    
## Build and Run

Using Docker and Docker Compose

    gradlew.bat
    docker build -t deepster/eurovision-voting:latest .
    docker push deepster/eurovision-voting:latest
    docker-compose down
    docker-compose up


## Limitations

- Using Redis in memory DB to handle all traffic, could migrate to a message queue for queueing requests to better hanlde higher loads and spikes
- Using personal Docker Hub to publish and pull images from, a private repo is preferable
- No separation of production config, although possible to add it easily
