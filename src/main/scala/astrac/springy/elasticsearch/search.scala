package astrac.springy.elasticsearch

import astrac.springy._
import astrac.springy.api._
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import scala.language.higherKinds
import cats.Applicative

trait SearchSupport {
  class SearchExecutable[T: Readable] extends Executable[Executor.JavaApi, SearchRequest[T], SearchResponse[T]] {

    def queryFor(q: Query): QueryBuilder = q match {
      case Query.MatchAll   => QueryBuilders.matchAllQuery()
      case Query.Term(f, v) => QueryBuilders.termQuery(f, v)
    }

    def perform[M[_]: Applicative](executor: Executor.JavaApi, request: SearchRequest[T]): M[SearchResponse[T]] = {
      val result = executor
        .client
        .prepareSearch(request.index)
        .setTypes(request.`type`)
        .setQuery(queryFor(request.query))
        .execute()

      implicitly[Applicative[M]].pure {
        val resp = result.get()
        val hits = resp.getHits()

        SearchResponse(
          ShardInfo(
            resp.getTotalShards(),
            resp.getSuccessfulShards(),
            resp.getFailedShards()
          ),
          SearchHits(
            hits.getTotalHits(),
            hits.getHits().map { h =>
              SearchHit(
                h.getIndex(),
                h.getType(),
                h.getId(),
                implicitly[Readable[T]].fromBytes(h.getSourceRef().array()).getOrElse(???))
            }.toList
          )
        )
      }
    }
  }

  implicit def searchSupport[T: Readable]: Executable[Executor.JavaApi, SearchRequest[T], SearchResponse[T]] = new SearchExecutable
}

trait SearchApiSupport extends Search[Executor.JavaApi] with SearchSupport

object SearchApiSupport extends SearchApiSupport
