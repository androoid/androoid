package io.androoid.roo.addon.suite.addon.persistence;

import io.androoid.roo.addon.suite.addon.project.AndrooidProjectOperations;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;

/**
 * Implementation of {@link AndrooidPersistenceOperations} interface.
 *
 * @author Juan Carlos García
 * @since 1.0.0
 */
@Component
@Service
public class AndrooidPersistenceOperationsImpl implements AndrooidPersistenceOperations {

	/**
	 * Get hold of a JDK Logger
	 */
	private Logger LOGGER = Logger.getLogger(getClass().getName());

	/**
	 * Using the Roo file manager instead if java.io.File gives you automatic
	 * rollback in case an Exception is thrown.
	 */
	@Reference
	private FileManager fileManager;

	/**
	 * Get a reference to the ProjectOperations from the underlying OSGi
	 * container.
	 */
	@Reference
	private ProjectOperations projectOperations;
	
	/**
	 * Get a reference to the AndrooidProjectOperations from the underlying OSGi
	 * container
	 */
	@Reference
	private AndrooidProjectOperations androoidProjectOperations;

	/**
	 * Get a reference to the PathResolver from the undelying OSGi container
	 */
	@Reference
	private PathResolver pathResolver;

	/** {@inheritDoc} */
	public boolean isPersistenceSetupAvailable() {
		return androoidProjectOperations.isAndrooidProjectGenerated();
	}

	
	/** {@inheritDoc} */
	public void setup() {
		// Include gradle dependencies
		
	}


}