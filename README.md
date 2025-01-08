# Simple User Management Application

## Building

### Note
This software has been built, tested and run on Kubuntu 24.04, but should work on Windows, *nix and macOS. 
However, it has only been tested on Ubuntu based distributions.

### Prerequisites 

#### Java 
Java 21.05 have been used for developing this software, but the target compatibility is JAVA-17
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





