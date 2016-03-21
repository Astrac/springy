package astrac.springy

import org.elasticsearch.client.Client
import scala.concurrent.Future
import scala.language.higherKinds
import cats.{Applicative, Id}

trait Executable[E <: Executor, Request, Response] {
  def perform[M[_]: Applicative](executor: E, request: Request): M[Response]
}

class WrappedExecutor[E <: Executor, Request, Response](executable: Executable[E, Request, Response])(executor: E, request: Request) {
  def to[M[_]: Applicative] = executable.perform[M](executor, request)
}

trait Executor {
  def execute[E <: Executor, Request, Response](req: Request)(implicit
    ev: this.type <:< E,
    executable: Executable[E, Request, Response]) = new WrappedExecutor(executable)(this, req)
}

object Executor {
  class JavaApi(val client: Client) extends Executor
}
