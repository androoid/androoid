package io.androoid.roo.addon.suite.addon.persistence;

import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface of Androoid persistence commands that are available via the Roo
 * shell.
 *
 * @author Juan Carlos García
 * @since 1.0
 */
public interface AndrooidPersistenceOperations extends Feature {

	public static final String FEATURE_ANDROOID_PERSISTENCE = "androoid-persistence";

	/**
	 * Indicates if androoid persistence command should be available.
	 * 
	 * @return true if it should be available, otherwise false
	 */
	boolean isPersistenceSetupAvailable();

	/**
	 * Configures persistence on generated Android project.
	 * 
	 * TODO: By default, uses ORMLite and SQLite, but in future versions
	 * developers should select ORM they want to use.
	 * 
	 */
	void setup();

	/**
	 * Includes new DAO on DatabaseHelper. Will include Dao and
	 * RuntimeExceptionDao
	 * 
	 * @param type of the new Dao (Ex: MyEntity)
	 */
	void addDao(JavaType type);

}