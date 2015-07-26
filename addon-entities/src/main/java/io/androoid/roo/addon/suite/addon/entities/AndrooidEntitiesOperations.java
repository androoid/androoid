package io.androoid.roo.addon.suite.addon.entities;

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
	 */
	void createEntity();

}