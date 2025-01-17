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
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoMap
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo

@Suppress("unused")
class ScreenInjectionGenerator(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
   private lateinit var ksTypeScreenKey: KSType
   private lateinit var ksTypeUnit: KSType
   private lateinit var ksTypeScopedService: KSType
   private var sharedTypesResolved = false

   override fun process(resolver: Resolver): List<KSAnnotated> {
      val screens = resolver.getSymbolsWithAnnotation(ANNOTATION_INJECT_NAVIGATION_SCREEN)

      val (validScreens, invalidScreens) = screens.partition { it.validate() }

      if (validScreens.isNotEmpty() && !sharedTypesResolved) {
         ksTypeScreenKey = resolver.getClassDeclarationByName(SCREEN_KEY_BASE_CLASS.toString())!!.asStarProjectedType()
         ksTypeScopedService = resolver.getClassDeclarationByName(SCOPED_SERVICE_BASE_CLASS.toString())!!.asStarProjectedType()
         ksTypeUnit = resolver.builtIns.unitType
         sharedTypesResolved = true
      }

      for (screen in validScreens) {
         try {
            generateScreenComponent(codeGenerator, screen as KSClassDeclaration)
         } catch (e: Exception) {
            throw IllegalStateException("Failed to generate injection for $screen", e)
         }
      }

      return invalidScreens
   }

   private fun generateScreenComponent(
      codeGenerator: CodeGenerator,
      clas: KSClassDeclaration,
   ) {
      if (clas.isAbstract()) {
         error("@InjectNavigationScreen is not supported for abstract screens")
      }

      val screenClass = clas.toClassName()
      val type = clas.asStarProjectedType()
      val outputClassName = screenClass.simpleName + "Component"
      val screenKeyType =
         type.getScreenKeyIfItExists() ?: error("Class $clas annotated with @InjectNavigationScreen does not extend Screen")

      val constructorParameters = clas.primaryConstructor?.parameters ?: emptyList()

      val screenFactoryProvider = createScreenFactoryProvider(
         screenClass,
         constructorParameters
      )

      val bindScreenFactoryToFactoryMultibindsFunction =
         createBindScreenFactoryToFactoryMultibindsFunction(screenClass)

      val screenRegistrationFunction = createScreenServiceRegistrationFunction(
         screenClass,
         screenKeyType
      )

      val screenFactoryToParentFactoryBindingFunction = createScreenFactoryToParentFactoryBindingFunction(clas)

      val suppressAnnotation = AnnotationSpec
         .builder(ClassName("kotlin", "Suppress"))
         .addMember("\"NAME_SHADOWING\", \"UNUSED_ANONYMOUS_PARAMETER\", \"UNCHECKED_CAST\"")
         .build()

      val contributesToAnnotation = AnnotationSpec.builder(ContributesTo::class)
         .addMember("%T::class", BACKSTACK_SCOPE_ANNOTATION)
         .build()

      val dependencies = listOfNotNull(clas.containingFile)

      FileSpec.builder(screenClass.packageName, outputClassName).apply {
         val componentInterfaceSpec = TypeSpec.interfaceBuilder(outputClassName)
            .addAnnotation(Component::class)
            .addModifiers(KModifier.ABSTRACT)
            .addAnnotation(contributesToAnnotation)
            .addAnnotation(suppressAnnotation)
            .addFunction(bindScreenFactoryToFactoryMultibindsFunction)
            .addFunction(screenFactoryProvider)
            .addFunction(screenRegistrationFunction)
            .also {
               if (screenFactoryToParentFactoryBindingFunction != null) {
                  it.addFunction(screenFactoryToParentFactoryBindingFunction)
               }
            }
            .build()

         addType(componentInterfaceSpec)
      }
         .build()
         .writeTo(codeGenerator, false, dependencies)
   }

   private fun createBindScreenFactoryToFactoryMultibindsFunction(
      screenClass: ClassName
   ): FunSpec {
      val classType = Class::class.asClassName().parameterizedBy(
         WildcardTypeName.producerOf(
            SCREEN_BASE_CLASS.parameterizedBy(
               WildcardTypeName.producerOf(SCREEN_KEY_BASE_CLASS)
            )
         )
      )
      val returnType = Pair::class.asClassName().parameterizedBy(classType, SCREEN_FACTORY.parameterizedBy(STAR))

      return FunSpec.builder("provides${screenClass.simpleName}Factory")
         .returns(returnType)
         .addAnnotation(Provides::class)
         .addAnnotation(IntoMap::class)
         .addParameter("screenFactory", SCREEN_FACTORY.parameterizedBy(screenClass))
         .addStatement(
            "return %T(%T::class.java, screenFactory)",
            Pair::class.asClassName(),
            screenClass
         )
         .build()
   }

   private fun createScreenServiceRegistrationFunction(
      screenClass: ClassName,
      screenKeyType: TypeName
   ): FunSpec {
      val classType = Class::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(SCREEN_KEY_BASE_CLASS))
      val returnType = Pair::class.asClassName().parameterizedBy(classType, SCREEN_REGISTRATION.parameterizedBy(STAR))

      return FunSpec.builder("provides${screenClass.simpleName}Registration")
         .returns(returnType)
         .addAnnotation(Provides::class)
         .addAnnotation(IntoMap::class)
         .addStatement(
            "return %T(%T::class.java, %T(%T::class.java))",
            Pair::class.asClassName(),
            screenKeyType,
            SCREEN_REGISTRATION,
            screenClass,
         )
         .build()
   }

   private fun createScreenFactoryToParentFactoryBindingFunction(
      clas: KSClassDeclaration,
   ): FunSpec? {
      val contributeScreenBindingAnnotation = clas.annotations.firstOrNull {
         it.annotationType.toTypeName() == ANNOTATION_CONTRIBUTES_SCREEN_BINDING
      }

      val screenBindingFunction = if (contributeScreenBindingAnnotation != null) {
         var boundType = contributeScreenBindingAnnotation
            .arguments.firstOrNull { it.name?.asString() == "boundType" }
            ?.run { value as KSType }
            ?.takeIf { it != ksTypeUnit }
            ?.toTypeName()
            ?: clas.getFirstScreenParent()
            ?: error("Invalid @ContributesScreenBinding annotation: $clas does not extend Screen")

         if (boundType is ParameterizedTypeName &&
            boundType.rawType == SCREEN_BASE_CLASS &&
            boundType.typeArguments.firstOrNull() !is ClassName
         ) {
            val screenKey = clas.asStarProjectedType().getScreenKeyIfItExists() ?: error("Unknown screen key for $clas")
            boundType = SCREEN_BASE_CLASS.parameterizedBy(screenKey)
         }

         val returnType = SCREEN_FACTORY.parameterizedBy(boundType)
         FunSpec.builder("provides${clas.simpleName.asString()}FactoryToParentType")
            .returns(returnType)
            .addAnnotation(Provides::class)
            .addParameter("screenFactory", SCREEN_FACTORY.parameterizedBy(clas.toClassName()))
            .addStatement("return screenFactory as %T", returnType)
            .build()
      } else {
         null
      }
      return screenBindingFunction
   }

   private fun createScreenFactoryProvider(
      className: ClassName,
      allConstructorParameters: List<KSValueParameter>
   ): FunSpec {
      val (scopedServiceConstructorParameters,
         nonScopedServiceParameters) = allConstructorParameters.filterNot {
         it.annotations.any { annotation ->
            annotation.annotationType.toTypeName() == ANNOTATION_CURRENT_SCOPE_TAG
         }
      }
         .partition { it.type.isScopedService() }

      val (nestedScreenConstructorParameters,
         externalDependenciesConstructorParameters) =
         nonScopedServiceParameters.partition { it.type.resolve().getScreenKeyIfItExists() != null }

      val factoryLambda =
         createScreenFactoryLamda(
            scopedServiceConstructorParameters,
            externalDependenciesConstructorParameters,
            nestedScreenConstructorParameters,
            allConstructorParameters,
            className
         )

      val allRequiredScopedServices = scopedServiceConstructorParameters
         .filterNot { it.annotations.any { annotation -> annotation.annotationType.toTypeName() == ANNOTATION_INHERITED } }

      val lambdaParameters = "scope, backstack ->"

      return FunSpec.builder("provides${className.simpleName}Factory")
         .returns(SCREEN_FACTORY.parameterizedBy(className))
         .addAnnotation(Provides::class)
         .addParameters(externalDependenciesConstructorParameters.map { parameter ->
            ParameterSpec.builder(
               "${parameter.name!!.asString()}Provider",
               LambdaTypeName.get(returnType = parameter.type.toTypeName())
            )
               .apply {
                  for (annotation in parameter.annotations) {
                     addAnnotation(annotation.toAnnotationSpec())
                  }
               }
               .build()
         })
         .addParameters(nestedScreenConstructorParameters.map { parameter ->
            ParameterSpec.builder(
               "${parameter.name!!.asString()}Factory",
               SCREEN_FACTORY.parameterizedBy(parameter.type.toTypeName())
            )
               .apply {
                  for (annotation in parameter.annotations) {
                     addAnnotation(annotation.toAnnotationSpec())
                  }
               }
               .build()
         })
         .addStatement(
            "return %L(listOf(${allRequiredScopedServices.joinToString { "%T::class.java" }}), " +
               "listOf(${nestedScreenConstructorParameters.joinToString { "%L" }})) { $lambdaParameters \n %L}",
            SCREEN_FACTORY,
            *allRequiredScopedServices.map { it.type.toTypeName() }.toTypedArray(),
            *nestedScreenConstructorParameters.map { "${it.name!!.asString()}Factory" }.toTypedArray(),
            factoryLambda
         )
         .build()
   }

   private fun createScreenFactoryLamda(
      scopedServiceConstructorParameters: List<KSValueParameter>,
      externalDependencyConstructorParameters: List<KSValueParameter>,
      nestedScreenConstructorParameters: List<KSValueParameter>,
      allConstructorParameters: List<KSValueParameter>,
      className: ClassName
   ): CodeBlock {
      val factoryLambda = buildCodeBlock {
         for (service in scopedServiceConstructorParameters) {
            val serviceType = service.type.toTypeName()

            addStatement(
               "val %L = backstack.lookupFromScope<%T>(%L, %T::class.java.name)",
               service.name!!.asString(),
               serviceType,
               "scope",
               serviceType
            )
         }

         for (nestedScreenParameter in nestedScreenConstructorParameters) {
            addStatement(
               "val %L = %L.create(scope, backstack)",
               nestedScreenParameter.name!!.asString(),
               "${nestedScreenParameter.name!!.asString()}Factory",
            )
         }

         for (parameter in allConstructorParameters) {
            if (
               parameter.annotations.any { annotation ->
                  annotation.annotationType.toTypeName() == ANNOTATION_CURRENT_SCOPE_TAG
               }
            ) {
               addStatement(
                  "val %L = %L",
                  parameter.name!!.asString(),
                  "scope"
               )
            }
         }

         for (parameter in externalDependencyConstructorParameters) {
            addStatement(
               "val %L = %LProvider.invoke()",
               parameter.name!!.asString(),
               parameter.name!!.asString()
            )
         }

         val parameters = allConstructorParameters.joinToString { it.name!!.asString() }

         addStatement("%T($parameters)", className)
      }
      return factoryLambda
   }

   private fun KSType.getScreenKeyIfItExists(initialPassedKeyType: KSTypeReference? = null): TypeName? {
      val declaration = declaration
      require(declaration is KSClassDeclaration)

      if (declaration.toClassName() == SCREEN_BASE_CLASS) {
         return arguments.first().type!!.toTypeName()
      }

      for (superReference in declaration.superTypes) {
         val passedKeyType =
            initialPassedKeyType
               ?: superReference.resolve().arguments.firstOrNull { it.type?.isScreenKey() == true }?.type

         if (passedKeyType != null && (superReference.resolve().declaration as? KSClassDeclaration)?.toClassName() == SCREEN_BASE_CLASS) {
            return passedKeyType.toTypeName()
         } else {
            superReference.resolve().getScreenKeyIfItExists(passedKeyType)?.let { return it }
         }
      }

      return null
   }

   private fun KSClassDeclaration.getFirstScreenParent(): TypeName? {
      return getAllSuperTypes().firstOrNull { it.getScreenKeyIfItExists() != null }?.toTypeName()
   }

   private fun KSTypeReference.isScreenKey(): Boolean {
      val typeName = try {
         this.toTypeName()
      } catch (ignored: NoSuchElementException) {
         // Got a non-nameable type such as K
         return false
      }

      if (typeName == SCREEN_KEY_BASE_CLASS) {
         return true
      }

      return ksTypeScreenKey.isAssignableFrom(resolve())
   }

   private fun KSTypeReference.isScopedService(): Boolean {
      if (this.toTypeName() == SCOPED_SERVICE_BASE_CLASS) {
         return true
      }

      return ksTypeScopedService.isAssignableFrom(resolve())
   }
}

@AutoService(SymbolProcessorProvider::class)
class ScreenInjectionGeneratorProvider : SymbolProcessorProvider {
   override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ScreenInjectionGenerator(environment.codeGenerator, environment.logger)
   }
}
