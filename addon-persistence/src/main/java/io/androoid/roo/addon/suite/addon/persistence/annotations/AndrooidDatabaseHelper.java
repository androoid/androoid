package io.androoid.roo.addon.suite.addon.persistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate DatabaseHelper methods
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AndrooidDatabaseHelper {

	/**
	 * @return an array of classes, with each class being an entity that should
	 *         be created as a DAO on DatabaseHelper {@value #ID_FIELD_DEFAULT}; must be provided)
	 */
	Class[] entities();

}
