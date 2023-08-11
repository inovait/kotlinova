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

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.rules.fqNameOrNull
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperclassesWithoutAny
import org.jetbrains.kotlin.types.typeUtil.getImmediateSuperclassNotAny

class NavigationKeyNoEnums(config: Config) : Rule(config) {
   override val issue: Issue = Issue(
      javaClass.simpleName,
      Severity.Defect,
      "Navigation keys should not contain any enums due to hashcode non-determinism. " +
         "See https://github.com/Zhuinden/simple-stack-compose-integration/issues/29.",
      Debt.FIVE_MINS
   )

   override fun visitClass(klass: KtClass) {
      if (!klass.isData()) {
         return
      }

      val resolvedClass = bindingContext[BindingContext.CLASS, klass] ?: return

      val parents = resolvedClass.getAllSuperclassesWithoutAny()
      if (parents.all { it.fqNameOrNull()?.toString() != SCREEN_KEY_CLASS }) {
         return
      }

      processParameters(klass, resolvedClass, listOf(resolvedClass.name.toString()))
   }

   private fun processParameters(
      originalScreenClass: KtClass,
      resolvedClass: ClassDescriptor,
      parentTree: List<String>
   ) {
      val parameters = resolvedClass.unsubstitutedPrimaryConstructor?.valueParameters ?: emptyList()
      for (parameter in parameters) {
         val parameterType = parameter.type
         val immediateSuperType = parameterType.getImmediateSuperclassNotAny()?.fqNameOrNull()?.toString()
         if (immediateSuperType == "kotlin.Enum" || immediateSuperType == "java.lang.Enum") {
            report(
               CodeSmell(
                  issue,
                  Entity.from(originalScreenClass),
                  "Screen key ${parentTree.joinToString(" -> ")} has " +
                     "Enum parameter '${parameter.name}'.",
               )
            )

            continue
         }

         val typeClass = parameterType.constructor.declarationDescriptor as? ClassDescriptor
         if (typeClass?.isData == true) {
            processParameters(
               originalScreenClass,
               typeClass,
               parentTree + typeClass.name.toString()
            )
         }
      }
   }
}

private const val SCREEN_KEY_CLASS = "si.inova.kotlinova.navigation.screenkeys.ScreenKey"
