/*
 * Copyright 2025 INOVA IT d.o.o.
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
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provides
import kotlin.reflect.KClass

@Suppress("unused")
class ScopedServiceInjectionGenerator(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
   override fun process(resolver: Resolver): List<KSAnnotated> {
      val services = resolver.getSymbolsWithAnnotation(ANNOTATION_INJECT_SCOPED_SERVICE.toString())

      val (validServices, invalidServices) = services.partition { it.validate() }

      for (service in validServices) {
         try {
            generateScopedServiceProviders(codeGenerator, service as KSClassDeclaration)
         } catch (e: Exception) {
            throw IllegalStateException("Failed to generate injection for $service", e)
         }
      }

      return invalidServices
   }

   private fun generateScopedServiceProviders(codeGenerator: CodeGenerator, service: KSClassDeclaration) {
      val serviceClassName = service.toClassName()
      val outputClassName = serviceClassName.simpleName + "Providers"

      val backstackContributesToAnnotation = AnnotationSpec.builder(ContributesTo::class)
         .addMember("%T::class", BACKSTACK_SCOPE_ANNOTATION)
         .build()

      val provideServiceFunction = createBindsServiceFunction(serviceClassName, serviceClassName)
      val provideFromSimpleStackFunction = createProvideFromSimpleStackFunction(serviceClassName, serviceClassName)

      val contributesBindingAnnotation =
         service.annotations.firstOrNull { it.annotationType.toTypeName() == ContributesBinding::class.asClassName() }

      val fromBackstackProviderProviders = TypeSpec.interfaceBuilder(serviceClassName.simpleName + "BackstackProviders")
         .addAnnotation(backstackContributesToAnnotation)
         .addFunction(provideFromSimpleStackFunction)
         .addFunction(provideServiceFunction)
         .apply {
            if (contributesBindingAnnotation != null) {
               val boundType = boundType(service, contributesBindingAnnotation).toClassName()
               addFunction(createBindsServiceFunction(serviceClassName, boundType))
               addFunction(createProvideFromSimpleStackFunction(serviceClassName, boundType))
            }
         }
         .build()

      val dependencies = listOfNotNull(service.containingFile)

      FileSpec.builder(service.packageName.asString(), outputClassName).apply {
         addType(fromBackstackProviderProviders)
      }
         .build()
         .writeTo(codeGenerator, false, dependencies)
   }

   private fun createBindsServiceFunction(
      serviceClassName: ClassName,
      targetClassName: ClassName
   ): FunSpec {
      return FunSpec.builder("bind${targetClassName.simpleName}Constructor")
         .returns(SCOPED_SERVICE_BASE_CLASS)
         .addParameter("service", targetClassName)
         .addAnnotation(Binds::class)
         .addAnnotation(IntoMap::class)
         .addAnnotation(
            AnnotationSpec.builder(ClassKey::class)
               .addMember("%T::class", targetClassName)
               .build()
         )
         .addModifiers(KModifier.ABSTRACT)
         .build()
   }

   private fun createProvideFromSimpleStackFunction(
      serviceClassName: ClassName,
      targetClassName: ClassName
   ) = FunSpec.builder("provide${targetClassName.simpleName}FromSimpleStack")
      .returns(targetClassName)
      .addParameter("backstack", SIMPLE_STACK_BACKSTACK_CLASS)
      .addAnnotation(Provides::class)
      .addAnnotation(FROM_BACKSTACK_QUALIFIER_ANNOTATION)
      .addCode("return backstack.lookupService(%T::class.java.name)", serviceClassName)
      .build()

   /**
    * From https://github.com/amzn/kotlin-inject-anvil/blob/7f050d078a3f1100cfbeff469f19aef76915efe6/compiler/src/main/kotlin/software/amazon/lastmile/kotlin/inject/anvil/processor/ContributesBindingProcessor.kt
    */
   private fun boundTypeFromAnnotation(annotation: KSAnnotation): KSType? {
      return annotation.arguments.firstOrNull { it.name?.asString() == "boundType" }
         ?.let { it.value as? KSType }
         ?.takeIf {
            it.declaration.requireQualifiedName() != Unit::class.requireQualifiedName()
         }
   }

   /**
    * From https://github.com/amzn/kotlin-inject-anvil/blob/7f050d078a3f1100cfbeff469f19aef76915efe6/compiler/src/main/kotlin/software/amazon/lastmile/kotlin/inject/anvil/processor/ContributesBindingProcessor.kt
    */
   @Suppress("ReturnCount")
   private fun boundType(
      clazz: KSClassDeclaration,
      annotation: KSAnnotation,
   ): KSType {
      boundTypeFromAnnotation(annotation)?.let { return it }

      // The bound type is not defined in the annotation, let's inspect the super types.
      val superTypes = clazz.superTypes
         .map { it.resolve() }
         .filter { it.declaration.requireQualifiedName() != Any::class.requireQualifiedName() }
         .toList()

      when (superTypes.size) {
         0 -> {
            val message = "The bound type could not be determined for " +
               "${clazz.simpleName.asString()}. There are no super types."
            logger.error(message, clazz)
            throw IllegalArgumentException(message)
         }

         1 -> {
            return superTypes.single()
         }

         else -> {
            val message = "The bound type could not be determined for " +
               "${clazz.simpleName.asString()}. There are multiple super types: " +
               superTypes.joinToString { it.declaration.simpleName.asString() } +
               "."
            logger.error(message, clazz)
            throw IllegalArgumentException(message)
         }
      }
   }

   internal fun KSDeclaration.requireQualifiedName(): String =
      requireNotNull(qualifiedName?.asString()) {
         "Qualified name was null for $this"
      }

   internal fun KClass<*>.requireQualifiedName(): String =
      requireNotNull(qualifiedName) {
         "Qualified name was null for $this"
      }
}

@AutoService(SymbolProcessorProvider::class)
class ScopedServiceInjectionGeneratorProvider : SymbolProcessorProvider {
   override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ScopedServiceInjectionGenerator(environment.codeGenerator, environment.logger)
   }
}
