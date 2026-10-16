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

import matryoshka.implicits._

import scalaz._

trait EqualT[T[_[_]]] {
  def equal[F[_]: Functor](tf1: T[F], tf2: T[F])(implicit del: Delay[Equal, F]):
      Boolean

  def equalT[F[_]: Functor](delay: Delay[Equal, F]): Equal[T[F]] =
    Equal.equal[T[F]](equal[F](_, _)(Functor[F], delay))
}

@SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
object EqualT {
  trait Ops[T[_[_]], F[_]] {
    def self: T[F]
    def typeClassInstance: EqualT[T]
    def equal(other: T[F])(implicit F: Functor[F], del: Delay[Equal, F]): Boolean =
      typeClassInstance.equal(self, other)
  }
  trait AllOps[T[_[_]], F[_]] extends Ops[T, F]
  trait ToEqualTOps {
    implicit def toEqualTOps[T[_[_]], F[_]](value: T[F])(implicit instance: EqualT[T]): AllOps[T, F] =
      new AllOps[T, F] {
        val self = value
        val typeClassInstance = instance
      }
  }
  object ops extends ToEqualTOps
  object nonInheritedOps extends ToEqualTOps

  def apply[T[_[_]]](implicit instance: EqualT[T]): EqualT[T] = instance

  def recursiveT[T[_[_]]: RecursiveT]: EqualT[T] = new EqualT[T] {
    def equal[F[_]: Functor]
      (tf1: T[F], tf2: T[F])
      (implicit del: Delay[Equal, F]) =
      del(equalT[F](del)).equal(tf1.project, tf2.project)
  }
}
