package io.androoid.roo.addon.suite.addon.persistence;

import io.androoid.roo.addon.suite.addon.project.AndrooidProjectOperations;
import io.androoid.roo.addon.suite.dependency.manager.DependencyManagerOperations;
import io.androoid.roo.addon.suite.dependency.manager.providers.DependencyManagerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;

/**
 * Implementation of {@link AndrooidPersistenceOperations} interface.
 *
 * @author Juan Carlos García
 * @since 1.0.0
 */
@Component
@Service
public class AndrooidPersistenceOperationsImpl implements
		AndrooidPersistenceOperations {

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
	 * Get a reference to the DependencyManagerOperations from the underlying
	 * OSGi container
	 */
	@Reference
	private DependencyManagerOperations dependencyManagerOperations;

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

		// Install necessary dependencies
		dependencyManagerOperations.getInstalledProvider().addDependency(
				"com.j256.ormlite", "ormlite-core", "4.48");
		dependencyManagerOperations.getInstalledProvider().addDependency(
				"com.j256.ormlite", "ormlite-android", "4.48");

		// Generate ormlite_config.txt file on src/main/res/raw folder
		Validate.isTrue(
				!fileManager.exists("src/main/res/raw/ormlite_config.txt"),
				"'ormlite_config.txt' file exists!");

		final InputStream templateInputStream = FileUtils.getInputStream(
				getClass(), "raw/ormlite_config.txt");

		OutputStream outputStream = null;

		try {
			// Read template and insert the user's package
			String input = IOUtils.toString(templateInputStream);
			input = input.replace("_CURRENT_DATE_", new Date().toString());

			// Output the file for the user
			final MutableFile mutableFile = fileManager.createFile(pathResolver
					.getRoot() + "/src/main/res/raw/ormlite_config.txt");

			outputStream = mutableFile.getOutputStream();
			IOUtils.write(input, outputStream);
		} catch (final IOException ioe) {
			throw new IllegalStateException(
					"Unable to create ormlite_config.txt file", ioe);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
			IOUtils.closeQuietly(outputStream);
		}

	}

}