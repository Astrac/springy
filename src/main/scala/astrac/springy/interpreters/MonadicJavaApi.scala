package astrac.springy
package interpreters

import api._
import cats.{~>, Monad}
import language.higherKinds
import org.elasticsearch.client.Client

class MonadicJavaApi[M[_]: Monad](client: Client) extends (IndexIOAst ~> M) {

  val monad = implicitly[Monad[M]]

  def apply[T](io: IndexIOAst[T]): M[T] = io match {
    case r @ GetIndexRequest(indexName) => ???
    case r @ DeleteDocumentRequest(_, _, _) => monad.pure(javaapi.deleteDocument(client, r))
    case r @ DeleteIndexRequest(_) => monad.pure(javaapi.deleteIndex(client, r))
    case r @ GetDocumentRequest(_, _, _) => monad.pure(javaapi.getDocument(client, r)(r.readable))
    case r @ IndexDocumentRequest(_, _, _, _, _, _, _, _, _, _, _, _, _, _) => monad.pure(javaapi.indexDocument(client, r)(r.writeable))
    case r @ UpdateDocumentRequest(_, _, _, _) => monad.pure(javaapi.updateDocument(client, r)(r.writeable))
    case r @ BulkRequest(_) => monad.pure(javaapi.bulk(client, r))
    case r @ SearchRequest(_, _, _) => monad.pure(javaapi.search(client, r)(r.readable))
  }

}
