_springy_ is a library that attempts to provide a scala-esque way of connecting to Elasticsearch. It also serves as a personal playground for experimenting with cats and more advanced scala concepts in general. Some of the goals of the project are:

* Provide a set of immutable scala models for the ES APIs in form of `*Request`/`*Response` case classes
* Provide a simple way to marshall/unmarshall content to/from ES allowing the use of any json library (circe will be supported out of the box)
* Provide a test harness to enable in-process testing against real Elasticsearch instances
* Be protocol independent and provide implementations for the Java API and for HTTP
* Be container-independent and allow the user to select which kind of container to use for the response at call time

# State of the project

At the moment the project is in its very early stages and its use is recommended only for experimenting with the library's approach or contributing to it.

## API support

At the moment it supports the Elasticsearch Document API in its simplest use cases and the Search API only to perform _term_ or _matchall_ queries.

## Protocol support

The ES API is modelled through an AST whose parent trait is `IndexIO` and that is lifted in a Free Monad. The library provides a `MonadicJavaApi` interpreter that supports any container provided with a cats `Monad` instance and that only uses the `pure` function to wrap calls to the Java API. Interpreters can easily be defined for other scenarios (e.g. HTTP) just as natural transformations of the `IndexIO` AST.

## Testing

There are some basic test scenarios already implemented and they provide also some examples of how the library can be used. These tests rely on an embedded ES instance and execute real queries against it. This means that the tests could get quite heavy on memory and CPU.
