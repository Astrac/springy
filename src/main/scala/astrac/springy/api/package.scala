package astrac.springy

import cats.free.Free

package object api {
  type IndexIO[T] = Free[IndexIOAst, T]
}
