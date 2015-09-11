package io.androoid.roo.addon.suite.addon.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
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
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.model.EnumDetails;
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

import io.androoid.roo.addon.suite.addon.persistence.annotations.AndrooidDatabaseConfig;
import io.androoid.roo.addon.suite.addon.persistence.annotations.AndrooidDatabaseHelper;
import io.androoid.roo.addon.suite.addon.project.AndrooidProjectOperations;

/**
 * Implementation of {@link AndrooidPersistenceOperations} interface.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidPersistenceOperationsImpl implements AndrooidPersistenceOperations {

	private static final String LINE_SEPARATOR = "\n";

	private static final JavaType ANDROOID_DATABASE_HELPER = new JavaType(AndrooidDatabaseHelper.class);

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

	/** {@inheritDoc} */
	public void addDao(JavaType type) {

		// Getting class with annotation @AndrooidDatabaseHelper
		Set<JavaType> databaseHelperClasses = typeLocationService.findTypesWithAnnotation(ANDROOID_DATABASE_HELPER);

		Validate.notEmpty(databaseHelperClasses,
				"Android project needs class annotated with @AndrooidDatabaseHelper to works correctly.");

		Iterator<JavaType> it = databaseHelperClasses.iterator();

		while (it.hasNext()) {
			// Getting @AndrooidDatabaseHelper attributes
			JavaType databaseHelperClass = it.next();
			ClassOrInterfaceTypeDetails databaseHelperDetails = typeLocationService.getTypeDetails(databaseHelperClass);

			AnnotationMetadata androoidDatabaseHelperAnnotation = databaseHelperDetails
					.getAnnotation(ANDROOID_DATABASE_HELPER);
			AnnotationAttributeValue<List<ClassAttributeValue>> entitiesAttribute = androoidDatabaseHelperAnnotation
					.getAttribute("entities");

			// Creating new annotation with old values
			final List<AnnotationAttributeValue<?>> attributes = new ArrayList<AnnotationAttributeValue<?>>();
			final List<ClassAttributeValue> desiredEntities = new ArrayList<ClassAttributeValue>();

			if (entitiesAttribute != null) {
				List<ClassAttributeValue> currentEntities = entitiesAttribute.getValue();

				Iterator<ClassAttributeValue> currentEntitiesIt = currentEntities.iterator();
				while (currentEntitiesIt.hasNext()) {
					ClassAttributeValue entity = currentEntitiesIt.next();
					desiredEntities.add(entity);
				}
			}

			// Prepare class builder
			ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
					databaseHelperDetails);
			// Removing old annotation
			cidBuilder.removeAnnotation(ANDROOID_DATABASE_HELPER);

			// Including new types
			desiredEntities.add(new ClassAttributeValue(new JavaSymbolName("entities"), type));

			attributes
					.add(new ArrayAttributeValue<ClassAttributeValue>(new JavaSymbolName("entities"), desiredEntities));

			AnnotationMetadataBuilder databaseHelperAnnotation = new AnnotationMetadataBuilder(ANDROOID_DATABASE_HELPER,
					attributes);

			// Including new annotation
			cidBuilder.addAnnotation(databaseHelperAnnotation);

			typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

		}

	}

	/**
	 * Method to generate DatabaseHelper.java on src/main/java/${package}/utils
	 */
	private void createDatabaseHelper() {
		String projectPackage = projectOperations.getFocusedTopLevelPackage().getFullyQualifiedPackageName()
				.concat(".utils");
		final JavaType javaType = new JavaType(projectPackage + ".DatabaseHelper");
		final String physicalPath = pathResolver.getFocusedCanonicalPath(Path.SRC_MAIN_JAVA, javaType);
		if (fileManager.exists(physicalPath)) {
			return;
		}
		InputStream inputStream = null;
		try {
			inputStream = FileUtils.getInputStream(getClass(), "java/DatabaseHelper-template._java");
			String input = IOUtils.toString(inputStream);
			// Replacing .utils package
			input = input.replace("__UTILS_PACKAGE__", projectPackage);
			// Replacing general package
			input = input.replace("__GENERAL_PACKAGE__",
					projectOperations.getFocusedTopLevelPackage().getFullyQualifiedPackageName());
			fileManager.createOrUpdateTextFileIfRequired(physicalPath, input, false);
		} catch (final IOException e) {
			throw new IllegalStateException("Unable to create '" + physicalPath + "'", e);
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
		String utilsPath = projectOperations.getFocusedTopLevelPackage().getFullyQualifiedPackageName()
				.concat(".utils");

		int modifier = Modifier.PUBLIC;
		JavaType target = new JavaType(utilsPath.concat(".DatabaseConfigUtils"));
		final String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(target,
				pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
		File targetFile = new File(typeLocationService.getPhysicalTypeCanonicalPath(declaredByMetadataId));
		Validate.isTrue(!targetFile.exists(), "Type '%s' already exists", target);

		// Prepare class builder
		final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
				declaredByMetadataId, modifier, target, PhysicalTypeCategory.CLASS);

		// DatabaseConfigUtils extends OrmLiteConfigUtil
		cidBuilder.addExtendsTypes(new JavaType("com.j256.ormlite.android.apptools.OrmLiteConfigUtil"));

		// Including AndrooidDatabaseConfig annotation
		cidBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType(AndrooidDatabaseConfig.class)));

		typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

	}

	/**
	 * Method to create ormlite_config.txt file on src/main/res/raw folder
	 */
	private void createOrmLiteConfigFile() {
		final String ormLiteConfigPath = pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RES, "raw/ormlite_config.txt");
		Validate.isTrue(!fileManager.exists(ormLiteConfigPath), "'ormlite_config.txt' file exists!");

		final InputStream templateInputStream = FileUtils.getInputStream(getClass(), "raw/ormlite_config.txt");

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
			throw new IllegalStateException("Unable to create ormlite_config.txt file", ioe);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
			IOUtils.closeQuietly(outputStream);
		}
	}

	/** {@inheritDoc} */
	public void updatePersistenceConfigFile() {
		try {
			final String ormLiteConfigPath = pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RES,
					"raw/ormlite_config.txt");

			File file = new File(ormLiteConfigPath);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// Generating string with content
			Set<ClassOrInterfaceTypeDetails> currentEntities = typeLocationService
					.findClassesOrInterfaceDetailsWithAnnotation(
							new JavaType("io.androoid.roo.addon.suite.addon.entities.annotations.AndrooidEntity"));

			StringBuilder sb = new StringBuilder();

			sb.append("#").append(LINE_SEPARATOR);
			sb.append("# generated on ").append(new Date().toString()).append(LINE_SEPARATOR);
			sb.append("#").append(LINE_SEPARATOR);

			for (ClassOrInterfaceTypeDetails entity : currentEntities) {
				// # --table-start--
				sb.append("# --table-start--").append(LINE_SEPARATOR);
				// dataClass=ENTITY_WITH_PACKAGE
				sb.append("dataClass=").append(entity.getName().getFullyQualifiedTypeName()).append(LINE_SEPARATOR);
				// tableName=entity
				sb.append("tableName=").append(entity.getName().getSimpleTypeName().toLowerCase())
						.append(LINE_SEPARATOR);
				// # --table-fields-start--
				sb.append("# --table-fields-start--").append(LINE_SEPARATOR);

				List<? extends FieldMetadata> entityFields = entity.getDeclaredFields();

				for (FieldMetadata field : entityFields) {

					// Checking if current field is a Database field or not
					AnnotationMetadata dbFieldAnnotation = field
							.getAnnotation(new JavaType("com.j256.ormlite.field.DatabaseField"));

					if (dbFieldAnnotation != null) {
						// # --field-start--
						sb.append("# --field-start--").append(LINE_SEPARATOR);
						// fieldName=fieldName
						sb.append("fieldName=").append(field.getFieldName().toString()).append(LINE_SEPARATOR);

						AnnotationAttributeValue<Boolean> generatedIdAttr = dbFieldAnnotation
								.getAttribute("generatedId");
						if (generatedIdAttr != null) {
							// generatedId=value
							sb.append("generatedId=").append(generatedIdAttr.getValue()).append(LINE_SEPARATOR);
						}

						AnnotationAttributeValue<Boolean> foreignAttr = dbFieldAnnotation.getAttribute("foreign");
						if (foreignAttr != null) {
							// foreign=value
							sb.append("foreign=").append(foreignAttr.getValue()).append(LINE_SEPARATOR);
						}

						AnnotationAttributeValue<Boolean> foreignAutoRefreshAttr = dbFieldAnnotation
								.getAttribute("foreignAutoRefresh");
						if (foreignAutoRefreshAttr != null) {
							// foreignAutoRefresh=value
							sb.append("foreignAutoRefresh=").append(foreignAutoRefreshAttr.getValue())
									.append(LINE_SEPARATOR);
						}

						AnnotationAttributeValue<EnumDetails> dataPersisterAttr = dbFieldAnnotation
								.getAttribute("dataType");
						if (dataPersisterAttr != null) {
							// dataPersister=value
							sb.append("dataPersister=").append(dataPersisterAttr.getValue().getField().toString())
									.append(LINE_SEPARATOR);
						}

						// # --field-end--
						sb.append("# --field-end--").append(LINE_SEPARATOR);

					}

				}

				// # --table-fields-end--
				sb.append("# --table-fields-end--").append(LINE_SEPARATOR);

				// # --table-end--
				sb.append("# --table-end--").append(LINE_SEPARATOR);
				sb.append("#################################").append(LINE_SEPARATOR);
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(sb.toString());
			bw.close();

			LOGGER.log(Level.INFO,
					"File src/main/res/raw/ormlite_config.txt has been updated with last entity model modifications!");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Method that uses configuration.xml file to install dependencies and
	 * properties on current pom.xml
	 */
	private void installDependencies() {
		final Element configuration = XmlUtils.getConfiguration(getClass());

		// Add properties
		List<Element> properties = XmlUtils.findElements("/configuration/androoid/properties/*", configuration);
		for (Element property : properties) {
			projectOperations.addProperty(projectOperations.getFocusedModuleName(), new Property(property));
		}

		// Add dependencies
		List<Element> elements = XmlUtils.findElements("/configuration/androoid/dependencies/dependency",
				configuration);
		List<Dependency> dependencies = new ArrayList<Dependency>();
		for (Element element : elements) {
			Dependency dependency = new Dependency(element);
			dependencies.add(dependency);
		}
		projectOperations.addDependencies(projectOperations.getFocusedModuleName(), dependencies);
	}

	/**
	 * FEATURE METHODS
	 */

	public String getName() {
		return FEATURE_ANDROOID_PERSISTENCE;
	}

	public boolean isInstalledInModule(String moduleName) {
		final String ormLiteConfigPath = pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RES, "raw/ormlite_config.txt");
		return fileManager.exists(ormLiteConfigPath);
	}

}