package io.androoid.roo.addon.suite.addon.fields;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Interface of Androoid entities commands that are available via the Roo shell.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public interface AndrooidFieldsOperations {

	/**
	 * Indicates if androoid field command should be available.
	 * 
	 * @return true if it should be available, otherwise false
	 */
	boolean isFieldCreationAvailable();

	/**
	 * Creates new field on entity
	 * 
	 * @param The entity where field will be added
	 * @param The fieldName to use for this field. 
	 * @param The field type that will be used for the field 
	 */
	void createField(JavaType entity, JavaSymbolName fieldName, JavaType fieldType);
	
	/**
	 * Creates new entity field on entity
	 * 
	 * @param The entity where field will be added
	 * @param The fieldName to use for this field. 
	 * @param The entity to reference. 
	 */
	void createReferencedField(JavaType entity, JavaSymbolName fieldName, JavaType entityToReference);
	
	/**
	 * Creates new Geo Field on entity
	 * 
	 * @param The entity where field will be added
	 * @param The fieldName to use for this field. 
	 * @param The GEO field type
	 */
	void createGeoField(JavaType entity, JavaSymbolName fieldName, AndrooidFieldGeoTypes entityToReference);

}