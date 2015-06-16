package io.androoid.roo.addon.suite.dependency.manager.providers.gradle;

import io.androoid.roo.addon.suite.dependency.manager.providers.DependencyManagerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.FileUtils;

/**
 * Gradle dependency manager provider.
 * 
 * This class provides all necessary functionalities to manage your project
 * using gradle.
 * 
 * @author Juan Carlos García
 * @since 1.0.0
 */
@Component
@Service
public class GradleDependencyManagerProvider implements
		DependencyManagerProvider {

	/**
	 * Using the Roo file manager instead if java.io.File gives you automatic
	 * rollback in case an Exception is thrown.
	 */
	@Reference
	private FileManager fileManager;

	/**
	 * Get a reference to the PathResolver from the undelying OSGi container
	 */
	@Reference
	private PathResolver pathResolver;

	public static final String NAME = "GRADLE";

	public static final String DESCRIPTION = "Uses Gradle to manage dependencies of your generated project.";

	private static final Logger LOGGER = Logger
			.getLogger(GradleDependencyManagerProvider.class.getName());

	public String getName() {
		return NAME;
	}

	public String getDescription() {
		return DESCRIPTION;
	}

	/** {@inheritDoc} */
	public void addDependency(String groupId, String artifactId, String version) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	public void install(JavaPackage applicationId, String minSdkVersion,
			String targetSdkVersion) {
		// Checking that build.gradle doesn't exists
		Validate.isTrue(!fileManager.exists("build.gradle"),
				"'build.gradle' file exists!");

		final InputStream templateInputStream = FileUtils.getInputStream(
				getClass(), "build.gradle-template");

		OutputStream outputStream = null;

		try {
			// Read template and insert the user's package
			String input = IOUtils.toString(templateInputStream);
			input = input.replace("_COMPILE_SDK_VERSION_", targetSdkVersion);
			input = input.replace("_APPLICATION_ID_",
					"\"".concat(applicationId.getFullyQualifiedPackageName())
							.concat("\""));
			input = input.replace("_MIN_SDK_VERSION_", minSdkVersion);
			input = input.replace("_TARGET_SDK_VERSION_", targetSdkVersion);

			// Output the file for the user
			final MutableFile mutableFile = fileManager.createFile(pathResolver
					.getRoot() + "/build.gradle");

			outputStream = mutableFile.getOutputStream();
			IOUtils.write(input, outputStream);
		} catch (final IOException ioe) {
			throw new IllegalStateException(
					"Unable to create build.gradle file", ioe);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
			IOUtils.closeQuietly(outputStream);
		}

	}

}
