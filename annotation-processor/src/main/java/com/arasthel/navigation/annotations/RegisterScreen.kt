package com.arasthel.navigation.annotations

import com.arasthel.navigation.screen.Screen
import kotlin.reflect.KClass

/**
 * Can be used to register activities, fragments and dialog fragments with the help of an annotation processor.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RegisterScreen(
    val value: KClass<out Screen>,
)