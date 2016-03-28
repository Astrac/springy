package astrac.springy

import api._
import cats.{~>, Monad}
import elasticsearch.UnsafeClient
import language.higherKinds
import org.elasticsearch.client.Client

class MonadicJavaApi[M[_]: Monad](client: Client) extends (IndexIOAst ~> M) {

  val monad = implicitly[Monad[M]]

  def apply[T](io: IndexIOAst[T]): M[T] = io match {
    case r @ GetIndexRequest(indexName) => ???
    case r @ DeleteDocumentRequest(_, _, _) => monad.pure(UnsafeClient.deleteDocument(client, r))
    case r @ DeleteIndexRequest(_) => ???
    case r @ GetDocumentRequest(_, _, _) => monad.pure(UnsafeClient.getDocument(client, r)(r.readable))
    case r @ IndexDocumentRequest(_, _, _, _, _, _, _, _, _, _, _, _, _, _) => monad.pure(UnsafeClient.indexDocument(client, r)(r.writeable))
    case r @ SearchRequest(_, _, _) => monad.pure(UnsafeClient.search(client, r)(r.readable))
    case r @ UpdateDocumentRequest(_, _, _, _) => monad.pure(UnsafeClient.updateDocument(client, r)(r.writeable))
  }

}
