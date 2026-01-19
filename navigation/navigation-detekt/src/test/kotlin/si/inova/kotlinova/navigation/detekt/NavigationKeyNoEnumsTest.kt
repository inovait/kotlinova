/*
 * Copyright 2023 INOVA IT d.o.o.
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

package si.inova.kotlinova.navigation.detekt

import dev.detekt.api.Config
import dev.detekt.test.junit.KotlinCoreEnvironmentTest
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.KotlinEnvironmentContainer
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldContain
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
class NavigationKeyNoEnumsTest(val env: KotlinEnvironmentContainer) {
   private val subject = NavigationKeyNoEnums(Config.empty)

   @Language("kotlin")
   val screenKey = """
      package si.inova.kotlinova.navigation.screenkeys
      
      abstract class ScreenKey
   """.trimIndent()

   @Test
   fun `Allow enums inside non-Screen data classes`() {
      @Language("kotlin")
      val code = """
         $screenKey 
         enum class Enum {A, N}
         data class Test(val enum: Enum)
      """.trimIndent()

      val findings = subject.lintWithContext(env, code)

      findings.shouldBeEmpty()
   }

   @Test
   fun `Allow Screen data classes without Enums`() {
      @Language("kotlin")
      val code = """
         $screenKey
         data class Test(val res: String): ScreenKey()
      """.trimIndent()

      val findings = subject.lintWithContext(env, code)

      findings.shouldBeEmpty()
   }

   @Test
   fun `Warn about enum inside data class key`() {
      @Language("kotlin")
      val code = """
         $screenKey
         
         enum class Enum {A, N}
         data class Test(val enum: Enum): ScreenKey()
      """.trimIndent()

      val finding = subject.lintWithContext(env, code)

      finding.shouldHaveSize(1).first().apply {
         message shouldContain "Test"
         message shouldContain "enum"
      }
   }

   @Test
   fun `Warn about enum when extending screen in subclass`() {
      @Language("kotlin")
      val code = """
         $screenKey
         
         enum class Enum {A, N}
         abstract class TestB(open val enum: Enum): ScreenKey()
         data class TestA(override val enum: Enum): TestB(enum)
      """.trimIndent()

      val finding = subject.lintWithContext(env, code)

      finding.shouldHaveSize(1).first().apply {
         message shouldContain "Test"
         message shouldContain "enum"
      }
   }

   @Test
   fun `Warn about enum inside nested data class key`() {
      @Language("kotlin")
      val code = """
         $screenKey
         
         enum class Enum() {A, N}
         data class TestB(val enum: Enum)
         data class TestA(val b: TestB): ScreenKey()
      """.trimIndent()

      val finding = subject.lintWithContext(env, code)

      finding.shouldHaveSize(1).first().apply {
         // Mention both classes to ease debugging
         message shouldContain "TestA"
         message shouldContain "TestB"
         message shouldContain "enum"
      }
   }
}
