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



/** Provides a type describing the pattern functor of some {co}recursive type
  * `T`. For standard fixed-point types like [[matryoshka.data.Fix]],
  * `Based[Fix[F]]#Base` is simply `F`. However, directly recursive types
  * generally have a less obvious pattern functor. E.g., `Based[Cofree[F,
  * A]]#Base` is `EnvT[A, F, *]`.
  */
trait Based[T] {
  type Base[A]
}

@SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
object Based {
  trait Ops[T] {
    def self: T
    def typeClassInstance: Based[T]
  }
  trait AllOps[T] extends Ops[T]
  trait ToBasedOps {
    implicit def toBasedOps[T](value: T)(implicit instance: Based[T]): AllOps[T] =
      new AllOps[T] {
        val self = value
        val typeClassInstance = instance
      }
  }
  object ops extends ToBasedOps
  object nonInheritedOps extends ToBasedOps

  def apply[T](implicit instance: Based[T]): Based[T] = instance

}
