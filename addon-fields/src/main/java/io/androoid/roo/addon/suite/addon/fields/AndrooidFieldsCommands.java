package io.androoid.roo.addon.suite.addon.fields;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Androoid Fields commands class. This class provides all necessary commands
 * to generate entity fields on Android Project.
 * 
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidFieldsCommands implements CommandMarker {

	/**
	 * Get hold of a JDK Logger
	 */
	private Logger LOGGER = Logger.getLogger(getClass().getName());

	@Reference
	private AndrooidFieldsOperations fieldOperations;
	
	/**
	 * Field generation is only available if persistence was configured using
	 * androoid persistence command.
	 * 
	 * @return true if persistence is installed.
	 */
	@CliAvailabilityIndicator("androoid field")
	public boolean isEntityCreationAvailable() {
		return fieldOperations.isFieldCreationAvailable();
	}

	/**
	 * This method registers the androoid field command.
	 * 
	 * @param class
	 */
	@CliCommand(value = "androoid field", help = "Creates new field on selected entity.")
	public void createEntity(
			@CliOption(key = "entity", optionContext = UPDATE_PROJECT, mandatory = true, help = "Name of the entity where generated field will be added. (Ex: ~.domain.MyEntity)") final JavaType entity,
			@CliOption(key = "name", mandatory = false, optionContext = UPDATE_PROJECT, help = "The field name to use.") final JavaSymbolName fieldName,
			@CliOption(key = "type", mandatory = true, optionContext = "java-lang,project", help = "The field type that will be used for the field.") final JavaType fieldType) {
		
		// Creating new field
		fieldOperations.createField(entity, fieldName, fieldType);
	}
}