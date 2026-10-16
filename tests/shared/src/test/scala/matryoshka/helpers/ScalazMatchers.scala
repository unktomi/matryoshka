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

package matryoshka.helpers

import org.specs2.matcher.{Expectable, Matcher, MatchResult}
import scalaz.{Equal, Show}

/** Keep the original suite's Scalaz equality, including its custom Exp equality. */
trait ScalazMatchers {
  def equal[T: Equal: Show](expected: T): Matcher[T] = new Matcher[T] {
    def apply[S <: T](actual: Expectable[S]): MatchResult[S] =
      result(
        Equal[T].equal(expected, actual.value),
        s"${Show[T].shows(actual.value)} === ${Show[T].shows(expected)}",
        s"${Show[T].shows(actual.value)} !== ${Show[T].shows(expected)}",
        actual)
  }
}
