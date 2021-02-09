# simpleparser

A two part exercise in parsing files and building REST endpoints.

## Setup

This project uses sbt, so the first step is to make sure you have that installed:

https://www.scala-sbt.org/1.x/docs/Setup.html

Once sbt is installed you can bootstrap the project by running:

>sbt clean compile test

You can acces the CLI or the REST API as follows:

### CLI

>sbt "project cli" run [filename]

or

>sbt "project cli" test

### API

>sbt "project api" run

Starts a local server at http://localhost:8080.

>sbt "project api" test

## Next Steps

- Add packaging step to produce deployable jars
- Look into using tapir (https://github.com/softwaremill/tapir) to separate endpoint defintions from logic and to provide API documentation
- Use more cats (https://typelevel.org/cats/)
