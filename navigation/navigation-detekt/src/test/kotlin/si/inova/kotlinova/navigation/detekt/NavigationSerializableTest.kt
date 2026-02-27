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
class NavigationSerializableTest(val env: KotlinEnvironmentContainer) {
   private val subject = NavigationSerializable(Config.empty)

   @Language("kotlin")
   val baseClasses = arrayOf(
      """
         package si.inova.kotlinova.navigation.screenkeys
         
         abstract class ScreenKey
      """.trimIndent(),
      """
         package si.inova.kotlinova.navigation.conditions
         
         interface NavigationCondition
      """.trimIndent(),
      """
         package si.inova.kotlinova.navigation.instructions
         
         abstract class NavigationInstruction
      """.trimIndent(),
      """
         package kotlinx.serialization
         
         public final annotation class Serializable
      """.trimIndent(),
   )

   @Test
   fun `Do not complain about class not part of the ones that need serializing`() {
      @Language("kotlin")
      val code = """
         data class Test(val argument: Int)
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldBeEmpty()
   }

   @Test
   fun `Do not complain about serializable ScreenKey`() {
      @Language("kotlin")
      val code = """
         import si.inova.kotlinova.navigation.screenkeys.ScreenKey
         import kotlinx.serialization.Serializable

         @Serializable
         data class Test(val argument: Int): ScreenKey()
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldBeEmpty()
   }

   @Test
   fun `Complain about non-serializable ScreenKey`() {
      @Language("kotlin")
      val code = """
         import si.inova.kotlinova.navigation.screenkeys.ScreenKey

         data class Test(val argument: Int): ScreenKey()
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldHaveSize(1).first().apply {
         message shouldContain "Test"
         message shouldContain "Serializable"
      }
   }

   @Test
   fun `Complain about non-serializable ScreenKey object`() {
      @Language("kotlin")
      val code = """
         import si.inova.kotlinova.navigation.screenkeys.ScreenKey

         data object Test: ScreenKey()
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldHaveSize(1).first().apply {
         message shouldContain "Test"
         message shouldContain "Serializable"
      }
   }

   @Test
   fun `Complain about non-serializable class that is not a direct subclass of a ScreenKey`() {
      @Language("kotlin")
      val code = """
         import si.inova.kotlinova.navigation.screenkeys.ScreenKey

         abstract class TestAbstract: ScreenKey() 
         data class Test(val argument: Int): TestAbstract()
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldHaveSize(1).first().apply {
         message shouldContain "Test"
         message shouldContain "Serializable"
      }
   }

   @Test
   fun `Do not complain about serializable NavigationCondition`() {
      @Language("kotlin")
      val code = """
         import si.inova.kotlinova.navigation.conditions.NavigationCondition
         import kotlinx.serialization.Serializable

         @Serializable
         data class Test(val argument: Int): NavigationCondition
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldBeEmpty()
   }

   @Test
   fun `Complain about non-serializable NavigationCondition`() {
      @Language("kotlin")
      val code = """
         import si.inova.kotlinova.navigation.conditions.NavigationCondition

         data class Test(val argument: Int): NavigationCondition
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldHaveSize(1).first().apply {
         message shouldContain "Test"
         message shouldContain "Serializable"
      }
   }

   @Test
   fun `Complain about non-serializable NavigationCondition object`() {
      @Language("kotlin")
      val code = """
         import si.inova.kotlinova.navigation.conditions.NavigationCondition

         data object Test: NavigationCondition
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldHaveSize(1).first().apply {
         message shouldContain "Test"
         message shouldContain "Serializable"
      }
   }

   @Test
   fun `Complain about non-serializable class that is not a direct subclass of a NavigationCondition`() {
      @Language("kotlin")
      val code = """
         import si.inova.kotlinova.navigation.conditions.NavigationCondition

         abstract class TestAbstract: NavigationCondition 
         data class Test(val argument: Int): TestAbstract()
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldHaveSize(1).first().apply {
         message shouldContain "Test"
         message shouldContain "Serializable"
      }
   }

   @Test
   fun `Do not complain about serializable NavigationInstruction`() {
      @Language("kotlin")
      val code = """
         import si.inova.kotlinova.navigation.instructions.NavigationInstruction
         import kotlinx.serialization.Serializable

         @Serializable
         data class Test(val argument: Int): NavigationInstruction()
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldBeEmpty()
   }

   @Test
   fun `Complain about non-serializable NavigationInstruction`() {
      @Language("kotlin")
      val code = """
         import si.inova.kotlinova.navigation.instructions.NavigationInstruction

         data class Test(val argument: Int): NavigationInstruction()
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldHaveSize(1).first().apply {
         message shouldContain "Test"
         message shouldContain "Serializable"
      }
   }

   @Test
   fun `Complain about non-serializable NavigationInstruction object`() {
      @Language("kotlin")
      val code = """
         import si.inova.kotlinova.navigation.instructions.NavigationInstruction

         data object Test: NavigationInstruction()
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldHaveSize(1).first().apply {
         message shouldContain "Test"
         message shouldContain "Serializable"
      }
   }

   @Test
   fun `Complain about non-serializable class that is not a direct subclass of a NavigationInstruction`() {
      @Language("kotlin")
      val code = """
         import si.inova.kotlinova.navigation.instructions.NavigationInstruction

         abstract class TestAbstract: NavigationInstruction() 
         data class Test(val argument: Int): TestAbstract()
      """.trimIndent()

      val findings = subject.lintWithContext(env, code, *baseClasses)

      findings.shouldHaveSize(1).first().apply {
         message shouldContain "Test"
         message shouldContain "Serializable"
      }
   }
}
