---
layout: docs
title: Getting Started
---

## {{ page.title }}

This fork targets Scala 3.3.7. Build and install the snapshot locally with
`sbt coreJVM/publishLocal` (or `coreJS/publishLocal` for Scala.js).
The Scala 3 snapshot has not been published to Maven Central.

Use native Scala 3 type lambdas, or enable `-Ykind-projector` for the `*`
placeholder syntax used in the examples.

```scala
libraryDependencies += "com.slamdata" %% "matryoshka-core" % "@VERSION@"
```

```scala
import matryoshka._
import matryoshka.implicits._
```

To use a specific fixed-point type (like `Mu`, `Nu`, or `Fix`), you will also need

```scala
import matryoshka.data._
```

However, we generally recommend that you abstract over the fixed-point type, and use the `Recursive` type constraint.

Then hang out on [Gitter](https://gitter.im/slamdata/matryoshka).
