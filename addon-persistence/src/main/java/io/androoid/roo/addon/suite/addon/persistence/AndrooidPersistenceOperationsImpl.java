package io.androoid.roo.addon.suite.addon.persistence;

import io.androoid.roo.addon.suite.addon.project.AndrooidProjectOperations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Implementation of {@link AndrooidPersistenceOperations} interface.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidPersistenceOperationsImpl implements
		AndrooidPersistenceOperations {

	/**
	 * Get hold of a JDK Logger
	 */
	private Logger LOGGER = Logger.getLogger(getClass().getName());

	@Reference
	private FileManager fileManager;
	@Reference
	private ProjectOperations projectOperations;
	@Reference
	private AndrooidProjectOperations androoidProjectOperations;
	@Reference
	private PathResolver pathResolver;

	/** {@inheritDoc} */
	public boolean isPersistenceSetupAvailable() {
		return projectOperations.isFeatureInstalled("androoid-project");
	}

	/** {@inheritDoc} */
	public void setup() {
		// Install necessary dependencies
		installDependencies();

		// Generate ormlite_config.txt file on src/main/res/raw folder
		final String ormLiteConfigPath = pathResolver.getFocusedIdentifier(
				Path.SRC_MAIN_RES, "raw/ormlite_config.txt");
		Validate.isTrue(
				!fileManager.exists(ormLiteConfigPath),
				"'ormlite_config.txt' file exists!");

		final InputStream templateInputStream = FileUtils.getInputStream(
				getClass(), "raw/ormlite_config.txt");

		OutputStream outputStream = null;

		try {
			// Read template and insert the current date
			String input = IOUtils.toString(templateInputStream);
			input = input.replace("_CURRENT_DATE_", new Date().toString());

			// Output the file for the user
			final MutableFile mutableFile = fileManager.createFile(ormLiteConfigPath);

			outputStream = mutableFile.getOutputStream();
			IOUtils.write(input, outputStream);
		} catch (final IOException ioe) {
			throw new IllegalStateException(
					"Unable to create ormlite_config.txt file", ioe);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
			IOUtils.closeQuietly(outputStream);
		}

		// Generate DatabaseConfigUtils

	}

	/**
	 * Method that uses configuration.xml file to install dependencies on
	 * current pom.xml
	 */
	private void installDependencies() {
		final Element configuration = XmlUtils.getConfiguration(getClass());
		// Install dependencies
		List<Element> elements = XmlUtils.findElements(
				"/configuration/androoid/dependencies/dependency",
				configuration);
		List<Dependency> dependencies = new ArrayList<Dependency>();
		for (Element element : elements) {
			Dependency dependency = new Dependency(element);
			dependencies.add(dependency);
		}
		projectOperations.addDependencies(
				projectOperations.getFocusedModuleName(), dependencies);
	}

	/**
	 * FEATURE METHODS
	 */
	
	public String getName() {
		return FEATURE_ANDROOID_PERSISTENCE;
	}

	public boolean isInstalledInModule(String moduleName) {
		final String ormLiteConfigPath = pathResolver.getFocusedIdentifier(
				Path.SRC_MAIN_RES, "raw/ormlite_config.txt");
		return fileManager.exists(ormLiteConfigPath);
	}

}