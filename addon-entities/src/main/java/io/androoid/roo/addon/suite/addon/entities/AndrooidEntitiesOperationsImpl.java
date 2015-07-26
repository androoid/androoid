package io.androoid.roo.addon.suite.addon.entities;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.project.ProjectOperations;

/**
 * Implementation of {@link AndrooidEntitiesOperations} interface.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidEntitiesOperationsImpl implements
		AndrooidEntitiesOperations {

	/**
	 * Get hold of a JDK Logger
	 */
	private Logger LOGGER = Logger.getLogger(getClass().getName());

	@Reference
	private ProjectOperations projectOperations;

	/** {@inheritDoc} */
	public boolean isEntityCreationAvailable() {
		return projectOperations.isFeatureInstalled("androoid-persistence");
	}

	/** {@inheritDoc} */
	public void createEntity() {


	}


}