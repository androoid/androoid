package io.androoid.roo.addon.suite.addon.activities.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate ActionBarCallback class methods
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AndrooidActionBarCallback {

}
