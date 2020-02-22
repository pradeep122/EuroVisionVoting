# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Pending]
 - Automate build and publish tasks for docker images
 - Code Analysis
 - CORS support
 - Caching to improve speed of GET calls
 - Performance Testing
 - Metrics (throughput, latency, jvm metrics (memory, cpu) disk )
 - Extranalize all strings and support Internationalization
 - Deploy to any cloud provider
 
## [Unreleased]
 - Using gradle tasks to build, tag and deploy docker images using the project version
 
## [1.0.0] - 2020-02-22
### Added
- POST call to accept votes for a year
- GET call to show top 3 winners for a year
- Validate countries
- Return proper HTTP codes  
- Add API tests
- Logging ( linked to loggly )
- Unit Tests for VotingService
- Gradle tasks for check, test, build and run
- Containerization
- Setup docker-compose for deployment



[Unreleased]: https://github.com/pradeep122/EuroVisionVoting/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/pradeep122/EuroVisionVoting/releases/tag/v0.0.1