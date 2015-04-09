_springy_ is a library that attempts to provide a scala-esque way of connecting to Elasticsearch. It also serves as a personal playground for experimenting with Scalaz and more advanced scala concepts in general. Some of the goals of the project are:

* Provide a set of immutable scala models for the ES APIs in form of `*Request`/`*Response` case classes
* Provide a simple way to marshall/unmarshall content to/from ES allowing the use of any json library (spray-json will be supported out of the box)
* Provide a test harness to enable in-process testing against real Elasticsearch instances
* Be protocol independent and provide implementations for the Java API and for HTTP (through akka-http)
* Be container-independent and allow the user to select which kind of container to use for the response at call time

This library has been inspired by some work I did in my previous company; the repository for that project can be found [here](https://github.com/blinkboxbooks/elastic-http/)

# State of the project

At the moment the project is in its very early stages and its use is recommended only for experimenting with the library's approach or contributing to it.

## API support

At the moment it supports the Elasticsearch Document API in its simplest use cases and the Search API only to perform _term_ or _matchall_ queries.

## Protocol support

It is possible to use only the Java API client but the structure to allow other clients is already in place: in the `api` package there are some abstract traits that need to be implemented and in the `elasticsearch` package there is the implementation for the Java API.

## Result containers

The library is also not bound to any _result container_. This means that you can decide to run your requests and get the result in a `Future`, in a `Try`, in a `Task` and in anything for which scalaz defines an `Applicative` instance. This allows also for selecting between synchronous and asynchronous semantics and easier testing.

## Testing

There are some basic test scenarios already implemented and they provide also some examples of how the library can be used. These tests rely on an embedded ES instance and execute real queries against it. This means that the tests could get quite heavy on memory and CPU.
