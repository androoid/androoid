package io.androoid.roo.addon.suite.addon.persistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate DatabaseConfigUtils methods
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AndrooidDatabaseConfig {

}
