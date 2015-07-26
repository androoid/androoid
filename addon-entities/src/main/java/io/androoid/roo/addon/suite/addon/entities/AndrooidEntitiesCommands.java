package io.androoid.roo.addon.suite.addon.entities;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CommandMarker;

/**
 * Androoid EntitiesCommands class. This class provides all necessary commands
 * to generate entities on Android Project and add fields to that generated
 * entities.
 * 
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidEntitiesCommands implements CommandMarker {

	/**
	 * Get hold of a JDK Logger
	 */
	private Logger LOGGER = Logger.getLogger(getClass().getName());

	@Reference
	private AndrooidEntitiesOperations entityOperations;

	/**
	 * Entity generation is only available if persistence was configured using
	 * androoid persistence command.
	 * 
	 * @return true if persistence is installed.
	 */
	@CliAvailabilityIndicator("androoid entity")
	public boolean isEntityCreationAvailable() {
		return entityOperations.isEntityCreationAvailable();
	}

	/**
	 * This method registers the androoid entity command.
	 * 
	 */
	@CliCommand(value = "androoid entity", help = "Creates new entity on current Androoid Project.")
	public void createEntity() {
		// Creating new entity
		entityOperations.createEntity();

	}
}