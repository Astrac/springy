package astrac.springy

import cats.~>
import cats.free.Free
import language.higherKinds

package object api {
  type IndexIO[T] = Free[IndexIOAst, T]
  type Interpreter[M[_]] = (IndexIOAst ~> M)
}
