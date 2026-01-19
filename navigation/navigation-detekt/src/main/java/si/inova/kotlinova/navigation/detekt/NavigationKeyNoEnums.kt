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
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass

class NavigationKeyNoEnums(config: Config) : Rule(
   config, "Navigation keys should not contain any enums due to hashcode non-determinism. " +
      "See https://github.com/Zhuinden/simple-stack-compose-integration/issues/29."
), RequiresAnalysisApi {
   override fun visitClass(klass: KtClass) {
      if (!klass.isData()) {
         return
      }

      analyze(klass) {
         val screenKey = findClass(SCREEN_KEY_CLASS) ?: return

         val classSymbol = klass.classSymbol ?: return
         if (classSymbol.isSubClassOf(screenKey)) {
            processParameters(klass, classSymbol, listOfNotNull(classSymbol.name?.toString()))
         }
      }
   }

   private fun KaSession.processParameters(
      originalScreenClass: KtClass,
      resolvedClass: KaClassSymbol,
      parentTree: List<String>
   ) {
      val primaryConstructor = resolvedClass.memberScope.constructors.firstOrNull { it.isPrimary == true } ?: return

      val parameters = primaryConstructor.valueParameters
      for (parameter in parameters) {
         val directSupertypes = parameter.returnType.directSupertypes.mapNotNull { it.expandedSymbol?.classId?.asSingleFqName()?.asString() }
         if (directSupertypes.any { it == "kotlin.Enum" || it == "java.lang.Enum" }) {
            report(
               Finding(
                  Entity.from(originalScreenClass),
                  "Screen key ${parentTree.joinToString(" -> ")} has " +
                     "Enum parameter '${parameter.name}'.",
               )
            )

            continue
         }

         val typeClass = parameter.returnType.expandedSymbol as? KaNamedClassSymbol
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

private val SCREEN_KEY_CLASS = ClassId.topLevel(FqName("si.inova.kotlinova.navigation.screenkeys.ScreenKey"))
