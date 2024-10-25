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
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoMap
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo

@Suppress("unused")
class ScopedServiceInjectionGenerator(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
   override fun process(resolver: Resolver): List<KSAnnotated> {
      val services = resolver.getSymbolsWithAnnotation(ANNOTATION_INJECT_SCOPED_SERVICE.toString())

      val (validServices, invalidServices) = services.partition { it.validate() }

      for (service in validServices) {
         try {
            generateScopedServiceComponent(codeGenerator, service as KSClassDeclaration)
         } catch (e: Exception) {
            throw IllegalStateException("Failed to generate injection for $service", e)
         }
      }

      return invalidServices
   }

   private fun generateScopedServiceComponent(codeGenerator: CodeGenerator, service: KSClassDeclaration) {
      val serviceClassName = service.toClassName()
      val outputClassName = serviceClassName.simpleName + "Component"

      val contributesToAnnotation = AnnotationSpec.builder(ContributesTo::class)
         .addMember("%T::class", APPLICATION_SCOPE_ANNOTATION)
         .build()

      val backstackContributesToAnnotation = AnnotationSpec.builder(ContributesTo::class)
         .addMember("%T::class", BACKSTACK_SCOPE_ANNOTATION)
         .build()

      val serviceMapKeyType = Class::class.asClassName().parameterizedBy(
         WildcardTypeName.producerOf(
            SCOPED_SERVICE_BASE_CLASS
         )
      )
      val returnType =
         Pair::class.asClassName().parameterizedBy(serviceMapKeyType, LambdaTypeName.get(returnType = SCOPED_SERVICE_BASE_CLASS))

      val provideServiceFunction = FunSpec.builder("provide${service.simpleName.asString()}Constructor")
         .returns(returnType)
         .addParameter("serviceFactory", LambdaTypeName.get(returnType = serviceClassName))
         .addAnnotation(Provides::class)
         .addAnnotation(IntoMap::class)
         .addStatement(
            "return %T(%T::class.java, serviceFactory)",
            Pair::class.asClassName(),
            serviceClassName
         )
         .build()

      val provideFromSimpleStackFunction = FunSpec.builder("provide${service.simpleName.asString()}FromSimpleStack")
         .returns(serviceClassName)
         .addParameter("backstack", SIMPLE_STACK_BACKSTACK_CLASS)
         .addAnnotation(Provides::class)
         .addAnnotation(FROM_BACKSTACK_QUALIFIER_ANNOTATION)
         .addCode("return backstack.lookupService(%T::class.java.name)", serviceClassName)
         .build()

      val fromBackstackProviderComponent = TypeSpec.interfaceBuilder(serviceClassName.simpleName + "BackstackComponent")
         .addAnnotation(Component::class)
         .addAnnotation(backstackContributesToAnnotation)
         .addFunction(provideFromSimpleStackFunction)
         .build()

      val globalProviderComponent = TypeSpec.interfaceBuilder(serviceClassName.simpleName + "Component")
         .addAnnotation(Component::class)
         .addAnnotation(contributesToAnnotation)
         .addFunction(provideServiceFunction)
         .build()

      val dependencies = listOfNotNull(service.containingFile)

      FileSpec.builder(service.packageName.asString(), outputClassName).apply {
         addType(fromBackstackProviderComponent)
         addType(globalProviderComponent)
      }
         .build()
         .writeTo(codeGenerator, false, dependencies)
   }
}

@AutoService(SymbolProcessorProvider::class)
class ScopedServiceInjectionGeneratorProvider : SymbolProcessorProvider {
   override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ScopedServiceInjectionGenerator(environment.codeGenerator, environment.logger)
   }
}
