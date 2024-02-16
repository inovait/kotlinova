/*
 * Copyright 2024 INOVA IT d.o.o.
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

package si.inova.kotlinova.navigation.compiler

import com.google.auto.service.AutoService
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.FileWithContent
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.buildFile
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.TypeReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

@OptIn(ExperimentalAnvilApi::class)
@Suppress("unused")
@AutoService(CodeGenerator::class)
class ScopedServiceInjectionGenerator : CodeGenerator {

   override fun generateCode(
      codeGenDir: File,
      module: ModuleDescriptor,
      projectFiles: Collection<KtFile>
   ): Collection<FileWithContent> {
      return projectFiles.classAndInnerClassReferences(module).mapNotNull {
         if (it.isAbstract() || !it.isScopedService()) {
            return@mapNotNull null
         }

         generateScopedServiceModule(codeGenDir, it)
      }.flatten().toList()
   }

   private fun generateScopedServiceModule(
      codeGenDir: File,
      clas: ClassReference.Psi
   ): Collection<FileWithContent> {
      val className = clas.asClassName()
      val packageName = clas.packageFqName.safePackageString(
         dotPrefix = false,
         dotSuffix = false,
      )
      val outputFileName = className.simpleName + "Module"

      val contributesToAnnotation = AnnotationSpec.builder(ContributesTo::class)
         .addMember("%T::class", APPLICATION_SCOPE_ANNOTATION)
         .build()

      val backstackContributesToAnnotation = AnnotationSpec.builder(ContributesTo::class)
         .addMember("%T::class", ACTIVITY_SCOPE_ANNOTATION)
         .build()

      val classKeyAnnotation = AnnotationSpec.builder(ClassKey::class)
         .addMember("%T::class", className)
         .build()

      val bindsServiceFunction = FunSpec.builder("bindConstructor")
         .returns(SCOPED_SERVICE_BASE_CLASS)
         .addParameter("service", className)
         .addAnnotation(Binds::class)
         .addAnnotation(IntoMap::class)
         .addAnnotation(classKeyAnnotation)
         .addModifiers(KModifier.ABSTRACT)
         .build()

      val provideFromSimpleStackFunction = FunSpec.builder("provideFromSimpleStack")
         .returns(className)
         .addParameter("backstack", SIMPLE_STACK_BACKSTACK_CLASS)
         .addAnnotation(Provides::class)
         .addAnnotation(FROM_BACKSTACK_QUALIFIER_ANNOTATION)
         .addCode("return backstack.lookupService(%T::class.java.name)", className)
         .build()

      val fromBackstackProviderModule = TypeSpec.classBuilder(className.simpleName + "BackstackModule")
         .addAnnotation(Module::class)
         .addAnnotation(backstackContributesToAnnotation)
         .addFunction(provideFromSimpleStackFunction)
         .build()

      val content = FileSpec.buildFile(
         packageName = packageName,
         fileName = outputFileName,
         generatorComment = "Automatically generated file. DO NOT MODIFY!"
      ) {
         val moduleInterfaceSpec = TypeSpec.Companion.interfaceBuilder(outputFileName)
            .addAnnotation(Module::class)
            .addAnnotation(contributesToAnnotation)
            .addFunction(bindsServiceFunction)
            .build()

         addType(moduleInterfaceSpec)
         addType(fromBackstackProviderModule)
      }

      return listOf(
         createGeneratedFile(codeGenDir, packageName, outputFileName, content, clas.containingFileAsJavaFile)
      )
   }

   override fun isApplicable(context: AnvilContext): Boolean = true
}

@OptIn(ExperimentalAnvilApi::class)
fun TypeReference.isScopedService(): Boolean {
   val classReference = this.asClassReference()

   return classReference.asClassName() == SCOPED_SERVICE_BASE_CLASS ||
      classReference.directSuperTypeReferences().any { it.isScopedService() }
}

@OptIn(ExperimentalAnvilApi::class)
fun ClassReference.Psi.isScopedService(): Boolean {
   return directSuperTypeReferences().any { it.isScopedService() }
}
