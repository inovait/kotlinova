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
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.isAbstract

class NavigationSerializable(config: Config) :
   Rule(
      config,
      "ScreenKey, NavigationInstruction, NavigationConditions classes must be serializable",
   ),
   RequiresAnalysisApi {
   override fun visitClassOrObject(klass: KtClassOrObject) {
      if ((klass is KtClass && (klass.isAbstract() || klass.isInterface())) ||
         klass.getSuperTypeList()?.entries?.isNullOrEmpty() != false
      ) {
         return
      }

      if (klass.annotationEntries.none { it.shortName?.asString() == "Serializable" }) {
         analyze(klass) {
            val classSymbol = klass.classSymbol ?: return

            reportOnFoundSuperclass(SCREEN_KEY_CLASS, classSymbol) {
               "Screen key ${klass.name ?: "UNKNOWN"} must have a @Serializable annotation."
            }

            reportOnFoundSuperclass(NAVIGATION_CONDITION_CLASS, classSymbol) {
               "Navigation condition ${klass.name ?: "UNKNOWN"} must have a @Serializable annotation."
            }

            reportOnFoundSuperclass(NAVIGATION_INSTRUCTION_CLASS, classSymbol) {
               "Navigation instruction ${klass.name ?: "UNKNOWN"} must have a @Serializable annotation."
            }
         }
      }
   }

   private inline fun KaSession.reportOnFoundSuperclass(
      classId: ClassId,
      classSymbol: KaClassSymbol,
      message: () -> String,
   ) {
      val screenKey = findClass(classId)
      if (screenKey != null && classSymbol.isSubClassOf(screenKey)) {
         report(
            Finding(
               Entity.from(classSymbol.psi!!),
               message(),
            )
         )
      }
   }
}

private val SCREEN_KEY_CLASS = ClassId.topLevel(FqName("si.inova.kotlinova.navigation.screenkeys.ScreenKey"))
private val NAVIGATION_CONDITION_CLASS = ClassId.topLevel(FqName("si.inova.kotlinova.navigation.conditions.NavigationCondition"))
private val NAVIGATION_INSTRUCTION_CLASS =
   ClassId.topLevel(FqName("si.inova.kotlinova.navigation.instructions.NavigationInstruction"))
