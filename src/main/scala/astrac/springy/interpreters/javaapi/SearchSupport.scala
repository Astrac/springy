package astrac.springy
package interpreters
package javaapi

import api._
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders

trait SearchSupport {
  private def queryFor(q: Query): QueryBuilder = q match {
    case Query.MatchAll => QueryBuilders.matchAllQuery()
    case Query.Term(f, v) => QueryBuilders.termQuery(f, v)
  }

  def search[T: Readable](client: Client, request: SearchRequest[T]) = {
    val result = client
      .prepareSearch(request.index)
      .setTypes(request.`type`)
      .setQuery(queryFor(request.query))
      .execute()

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
            implicitly[Readable[T]].fromBytes(h.getSourceRef().array()).getOrElse(???)
          )
        }.toList
      )
    )

  }
}
