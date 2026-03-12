/*
 * Copyright 2026 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import com.autonomousapps.DependencyAnalysisSubExtension

plugins {
   id("com.autonomousapps.dependency-analysis")
}

configure<DependencyAnalysisSubExtension> {
   issues {
      onAny {
         // Fail build if there are any violations
         severity("fail")

         // Included by kotlin automatically, we can't affect it
         exclude("org.jetbrains.kotlin:kotlin-stdlib")
      }

      onIncorrectConfiguration {
      }

      onUsedTransitiveDependencies {
         // AndroidX Annotations are auto-included with many other AndroidX libraries. It's fine to not explicitly depend on them
         exclude("androidx.annotation:annotation")

         //  Auto included with the parcelize plugin
         exclude("org.jetbrains.kotlin:kotlin-parcelize-runtime")

         // Detekt API dependencies
         exclude("org.jetbrains.kotlin:kotlin-compiler")
         exclude("dev.detekt:detekt-kotlin-analysis-api")
      }
   }
}
