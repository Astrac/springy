package astrac.springy
package interpreters
package javaapi

import api._
import org.elasticsearch.action.admin.indices.delete.{DeleteIndexRequest => EsDeleteIndexRequest}
import org.elasticsearch.client.Client

trait IndexSupport {
  def deleteIndex(client: Client, request: DeleteIndexRequest) = {
    val resp = client
      .admin()
      .indices()
      .delete(new EsDeleteIndexRequest(request.indexName))
      .actionGet()

    AcknowledgedResponse(resp.isAcknowledged())
  }
}
