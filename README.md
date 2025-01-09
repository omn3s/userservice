# Simple User Management Application

## Building

### Note
This software has been built, tested and run on Kubuntu 24.04, but should work on Windows, *nix and macOS. 
However, it has only been tested on Ubuntu based distributions.

### Prerequisites 

#### Java 

Java 21.05 have been used for developing this software, but the target compatibility is JAVA-17.
Github actions use java 17.
First install a JDK to build the software as described below.
#####  Ubuntu 22.04+ or WSL installation

    sudo apt install openjdk-<jdk version>-jdk[-headless]

e.g.
    
    sudo apt install openjdk-21-jdk
    sudo apt install openjdk-21-jdk-headless


##### Windows
Download from either Oracle (https://docs.oracle.com/en/java/javase/) or OpenJDK builds from Adoptium.net (https://adoptium.net/temurin/releases/)


##### Define `JAVA_HOME` environment variable

Note that this step may be optional if java is on the system path and `java -version` displays a compatible version.

`JAVA_HOME` should be defined to specify the installation directory. For example, on a Windows system with a custom installation location.

    JAVA_HOME=c:\java\jdk21\

Or on a Linux distribution

    JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64/


### Build System : Gradle
The Build system is using [gradle](https://gradle.org/). **There is no requirement to install gradle to use this build.**

The first time `gradlew` runs it will set up the appropriate environment, downloading appropriate gradlew version etc.

### Running the build

#### *nix, WSL, CYGWIN etc.
    ./gradlew <targets>

#### Windows
    .\gradlew.bat <targets>

#### Key build targets

* `./gradlew clean` Clean build artifacts
* `./gradlew run` Run from gradle
* `./gradlew install` Build and make an installation - located in the `<repo-root>/install/` use `./install/bin/userservice` to run
* `./gradlew jacocoTestReport` Run tests with jacoco test coverage and report (`./build/reports/jacoco/test/html/index.html`)
* `./gradlew test` Run tests without coverage

## Example Run

    ./gradlew clean install
    ./install/bin/userservice 

And in another terminal

    ./install/bin/walkthrough.sh

This bash script will register, login and retrieve the user profile using `curl` as the client.

## Artifacts from Github actions

There are 2 artifacts created by the github action.

`coverage-report` which is a zip containing the coverage report from the last commit.

`distribution` which contains the userservice-1.0-SNAPSHOT built by the build command.

## Design Choices and approaches

### Change should be simple and focused

A number of systems could be changed during the life-time of an application for example,

* database
* token scheme
* Web/API framework

Could all be replaced and therefore, the design of the system aims to minimise the consequences of change.

In the case of databases, for example whether a Sql or NoSql solution is used is only known to the construction code
in the application class. The `NativeKey` class focuses on separately the implementation of the primary key
(e.g. ObjectId - MongoDB, UniqueId- SqlServer). Also, the use of a `StorageException` to wrap exceptions from the
storage
implementation, means that handling code can identify the logical source of any issue, without knowing the
implementation.

The use of a separate web package to limit the exposure to the Javalin framework to the smallest number of classes.

### Why Javalin?

Javalin is used in preference to SpringBoot, micronaut, Vert.x or any other frameworks, because it is a small, simple
microframework. The main benefit is that it should be easier to comprehend to those with little experience with other
frameworks.
There is little convention or documentation to read prior to getting started with the application.
Also, it is very quick to start and easy to add features as necessary.
With Javalin is very easy to test the entire stack `org.omn3s.userservice.ApplicationFunctionalTests` contains
a set of test methods each of which create new instances of the application and test whole system.

### JSON Web Tokens

JSON Web Tokens (JWT) are a standard mechanism for generating and validating authentication tokens.
As they are not tied to sessions, connections are stateless and can be verified without accessing database resources.
Therefore, as long as the 'secret' is shared or a public/private pair is used, each token is valid in any member of a
cluster and resilient to process restarts.

However, the appropriate solution for authentication should be aligned with the front end technology whether a single
page web app or more traditional multiple page applications. In some cases, using sessions to initiate and maintain
authentication may be simpler.

### Why no Dependency Injection Library?

This was a deliberate decision to make explicit the application graph in a few lines of code in the `Application` class.
The benefits of autowiring dependences using `guice`, `springboot` or other frameworks are for more significantly
complex applications.

### Naming of endpoints

All urls serviced by this application begin with `/v1/` to provide versioning to the paths.
APIs are constantly changing and sometimes those changes are breaking.
If the entire system is a single system that is upgraded together with no external clients, a versioned API may not be
necessary.
However, if there are multiple software components with different release cadences then versioning allows new
functionality
to co-exist with older front end systems, whilst supporting the development of newer or alternative front-ends or client
software.

## Limitations and Improvements

### Configuration and Setup

At the moment, changing any settings requires a recompilation of the code.
Configuration support to change port or similar should be done.
If using a dependency inject library e.g. `guice` or `spring` then it becomes possible to switch between different
storage implementations or optional components.

### Database

The database used is an embedded H2 database using an in-memory only schema.
Clearly, in a production system this would a persistent storage solution, such as a SQL database or a NoSql document
store.

#### Database connection pooling

Database connection pooling is advised in production systems, it has not been implemented here.
Some drivers e.g. MongoDB implement database pooling internally.

#### Move sql strings to external files

As SQL queries may vary by SQL database used, it would be advisable to move the basic query strings into configuration
files.
Thus, when a different SQL implementation is used the appropriate dialect can be chosen.
This is particularly important for Data Definition Language(DDL) commands e.g. create.

#### Preventing data conflicts

At present, the user records cannot be modified and therefore data conflicts will not occur.
However, once the records can be changed a locking policy should be put in place to prevent conflicting writes.

### Rate Limiting

Rate limiting approach is based on in memory counts placed into discrete buckets of 1 minute and solely on the
`/v1/login` path.
This has a number of limitations

* Maximum run of requests is 9. i.e. 4 requests in minute 'X' can be followed by 5 requests in minute 'X+1' before a 429
  response is returned.
  This is usually adequate, but if hard limits of a rolling minute window are required then a more sophisticated
  approach is necessary
* In memory storage of counts means that rate limiting can be implemented across a cluster without implementing 'sticky
  clients'
  A solution to this would be the use of a tool like Redis to implement the counts for rate limiting.
* Registration (`/v1/register') endpoints are particularly susceptable for attacks.
  Using IP to determine a basic form of user identity would enable rate limiting to be applied there

### SSL

SSL configuration has not been supported. It is relatively straightforward to add SSL encryption to the application.

### Better password policy implementation

At the moment the password policy is a minimum length of 6. It would be wiser to enforce stricter standards.

### User Verification

When registration with and email is prudient to verify the registration by sending the user to the registring email.
This accomplishes a number of things.

* The email is validated, rather than assumed to be correct.
* It is more secure, if there is an additional step to slow the registration process down to prevent denial attacks

### Notification of application events

Sign up of a user is a significant event and thus should emit events to interested subsystems.
For example, this would enable the User Verfication to be triggered asynchronously. Also, provisioning of resources
required for the user. Using publish-subscribe event systems to support an Event driven architecture.

### Metrics and Monitoring

No metrics collection or monitoring has been incorporated into the code.
This could be provided by a hosting system such as AWS or by using other tools from monitoring tools such as data dog.

### Logging

The logging of this application is minimal, specifically, no request logging is implemented. Good logging approaches is
critical to successful monitoring and diagnostics of user issues.

### More fine-grained testing code

This code has focused on the overall functional tests and very few unit tests. For most activities this may be
sufficient,
the test coverage is > 95%. However, for high code coverage does not mean high data coverage.
For example, email validation should be tested against a wide range of emails to ensure good coverage of test cases. 
 








