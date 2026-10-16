/*
 * Copyright 2014–2018 SlamData Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package matryoshka


import scalaz._, Scalaz._

/** Like `Zip`, but it can fail to merge, so it’s much more general.
  */
trait Merge[F[_]] {
  // TODO[simulacrum#57]: `fa` should be lazy
  def merge[A, B](fa: F[A], fb: => F[B]): Option[F[(A, B)]]

  // TODO[simulacrum#57]: `fa` should be lazy
  def mergeWith[A, B, C, D](
    fa: F[A], fb: => F[B])(g: (A, B) => D)(
    implicit F: Functor[F]):
      Option[F[D]] =
    merge(fa, fb).map(_ ∘ g.tupled)
}

@SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
object Merge {
  trait Ops[F[_], A] {
    def self: F[A]
    def typeClassInstance: Merge[F]
    def merge[B](fb: => F[B]): Option[F[(A, B)]] = typeClassInstance.merge(self, fb)
    def mergeWith[B, C, D](fb: => F[B])(g: (A, B) => D)(implicit F: Functor[F]): Option[F[D]] =
      typeClassInstance.mergeWith[A, B, C, D](self, fb)(g)
  }
  trait AllOps[F[_], A] extends Ops[F, A]
  trait ToMergeOps {
    implicit def toMergeOps[F[_], A](fa: F[A])(implicit F: Merge[F]): AllOps[F, A] =
      new AllOps[F, A] {
        val self = fa
        val typeClassInstance = F
      }
  }
  object ops extends ToMergeOps
  object nonInheritedOps extends ToMergeOps

  def apply[F[_]](implicit instance: Merge[F]): Merge[F] = instance

  implicit def fromTraverse[F[_]: Traverse](implicit E: Equal[F[Unit]]):
      Merge[F] =
    new Merge[F] {
      def merge[A, B](fa: F[A], fb: => F[B]): Option[F[(A, B)]] =
        if (fa.void ≟ fb.void)
          fa.zipWithL(fb)((a, b) => b ∘ ((a, _))).sequence
        else None
    }
}
