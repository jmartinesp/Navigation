package com.arasthel.annotation_processor

import com.arasthel.navigation.annotations.RegisterScreen
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*

class RegisterScreenAnnotationProcessor: SymbolProcessor {

    companion object {
        val ANNOTATION_NAME = RegisterScreen::class.qualifiedName!!

        private const val PACKAGE = "com.arasthel.navigation.screen"
        private const val CLASS_NAME = "GeneratedScreenRegistry"
        private const val SUPERCLASS_NAME = "ScreenRegistry"
    }

    lateinit var codeGenerator: CodeGenerator
    lateinit var logger: KSPLogger

    override fun init(
        options: Map<String, String>,
        kotlinVersion: KotlinVersion,
        codeGenerator: CodeGenerator,
        logger: KSPLogger
    ) {
        this.codeGenerator = codeGenerator
        this.logger = logger
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val mappings = mutableMapOf<KSName, AnnotatedClass>()
        val annotatedClassesToReturn = resolver.getSymbolsWithAnnotation(ANNOTATION_NAME)
        logger.info("Found ${annotatedClassesToReturn.count()} classes")

        val annotatedClasses = annotatedClassesToReturn
            .mapNotNull { it as? KSClassDeclaration }

        if (annotatedClasses.isEmpty()) return emptyList()

        val activityTypeReference = resolver.getClassDeclarationByName(AnnotatedClass.Activity.type)!!.asType(emptyList())
        val fragmentTypeReference = resolver.getClassDeclarationByName(AnnotatedClass.Fragment.type)!!.asType(emptyList())

        val annotationType = resolver.getClassDeclarationByName(ANNOTATION_NAME)!!.asType(emptyList())
        annotatedClasses.forEach { clazz ->
            val annotatedClassName = clazz.qualifiedName!!

            val annotation = clazz.annotations.firstOrNull { it.annotationType.resolve() == annotationType }
                ?: error("Cannot resolve annotation")
            val screenArg = annotation.arguments.firstOrNull()
                ?: error("Cannot resolve Screen in RegisterScreen annotation for ${annotatedClassName.fullName}. Arguments: ${annotation.arguments}.")
            val screenType = (screenArg.value as KSType).declaration

            logger.info("Registering ${annotatedClassName.getShortName()}.")
            val screenName = screenType.qualifiedName!!

            val previousDeclaration = mappings[screenName]
            if (previousDeclaration != null) {
                error("Screen ${screenName.fullName} already registered. First declaration in ${previousDeclaration.className.fullName}, last in ${annotatedClassName.fullName}.")
            }

            val superTypes = clazz.getAllSuperTypes()
            val annotatedClass = when {
                superTypes.contains(activityTypeReference) -> AnnotatedClass.Activity(annotatedClassName)
                superTypes.contains(fragmentTypeReference) -> AnnotatedClass.Fragment(annotatedClassName)
                else -> error("Annotated class is not either a Fragment or an Activity")
            }

            mappings[screenName] = annotatedClass
        }

        val dependencyFiles = annotatedClasses.mapNotNull { it.containingFile }
        val dependencies = Dependencies(true, *dependencyFiles.toTypedArray())

        val output = codeGenerator.createNewFile(dependencies, PACKAGE, CLASS_NAME, "kt")
        val bufferedOutput = output.bufferedWriter()

        val code = CodeBlock.builder().apply {
            mappings.forEach { (screenName, annotatedClass) ->
                val componentClass = ClassName(annotatedClass.className.getQualifier(), annotatedClass.className.getShortName())
                val screenClass = ClassName(screenName.getQualifier(), screenName.getShortName())
                val statement = when (annotatedClass) {
                    is AnnotatedClass.Activity -> "registerActivity(%T::class.java, %T::class.java)"
                    is AnnotatedClass.Fragment -> "registerFragment(%T::class.java, %T::class.java)"
                }
                addStatement(statement, screenClass, componentClass)
            }
        }.build()

        val fileSpec = FileSpec.builder(PACKAGE, CLASS_NAME)
            .addType(
                TypeSpec.classBuilder(CLASS_NAME)
                    .addFunction(
                        FunSpec.constructorBuilder()
                            .addCode(code)
                            .build()
                    )
                    .superclass(ClassName(PACKAGE, SUPERCLASS_NAME))
                    .build()
            )
            .build()

        fileSpec.writeTo(bufferedOutput)
        bufferedOutput.close()

        return emptyList()
    }

    sealed class AnnotatedClass(val className: KSName) {
        class Activity(className: KSName): AnnotatedClass(className) {
            companion object {
                val type: String = "android.app.Activity"
            }
        }
        class Fragment(className: KSName): AnnotatedClass(className) {
            companion object {
                val type: String = "androidx.fragment.app.Fragment"
            }
        }
    }

    val KSName.fullName: String get() {
        return "${getQualifier()}.${getShortName()}"
    }
}