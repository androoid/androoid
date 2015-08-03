package io.androoid.roo.addon.suite.addon.entities.annotations;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate Entity methods
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AndrooidEntity {

	String ID_FIELD_DEFAULT = "id";

	/**
	 * @return the name of the identifier field to use (defaults to
	 *         {@value #ID_FIELD_DEFAULT}; must be provided)
	 */
	String identifierField() default ID_FIELD_DEFAULT;

	/**
	 * @return the class of identifier that should be used (defaults to
	 *         {@link Long}; must be provided)
	 */
	Class<? extends Serializable> identifierType() default Long.class;

}
