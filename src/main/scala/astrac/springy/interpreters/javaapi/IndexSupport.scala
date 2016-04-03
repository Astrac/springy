package astrac.springy
package interpreters
package javaapi

import api._
import org.elasticsearch.action.admin.indices.delete.{DeleteIndexRequest => EsDeleteIndexRequest}
import org.elasticsearch.client.Client

trait IndexSupport {
  def deleteIndex(client: Client, request: DeleteIndexRequest) = {
    val req = new EsDeleteIndexRequest(request.indexName)

    val resp = client
      .admin()
      .indices()
      .delete(request.options.fold(req)(req.indicesOptions))
      .actionGet()

    AcknowledgedResponse(resp.isAcknowledged())
  }
}
