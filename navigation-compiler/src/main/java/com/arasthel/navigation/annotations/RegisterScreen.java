package com.arasthel.navigation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.arasthel.navigation.screen.Screen;
import com.arasthel.navigation.screen.ScreenResult;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RegisterScreen {
	Class<? extends Screen> value();

	Class<? extends ScreenResult> screenResult() default ScreenResult.class;
}
