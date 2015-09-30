package io.androoid.roo.addon.suite.addon.fields.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field annotation to include on referenced fields
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface AndrooidReferencedField {

}
