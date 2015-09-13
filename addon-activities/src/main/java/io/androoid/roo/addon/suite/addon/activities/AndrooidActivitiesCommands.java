package io.androoid.roo.addon.suite.addon.activities;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CommandMarker;

/**
 * Androoid ActivitiesCommands class. This class provides all necessary commands
 * to generate android activities based on current application entity model.
 * 
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidActivitiesCommands implements CommandMarker {

	/**
	 * Get hold of a JDK Logger
	 */
	private Logger LOGGER = Logger.getLogger(getClass().getName());

	@Reference
	private AndrooidActivitiesOperations activitiesOperations;

	/**
	 * Activity layer setup is only available if exists a generated project
	 * 
	 * @return true if exists an androoid project in the current folder.
	 */
	@CliAvailabilityIndicator("androoid activity layer setup")
	public boolean isSetupAvailable() {
		return activitiesOperations.isSetupAvailable();
	}

	/**
	 * This method registers the androoid activity layer setup command.
	 */
	@CliCommand(value = "androoid activity layer setup", help = "Generates Android activity layer structure with all necessary components for activity layer.")
	public void setup() {

		// Install activity layer components
		activitiesOperations.setup();

	}
}