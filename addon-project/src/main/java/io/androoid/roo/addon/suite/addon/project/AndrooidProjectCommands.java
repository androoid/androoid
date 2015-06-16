package io.androoid.roo.addon.suite.addon.project;

import io.androoid.roo.addon.suite.addon.project.utils.AvailableSDKs;
import io.androoid.roo.addon.suite.dependency.manager.DependencyManagerOperations;
import io.androoid.roo.addon.suite.dependency.manager.providers.DependencyManagerProviderId;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Androoid ProjectCommand class. This class provides all necessary commands to
 * generate an android project using Spring Roo Shell.
 * 
 * 
 * @author Juan Carlos García
 * @since 1.0.0
 */
@Component
@Service
public class AndrooidProjectCommands implements CommandMarker {

	/**
	 * Get hold of a JDK Logger
	 */
	private Logger LOGGER = Logger.getLogger(getClass().getName());

	/**
	 * Get a reference to the AndrooidProjectOperations from the underlying OSGi
	 * container
	 */
	@Reference
	private AndrooidProjectOperations androoidOperations;

	@Reference
	private DependencyManagerOperations dependencyManagerOperations;

	/**
	 * Project generation is only available if other project was not generated
	 * before.
	 * 
	 * @return true if not exists other project in the current folder.
	 */
	@CliAvailabilityIndicator("androoid project setup")
	public boolean isProjectSetupAvailable() {
		return androoidOperations.isCreateProjectAvailable();
	}

	/**
	 * This method registers the androoid project setup command.
	 * 
	 * TODO: Include dependencyManager parameter to select dependency manager to use like gradle or maven.
	 * 
	 * @param applicationId
	 *            A String that identifies current generated project. Will be
	 *            included on defaultConfig of build.gradle file
	 * @param minSdkVersion
	 *            An integer designating the minimum API Level required for the
	 *            application to run. The Android system will prevent the user
	 *            from installing the application if the system's API Level is
	 *            lower than the value specified in this attribute. You should
	 *            always declare this attribute.
	 * @param targetSdkVersion
	 *            An integer designating the API Level that the application
	 *            targets. If not set, the default value equals that given to
	 *            minSdkVersion
	 */
	@CliCommand(value = "androoid project setup", help = "Generates Android Project structure")
	public void projectSetup(
			@CliOption(key = "applicationId", mandatory = true, help = "A String that identifies current generated project. (Ex: io.androoid.proof) ") JavaPackage applicationId,
			@CliOption(key = "minSdkVersion", mandatory = true, help = "An integer designating the minimum API Level required for the application to run") AvailableSDKs minSdkVersion,
			@CliOption(key = "targetSdkVersion", mandatory = false, help = "An integer designating the API Level that the application targets. If not set, the default value equals that given to minSdkVersion") AvailableSDKs targetSdkVersion/*,
			@CliOption(key = "dependencyManager", mandatory = false, help = "Dependency manager to use to generate project.") DependencyManagerProviderId dependencyManager*/) {

		// Checking if targetSdkVersion param was defined on executed command
		if (targetSdkVersion == null) {
			targetSdkVersion = minSdkVersion;
		}

		// Checking if dependencyManager was defined. If not, by default use
		// Gradle
		// Show TODO comment on javadoc
		/*if (dependencyManager == null) {
			dependencyManager = dependencyManagerOperations
					.getProviderByName("MAVEN");
		}*/

		// Generating new androoid project structure
		androoidOperations.setup(applicationId, minSdkVersion,
				targetSdkVersion, dependencyManagerOperations
				.getProviderByName("MAVEN"));

	}
}