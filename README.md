# Silver Programming Language
<img src="res/silver_logo.png" width="200">

Silver is a programming language designed to resemble Python with some C features, packed with a rich plugin system.

# Contribute
In order to ensure a minimal conflict workflow, it is required that all changes to the project be made in a separate
branch, to then be merged into the dev branch before then being merged into master. This ensures that master remains 
stable. *Only stable code is allowed into master*.

## Structure
The structure of the project is as follows:
```
root
    |- This is the root of the project, which is considered to be the "main" or "impl" of the compiler, which only has
        direct access to the `api` module. 
    |- api
        |- This is where all api code goes, which will be used for the final implementation of the compiler
    |- core
        |- This is where all core code goes, which is hidden by the final implementation and only stands as the backbone
            of the compiler.
  
```
The core is where all the core infrastructure of the compiler exists, minimally. This means,
* The basic structure and logic for a function component (file system, database, tokenizer, parser, etc)
* This is put together by the API, which provides a DSL for configuring the final behavior of the compiler
* If there is a bug with the way a specific part of the compiler works, it is easy to tell if it is in core or API, which makes it easier to find and kill bugs.

There is currently a planned set of features, which will be laid out and explained very soon.

## Database
The database allows for the compiler to load up previously compiled code and do incremental compilation as well as debugging externally with a database viewer (soon to come).
By having a database, this means that any part of the compiler can easily query and insert data for that specific part of the compiler.
It also allows for long term caching of the internals so that it can remember what it has already done, do a compare and swap, and preserve old code that has already been generated, or old states to be repeated without redoing everything.
It provides for predictive behaviors with a constantly changing project, and after the first initial compile (the warm-up), further compilations should become faster.

## Project Structure
The project structure is already implemented. It allows for a base project structure like so:
```
root
  |- libs
  |- src
  |- target
```
However, with CLI args, this can be changed, although the CLI args have not been implemented, the cli arg backbone has been integrated with an API
`silverc -srcDir=src -libsDir=libs -clibsDir=includes -outDir=out`
Which will allow for the creation of projects like this:
```
root
  |- includes
  |- libs
  |- out
  |- src
```
*Note: clibs is meant for C libraries to be used in the project. This is part of a planned drop-in C compiler feature*