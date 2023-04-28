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

package si.inova.kotlinova.navigation.compiler

import com.google.auto.service.AutoService
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.buildFile
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.ParameterReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.asTypeName
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

@Suppress("unused")
@OptIn(ExperimentalAnvilApi::class)
@AutoService(CodeGenerator::class)
class ScreenInjectionGenerator : CodeGenerator {
   override fun generateCode(
      codeGenDir: File,
      module: ModuleDescriptor,
      projectFiles: Collection<KtFile>
   ): Collection<GeneratedFile> {
      return projectFiles.classAndInnerClassReferences(module).mapNotNull {
         if (it.isAbstract()) {
            return@mapNotNull null
         }

         val screenType = it.getScreenKeyIfItExists() ?: return@mapNotNull null

         generateScreenFactory(codeGenDir, it, screenType)
      }.flatten().toList()
   }

   private fun generateScreenFactory(
      codeGenDir: File,
      clas: ClassReference.Psi,
      screenKeyType: ClassReference
   ): Collection<GeneratedFile> {
      val className = clas.asClassName()
      val packageName = clas.packageFqName.safePackageString(
         dotPrefix = false,
         dotSuffix = false,
      )
      val outputFileName = className.simpleName + "Module"

      val contributesToAnnotation = AnnotationSpec.builder(ContributesTo::class)
         .addMember("%T::class", ACTIVITY_SCOPE_ANNOTATION)
         .build()

      val classKeyAnnotation = AnnotationSpec.builder(ClassKey::class)
         .addMember("%T::class", className)
         .build()

      val screenKeyClassKeyAnnotation = AnnotationSpec.builder(ClassKey::class)
         .addMember("%T::class", screenKeyType.asTypeName())
         .build()

      val constructorParameters = clas.constructors.firstOrNull()?.parameters ?: emptyList()

      val bindsScreenFactoryFunction = FunSpec.builder("bindsScreenFactory")
         .addModifiers(KModifier.ABSTRACT)
         .returns(SCREEN_FACTORY.parameterizedBy(STAR))
         .addAnnotation(Binds::class)
         .addAnnotation(IntoMap::class)
         .addParameter("screenFactory", SCREEN_FACTORY.parameterizedBy(className))
         .addAnnotation(classKeyAnnotation)
         .build()

      val screenFactoryFunction = createScreenFactoryProvider(
         className,
         constructorParameters
      )

      val screenRegistrationFunction = if (!screenKeyType.isAbstract()) {
         screenServiceRegistrationFunction(screenKeyClassKeyAnnotation, constructorParameters, className)
      } else {
         null
      }

      val suppressAnnotation = AnnotationSpec
         .builder(ClassName("kotlin", "Suppress"))
         .addMember("\"NAME_SHADOWING\", \"UNUSED_ANONYMOUS_PARAMETER\"")
         .build()

      val content = FileSpec.buildFile(
         packageName = packageName,
         fileName = outputFileName,
         generatorComment = "Automatically generated file. DO NOT MODIFY!"
      ) {
         val companionObject = TypeSpec.companionObjectBuilder()
            .addFunction(screenFactoryFunction)
            .also { if (screenRegistrationFunction != null) it.addFunction(screenRegistrationFunction) }
            .build()

         val moduleInterfaceSpec = TypeSpec.classBuilder(outputFileName)
            .addAnnotation(Module::class)
            .addModifiers(KModifier.ABSTRACT)
            .addAnnotation(contributesToAnnotation)
            .addAnnotation(suppressAnnotation)
            .addFunction(bindsScreenFactoryFunction)
            .addType(companionObject)
            .build()

         addType(moduleInterfaceSpec)
      }

      return listOf(createGeneratedFile(codeGenDir, packageName, outputFileName, content))
   }

   private fun screenServiceRegistrationFunction(
      screenKeyClassKeyAnnotation: AnnotationSpec,
      constructorParameters: List<ParameterReference.Psi>,
      className: ClassName
   ): FunSpec {
      val allRequiredScopedServices = getRequiredScopedServices(constructorParameters)

      return FunSpec.builder("providesScreenRegistration")
         .returns(SCREEN_REGISTRATION.parameterizedBy(STAR))
         .addAnnotation(Provides::class)
         .addAnnotation(IntoMap::class)
         .addAnnotation(screenKeyClassKeyAnnotation)
         .addStatement(
            "return %T(%T::class.java, ${allRequiredScopedServices.joinToString { "%T::class.java" }})",
            SCREEN_REGISTRATION,
            className,
            *allRequiredScopedServices.map { it.type().asTypeName() }.toTypedArray()
         )
         .build()
   }

