
# Eurovision Voting System

## Scope

#### Included in Scope

- REST API for Adding Votes and Fetching results
- Validation for countries and year.
- Robust Application to handle moderate traffic spikes


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
        

I chose a lightweight Web Framework ( Spark ), backed by a Redis database.  Redis is configured to write to disk on every write to handle data consistency. In case of failure, the votes being processed will be lost. This setup provides scalability at the cost of consistency.
 
If data consistency is a priority , we could go for a relational database with read replicas to handle more traffic.
        
#### Out of Scope 

 - Authentication / Authorization
 - User Accounts
 - SSL / HTTPS ( within the cluster and outside)
 - Rate limiting
 - Fraud detection 
        
## Features 

            [X] Done  [-] Pending
    [x] POST call to accept votes for a year
    [x] GET call to show top 3 winners for a year
    [x] Validate countries
    [x] Return proper HTTP codes  
    [x] API tests
    [x] Logging ( linked to loggly )
    [-] Unit Tests for VotingService
    [-] Containerization
    [-] Gradle tasks for check, test, build, run and deploy
    [-] Extranalize all strings and support Internationalization
    [-] Setup docker-compose for deployment
    [-] Deploy to any cloud provider
    [-] Code Analysis
    [-] CORS support
    [-] Caching to improve speed of GET calls
    [-] Metrics (throughput, latency, jvm metrics (memory, cpu) disk )
    [-] Performance Testing

## Tech Stack

- Git 
- Java 13
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
    
        
