TWork - Server
==============

This repository is the server for the TWork system.
It's written in Java using the [Play! Framework](https://www.playframework.com/).


Installation instructions
------------------------

Requirements:

* JDK 1.8 or later.
* [Typesafe Activator](https://www.lightbend.com/activator/download) 1.3.7.
* PostgreSQL database.

Procedure:

* Start PostgreSQL on port 5432 with username `twork`, password `test` and a database named `tworkdata`.
* Clone the repository.
* Unpack Typesafe Activator into the `tworkserver` directory.
* Move into the `tworkserver` directory and type `./activator run` to start the server in development mode.

Code structure
--------------

All paths are given from the `tworkserver` directory.

### Configuration and dependencies:

* `build.sbt` contains the dependencies for the project.
* `conf/application.conf` contains database configuration.

### Routes:

`conf/routes` contains the routes for the server, that is, what URLs map to what Java code. For example it contains the line
`GET /job controllers.Application.job()`, which Play! uses to map the request a device makes to get a job to complete. This file is well commented and acts as the API specification for communicating devices. It is worth reading if you want to become familiar with the code.

### Website:

The HTML (with embedded Scala code, Java cannot be used here) is contained in `app/views`.
The images, JavaScript and CSS used by the site is in the `public` directory.
The supporting code for the main page of the website, including file upload, is contained in `app/controllers/Web.java`. The real time image result page is handled by `app/controllers/Display.java` with helper functions in `app/sitehelper`.

### Job server:

The code for the job server is all contained in packages in the `app/` directory, any directories not mentioned so far do not contain code written by us.

#### Global.java

This file contains code that runs every time the server is started.

#### controllers

`Application.java` is the entry point for requests from devices, the details of what each method should do is explained by the corresponding entry in `conf/routes`.

#### computations

This package contains both the code that is sent to devices to be run, which all implement `ComputationCode` and the code to generate computations, which all implement `BasicComputationGenerator`. Reading the relatively short `PrimeComputation` and `PrimeComputationCode` should be informative about the abstractions used in the server.

#### models

These classes can be stored persistently in the database. Ebean uses the field definitions and Java Persistence Annotations to generate SQL statements. UUID generation is handled automatically by Ebean when `save()` is first called.

#### twork

This package contains the bulk of the back end code that manages the computations and jobs as they move around the system.

Tests
-----

The `tworkserver/test/` directory contains a large test suite with tests covering much of the server's functionality. The tests use JUnit along with Play!'s helper functions. Along with unit tests there is a large test covering the lifetime of a PrimeComputation which runs against a test server, interacting via HTTP requests.

The tests may be run with `./activator test` (from `tworkserver/`).


&nbsp;

&copy; 2016 Razvan Kusztos (razvankusz), Ben Ramchandani (Ben-Ramchandani), Dmitrij Szamozvancev (DimaSamoz), James Wood (laMudri), Laura Nechita (redls)
