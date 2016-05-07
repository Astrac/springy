# Springy [![Build Status](https://travis-ci.org/Astrac/springy.svg?branch=master)](https://travis-ci.org/Astrac/springy)

_springy_ is a library that attempts to provide a scala-esque way of connecting to Elasticsearch. It also serves as a personal playground for experimenting with cats and more advanced scala concepts in general. Some of the goals of the project are:

* Provide a set of immutable scala models for the ES APIs in form of `*Request`/`*Response` case classes
* Provide a simple way to marshall/unmarshall content to/from ES allowing the use of any json library (circe will be supported out of the box)
* Provide a test harness to enable in-process testing against real Elasticsearch instances
* Be protocol independent and provide implementations for the Java API and for HTTP
* Be container-independent and allow the user to select which kind of container to use for the response at call time

# Example usage

Minimal example: index a document and get it back using circe auto-derivation to produce JSON documents for elasticsearch and an unsafe synchronous interpreter:

```scala
import astrac.springy.api.IndexIO._
import astrac.springy.CirceSerialization._
import cats.std.future._
import io.circe.generic.auto._
import org.elasticsearch.client.Client
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class Book(isbn: String, title: String, author: String)

val sirensOfTitan = Book("0000000000000", "The Sirens Of Titan", "Kurt Vonnegut")

// Type is: IndexIO[(IndexDocumentResponse, GetDocumentResponse[T])]
val io = for {
  idx <- indexDocument("es_test", "book", None, sirensOfTitan)
  get <- getDocument[Book]("es_test", "book", idx._id)
} yield (idx, get)

val client: Client = ??? // The JavaAPI elasticsearch client

val unsafeInterpreter = new MonadicJavaApi[Future](client)

// Type is: Future[(IndexDocumentResponse, GetDocumentResponse[T])]
val result = io.foldMap(unsafeInterpreter)

val (idxResp, getResp) = Await.result(result, 1.seconds)

// What to expect from this - scalatest style

idxResp._index shouldEqual "es_test"
idxResp._type shouldEqual "book"
idxResp._version shouldEqual 1
idxResp.created shouldBe true

getResp._index shouldEqual "es_test"
getResp._type shouldEqual "book"
getResp._version shouldEqual 1
getResp._id shouldEqual idxResp._id
getResp.found shouldBe true
getResp.document shouldEqual Some(sirensOfTitan)
```

# Project's structure

_springy_ leverages the free monad to provide an AST that describes the operations that need to happen. This AST is represented via the sealed hierarchy of traits and case classes with at the topmost `IndexIOAst`; the provided DSL lifts this AST in a free monad named `IndexIO`.

To run the computation you can use any mean that is valid for running free monads; provided in the implementation there is a naive `MonadicJavaApi` interpreter that uses the `pure` function of a provided monad type to wrap the lower-level invocations.

# State of the project

At the moment the project is in its very early stages and its use is recommended only for experimenting with the library's approach or contributing to it.

## API support

At the moment it supports the Elasticsearch Document API in its simplest use cases and the Search API only to perform _term_ or _matchall_ queries. It also supports native queries (i.e. queries coming from the official Java API).

## Protocol support

The ES API is modelled through an AST whose parent trait is `IndexIO` and that is lifted in a Free Monad. The library provides a `MonadicJavaApi` interpreter that supports any container provided with a cats `Monad` instance and that only uses the `pure` function to wrap calls to the Java API. Interpreters can easily be defined for other scenarios (e.g. HTTP) just as natural transformations of the `IndexIO` AST. It remains to be researched whether or not it is possible (or desirable) to support the native query type over HTTP.

## Testing

There are some basic test scenarios already implemented and they provide also some examples of how the library can be used. These tests rely on an embedded ES instance and execute real queries against it. This means that the tests could get quite heavy on memory and CPU.
