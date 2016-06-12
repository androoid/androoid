package io.androoid.roo.addon.suite.addon.entities;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Interface of Androoid entities commands that are available via the Roo shell.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public interface AndrooidEntitiesOperations {

  /**
   * Indicates if androoid entity command should be available.
   * 
   * @return true if it should be available, otherwise false
   */
  boolean isEntityCreationAvailable();

  /**
   * Creates new entity on Android Project
   * 
   * @param Name of the entity to create
   * @param The identifier field name to use for this entity. If null, set 'id' as default.
   * @param The data type that will be used for the identifier field (defaults to java.lang.Long) 
   */
  void createEntity(JavaType entity, JavaSymbolName idetifierField, JavaType identifierType);

}
