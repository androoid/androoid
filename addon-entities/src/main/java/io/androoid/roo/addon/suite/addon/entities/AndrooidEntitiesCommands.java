package io.androoid.roo.addon.suite.addon.entities;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
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
	 * @param class
	 */
	@CliCommand(value = "androoid entity", help = "Creates new entity on current Androoid Project.")
	public void createEntity(
			@CliOption(key = "class", optionContext = UPDATE_PROJECT, mandatory = true, help = "Name of the entity to create. (Ex: ~.domain.MyEntity)") final JavaType entity,
			@CliOption(key = "identifierField", mandatory = false, optionContext = UPDATE_PROJECT, help = "The identifier field name to use for this entity. If null, set 'id' as default.") final JavaSymbolName identifierField,
			@CliOption(key = "identifierType", mandatory = false, optionContext = "java-lang,project", help = "The data type that will be used for the identifier field (defaults to java.lang.Long)") final JavaType identifierType) {
		
		// Creating new entity
		entityOperations.createEntity(entity, identifierField, identifierType);
	}
}