   private fun createScreenFactoryProvider(
      className: ClassName,
      allConstructorParameters: List<ParameterReference.Psi>
   ): FunSpec {
      val (scopedServiceConstructorParameters,
         nonScopedServiceParameters) =
         allConstructorParameters
            .filterNot {
               it.annotations.any { annotation ->
                  val type = annotation.classReference.asClassName()
                  type == ANNOTATION_CURRENT_SCOPE_TAG
               }
            }
            .partition { it.type().isScopedService() }

      val (nestedScreenConstructorParameters,
         externalDependenciesConstructorParameters) =
         nonScopedServiceParameters.partition { it.type().asClassReferenceOrNull()?.getScreenKeyIfItExists() != null }

      val factoryLambda =
         createScreenFactoryLamda(
            scopedServiceConstructorParameters,
            nestedScreenConstructorParameters,
            allConstructorParameters,
            className
         )

      val lambdaParameters = "scope, backstack ->"

      return FunSpec.builder("providesScreenFactory")
         .returns(SCREEN_FACTORY.parameterizedBy(className))
         .addAnnotation(Provides::class)
         .addParameters(externalDependenciesConstructorParameters.map { parameter ->
            ParameterSpec.builder(parameter.name, parameter.type().asTypeName())
               .apply {
                  for (annotation in parameter.annotations) {
                     addAnnotation(annotation.toAnnotationSpec())
                  }
               }
               .build()
         })
         .addParameters(nestedScreenConstructorParameters.map { parameter ->
            ParameterSpec.builder("${parameter.name}Factory", SCREEN_FACTORY.parameterizedBy(parameter.type().asTypeName()))
               .apply {
                  for (annotation in parameter.annotations) {
                     addAnnotation(annotation.toAnnotationSpec())
                  }
               }
               .build()
         })
         .addStatement(
            "return %T { $lambdaParameters \n %L}",
            SCREEN_FACTORY,
            factoryLambda
         )
         .build()
   }

   private fun createScreenFactoryLamda(
      scopedServiceConstructorParameters: List<ParameterReference.Psi>,
      nestedScreenConstructorParameters: List<ParameterReference.Psi>,
      allConstructorParameters: List<ParameterReference.Psi>,
      className: ClassName
   ): CodeBlock {
      val factoryLambda = buildCodeBlock {
         for (service in scopedServiceConstructorParameters) {
            val serviceType = service.type().asTypeName()

            addStatement(
               "val %L = backstack.%M<%T>(%L, %T::class.java.name)",
               service.name,
               LOOKUP_FROM_SCOPE_WITH_INHERITANCE,
               serviceType,
               "scope",
               serviceType
            )
         }

         for (nestedScreenParameter in nestedScreenConstructorParameters) {
            addStatement(
               "val %L = %L.create(scope, backstack)",
               nestedScreenParameter.name,
               "${nestedScreenParameter.name}Factory",
            )
         }

         for (parameter in allConstructorParameters) {
            if (
               parameter.annotations.any { annotation ->
                  annotation.classReference.asClassName() == ANNOTATION_CURRENT_SCOPE_TAG
               }
            ) {
               addStatement(
                  "val %L = %L",
                  parameter.name,
                  "scope"
               )
            }
         }

         addStatement("%T(${allConstructorParameters.joinToString { it.name }})", className)
      }
      return factoryLambda
   }

   private fun getRequiredScopedServices(constructorParameters: List<ParameterReference.Psi>): List<ParameterReference> {
      val constructorsOfAllNestedScreens = constructorParameters
         .map { it.type().asClassReference() }
         .filter { it.getScreenKeyIfItExists() != null }
         .map { it.constructors.firstOrNull()?.parameters ?: emptyList() }

      val mergedConstructors = constructorsOfAllNestedScreens + listOf(constructorParameters)
      return mergedConstructors.flatMap { constructor ->
         constructor
            .filterNot { it.annotations.any { annotation -> annotation.classReference.asClassName() == ANNOTATION_INHERITED } }
            .filter { it.type().isScopedService() }
      }
   }

   private fun ClassReference.getScreenKeyIfItExists(initialPassedKeyType: ClassReference? = null): ClassReference? {
      for (superReference in directSuperTypeReferences()) {
         val passedKeyType = initialPassedKeyType ?: superReference.unwrappedTypes
            .mapNotNull { it.asClassReferenceOrNull() }
            .firstOrNull { it.isKey() }

         val superClassReference = superReference.asClassReference()

         if (passedKeyType != null && superClassReference.asClassName() == SCREEN_BASE_CLASS) {
            return passedKeyType
         } else {
            superClassReference.getScreenKeyIfItExists(passedKeyType)?.let { return it }
         }
      }

      return null
   }

   private fun ClassReference.isKey(): Boolean {
      if (this.asClassName() == SCREEN_KEY_BASE_CLASS) {
         return true
      }

      for (superReference in directSuperTypeReferences()) {
         val superClassReference = superReference.asClassReference()

         if (superClassReference.asClassName() == SCREEN_KEY_BASE_CLASS) {
            return true
         } else {
            superClassReference.isKey().let { if (it) return true }
         }
      }

      return false
   }

   override fun isApplicable(context: AnvilContext): Boolean = true
}
