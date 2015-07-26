package io.androoid.roo.addon.suite.addon.persistence;

import io.androoid.roo.addon.suite.addon.persistence.annotations.AndrooidDatabaseConfig;
import io.androoid.roo.addon.suite.addon.persistence.annotations.AndrooidDatabaseHelper;
import io.androoid.roo.addon.suite.addon.project.AndrooidProjectOperations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.ConstructorMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
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
	@Reference
	private TypeLocationService typeLocationService;
	@Reference
	private TypeManagementService typeManagementService;

	/** {@inheritDoc} */
	public boolean isPersistenceSetupAvailable() {
		return projectOperations.isFeatureInstalled("androoid-project");
	}

	/** {@inheritDoc} */
	public void setup() {
		// Install necessary dependencies
		installDependencies();
		// Generate ormlite_config.txt file on src/main/res/raw folder
		createOrmLiteConfigFile();
		// Generate DatabaseConfigUtils.java
		createDatabaseConfigUtils();
		// Generate DatabaseHelper.java
		createDatabaseHelper();

	}

	/**
	 * Method to generate DatabaseHelper.java on src/main/java/${package}/utils
	 */
	private void createDatabaseHelper() {
		String projectPackage = projectOperations.getFocusedTopLevelPackage()
				.getFullyQualifiedPackageName().concat(".utils");
		final JavaType javaType = new JavaType(projectPackage
				+ ".DatabaseHelper");
		final String physicalPath = pathResolver.getFocusedCanonicalPath(
				Path.SRC_MAIN_JAVA, javaType);
		if (fileManager.exists(physicalPath)) {
			return;
		}
		InputStream inputStream = null;
		try {
			inputStream = FileUtils.getInputStream(getClass(),
					"java/DatabaseHelper-template._java");
			String input = IOUtils.toString(inputStream);
			// Replacing .utils package
			input = input.replace("__UTILS_PACKAGE__", projectPackage);
			// Replacing general package
			input = input
					.replace("__GENERAL_PACKAGE__", projectOperations
							.getFocusedTopLevelPackage()
							.getFullyQualifiedPackageName());
			fileManager.createOrUpdateTextFileIfRequired(physicalPath, input,
					false);
		} catch (final IOException e) {
			throw new IllegalStateException("Unable to create '" + physicalPath
					+ "'", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * Method to generate DatabaseConfigUtils.java on
	 * src/main/java/${package}/utils
	 */
	private void createDatabaseConfigUtils() {

		// Getting current package
		String utilsPath = projectOperations.getFocusedTopLevelPackage()
				.getFullyQualifiedPackageName().concat(".utils");

		int modifier = Modifier.PUBLIC;
		JavaType target = new JavaType(utilsPath.concat(".DatabaseConfigUtils"));
		final String declaredByMetadataId = PhysicalTypeIdentifier
				.createIdentifier(target,
						pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
		File targetFile = new File(
				typeLocationService
						.getPhysicalTypeCanonicalPath(declaredByMetadataId));
		Validate.isTrue(!targetFile.exists(), "Type '%s' already exists",
				target);

		// Prepare class builder
		final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
				declaredByMetadataId, modifier, target,
				PhysicalTypeCategory.CLASS);

		// DatabaseConfigUtils extends OrmLiteConfigUtil
		cidBuilder.addExtendsTypes(new JavaType(
				"com.j256.ormlite.android.apptools.OrmLiteConfigUtil"));

		// Including AndrooidDatabaseConfig annotation
		cidBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType(
				AndrooidDatabaseConfig.class)));

		typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

	}

	/**
	 * Method to create ormlite_config.txt file on src/main/res/raw folder
	 */
	private void createOrmLiteConfigFile() {
		final String ormLiteConfigPath = pathResolver.getFocusedIdentifier(
				Path.SRC_MAIN_RES, "raw/ormlite_config.txt");
		Validate.isTrue(!fileManager.exists(ormLiteConfigPath),
				"'ormlite_config.txt' file exists!");

		final InputStream templateInputStream = FileUtils.getInputStream(
				getClass(), "raw/ormlite_config.txt");

		OutputStream outputStream = null;

		try {
			// Read template and insert the current date
			String input = IOUtils.toString(templateInputStream);
			input = input.replace("_CURRENT_DATE_", new Date().toString());

			// Output the file for the user
			final MutableFile mutableFile = fileManager
					.createFile(ormLiteConfigPath);

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

	/**
	 * Method that uses configuration.xml file to install dependencies and
	 * properties on current pom.xml
	 */
	private void installDependencies() {
		final Element configuration = XmlUtils.getConfiguration(getClass());

		// Add properties
		List<Element> properties = XmlUtils.findElements(
				"/configuration/androoid/properties/*", configuration);
		for (Element property : properties) {
			projectOperations.addProperty(projectOperations
					.getFocusedModuleName(), new Property(property));
		}

		// Add dependencies
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