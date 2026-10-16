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

trait ShowT[T[_[_]]] {
  def show[F[_]: Functor](tf: T[F])(implicit del: Delay[Show, F]): Cord =
    Cord(shows(tf))

  def shows[F[_]: Functor](tf: T[F])(implicit del: Delay[Show, F]): String =
    show(tf).toString

  def showT[F[_]: Functor](delay: Delay[Show, F]): Show[T[F]] =
    Show.show[T[F]](show[F](_)(Functor[F], delay))
}

@SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
object ShowT {
  trait Ops[T[_[_]], F[_]] {
    def self: T[F]
    def typeClassInstance: ShowT[T]
    def show(implicit F: Functor[F], del: Delay[Show, F]): Cord = typeClassInstance.show(self)
    def shows(implicit F: Functor[F], del: Delay[Show, F]): String = typeClassInstance.shows(self)
  }
  trait AllOps[T[_[_]], F[_]] extends Ops[T, F]
  trait ToShowTOps {
    implicit def toShowTOps[T[_[_]], F[_]](value: T[F])(implicit instance: ShowT[T]): AllOps[T, F] =
      new AllOps[T, F] {
        val self = value
        val typeClassInstance = instance
      }
  }
  object ops extends ToShowTOps
  object nonInheritedOps extends ToShowTOps

  def apply[T[_[_]]](implicit instance: ShowT[T]): ShowT[T] = instance

  def recursiveT[T[_[_]]: RecursiveT]: ShowT[T] = new ShowT[T] {
    override def show[F[_]: Functor](tf: T[F])(implicit del: Delay[Show, F]) =
      tf.cata(del(Cord.CordShow).show)
  }
}
