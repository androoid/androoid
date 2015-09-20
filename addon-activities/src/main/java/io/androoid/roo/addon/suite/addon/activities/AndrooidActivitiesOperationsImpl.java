package io.androoid.roo.addon.suite.addon.activities;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaPackage;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidFormActivity;
import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidListActivity;
import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidMainActivity;
import io.androoid.roo.addon.suite.addon.entities.annotations.AndrooidEntity;
import io.androoid.roo.addon.suite.addon.manifest.manager.AndrooidManifestOperations;
import io.androoid.roo.addon.suite.support.AndrooidOperationsUtils;

/**
 * Implementation of {@link AndrooidActivitiesOperations} interface.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidActivitiesOperationsImpl implements AndrooidActivitiesOperations {

	/**
	 * Get hold of a JDK Logger
	 */
	private Logger LOGGER = Logger.getLogger(getClass().getName());

	private ComponentContext cContext;

	@Reference
	private FileManager fileManager;
	@Reference
	private ProjectOperations projectOperations;
	@Reference
	private PathResolver pathResolver;
	@Reference
	private AndrooidOperationsUtils operationsUtils;
	@Reference
	private AndrooidManifestOperations manifestOperations;
	@Reference
	private TypeLocationService typeLocationService;
	@Reference
	private TypeManagementService typeManagementService;

	protected void activate(final ComponentContext componentContext) {
		cContext = componentContext;
	}

	/** {@inheritDoc} */
	public boolean isSetupAvailable() {
		return projectOperations.isFeatureInstalled("androoid-project");
	}

	/** {@inheritDoc} */
	public void setup() {
		// Include androoid dependencies
		installDependencies();

		// Including basic files
		addBasicFiles(projectOperations.getFocusedTopLevelPackage());

		// Update AndroidManifest.xml with basic configuration
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("android:icon", "@mipmap/app_icon");
		attributes.put("android:label", "@string/app_name");
		attributes.put("android:theme", "@style/AppTheme");
		manifestOperations.addApplicationConfig(attributes);

		// Update AndroidManifest.xml file with basic permissions
		List<String> permissionsNames = new ArrayList<String>();
		permissionsNames.add("android.permission.ACCESS_COARSE_LOCATION");
		permissionsNames.add("android.permission.ACCESS_FINE_LOCATION");
		permissionsNames.add("android.permission.ACCESS_WIFI_STATE");
		permissionsNames.add("android.permission.ACCESS_NETWORK_STATE");
		permissionsNames.add("android.permission.INTERNET");
		permissionsNames.add("android.permission.WRITE_EXTERNAL_STORAGE");
		manifestOperations.addPermissions(permissionsNames);

		// Generates Main Activity by default.
		generatesMainActivity();

	}

	/** {@inheritDoc} */
	public void add(JavaType entity) {

		// Checks if current entity is annotated with @AndrooidEntity
		ClassOrInterfaceTypeDetails entityDetails = typeLocationService.getTypeDetails(entity);
		AnnotationMetadata androoidEntityAnnotation = entityDetails.getAnnotation(new JavaType(AndrooidEntity.class));

		Validate.notNull(androoidEntityAnnotation,
				String.format(
						"ERROR: Provided entity %s is not annotated with @AndrooidEntity. "
								+ "Only Androoid Entity classes could be used to generate new Androoid Activities.",
						entity.getSimpleTypeName()));

		// Generate new List activity
		addListActivity(entity);

		// Generate new Form activity
		addFormActivity(entity);

		// Add new activity button to main view
		// addActivityToMainView(entity);
	}

	/**
	 * This method creates new AndrooidListActivity related with an specified
	 * entity that will allow users to list data about the specified entity.
	 * 
	 * @param entity
	 *            JavaType that will be used to generate the related activity
	 */
	private void addListActivity(JavaType entity) {

		// Creates new activity JavaType
		String entityName = entity.getSimpleTypeName();
		String listActivityName = projectOperations.getFocusedTopLevelPackage().getFullyQualifiedPackageName()
				.concat(".activities.").concat(entityName.toLowerCase()).concat(".").concat(entityName)
				.concat("ListActivity");
		JavaType listActivity = new JavaType(listActivityName);

		int modifier = Modifier.PUBLIC;
		final String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(listActivity,
				pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));

		File targetFile = new File(typeLocationService.getPhysicalTypeCanonicalPath(declaredByMetadataId));
		Validate.isTrue(!targetFile.exists(), "Type '%s' already exists", listActivity);

		// Prepare class builder
		final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
				declaredByMetadataId, modifier, listActivity, PhysicalTypeCategory.CLASS);

		// Including @AndrooidListActivity annotation
		AnnotationMetadataBuilder listActivityAnnotation = new AnnotationMetadataBuilder(
				new JavaType(AndrooidListActivity.class));
		listActivityAnnotation.addClassAttribute("entity", entity);
		cidBuilder.addAnnotation(listActivityAnnotation);

		// AndrooidActivityList extends OrmLiteBaseListActivity<DatabaseHelper>
		JavaType extendsType = new JavaType("com.j256.ormlite.android.apptools.OrmLiteBaseListActivity", 0,
				DataType.TYPE, null, Arrays.asList(new JavaType(projectOperations.getFocusedTopLevelPackage()
						.getFullyQualifiedPackageName().concat(".utils.DatabaseHelper"))));
		cidBuilder.addExtendsTypes(extendsType);

		// AndrooidActivityList implements AbsListView.MultiChoiceModeListener
		// and AdapterView.OnItemClickListener
		cidBuilder.addImplementsType(new JavaType("android.widget.AbsListView.MultiChoiceModeListener").getBaseType());
		cidBuilder.addImplementsType(new JavaType("android.widget.AdapterView.OnItemClickListener").getBaseType());

		typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
	}

	/**
	 * This method creates new AndrooidFormActivity related with an specified
	 * entity that will allow users to manage data about the specified entity.
	 * 
	 * @param entity
	 *            JavaType that will be used to generate the related activity
	 */
	private void addFormActivity(JavaType entity) {

		// Creates new activity JavaType
		String entityName = entity.getSimpleTypeName();
		String formActivityName = projectOperations.getFocusedTopLevelPackage().getFullyQualifiedPackageName()
				.concat(".activities.").concat(entityName.toLowerCase()).concat(".").concat(entityName)
				.concat("FormActivity");
		JavaType formActivity = new JavaType(formActivityName);

		int modifier = Modifier.PUBLIC;
		final String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(formActivity,
				pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));

		File targetFile = new File(typeLocationService.getPhysicalTypeCanonicalPath(declaredByMetadataId));
		Validate.isTrue(!targetFile.exists(), "Type '%s' already exists", formActivity);

		// Prepare class builder
		final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
				declaredByMetadataId, modifier, formActivity, PhysicalTypeCategory.CLASS);

		// Including @AndrooidFormActivity annotation
		AnnotationMetadataBuilder formActivityAnnotation = new AnnotationMetadataBuilder(
				new JavaType(AndrooidFormActivity.class));
		formActivityAnnotation.addClassAttribute("entity", entity);
		cidBuilder.addAnnotation(formActivityAnnotation);

		// AndrooidActivityList extends OrmLiteBaseListActivity<DatabaseHelper>
		JavaType extendsType = new JavaType("com.j256.ormlite.android.apptools.OrmLiteBaseActivity", 0,
				DataType.TYPE, null, Arrays.asList(new JavaType(projectOperations.getFocusedTopLevelPackage()
						.getFullyQualifiedPackageName().concat(".utils.DatabaseHelper"))));
		cidBuilder.addExtendsTypes(extendsType);

		typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
	}

	/**
	 * Method that generates necessary configuration files like strings.xml and
	 * styles.xml
	 * 
	 * @param applicationId
	 */
	private void addBasicFiles(JavaPackage applicationPackage) {

		// Installing all drawable resources
		operationsUtils.updateDirectoryContents("drawable/*.xml",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/drawable"),
				fileManager, cContext, getClass());
		operationsUtils.updateDirectoryContents("drawable/*.png",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/drawable"),
				fileManager, cContext, getClass());

		// Installing all drawable-hdpi resources
		operationsUtils.updateDirectoryContents("drawable-hdpi/*.xml",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/drawable-hdpi"),
				fileManager, cContext, getClass());
		operationsUtils.updateDirectoryContents("drawable-hdpi/*.png",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/drawable-hdpi"),
				fileManager, cContext, getClass());

		// Installing all drawable-mdpi resources
		operationsUtils.updateDirectoryContents("drawable-mdpi/*.xml",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/drawable-mdpi"),
				fileManager, cContext, getClass());
		operationsUtils.updateDirectoryContents("drawable-mdpi/*.png",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/drawable-mdpi"),
				fileManager, cContext, getClass());

		// Installing all drawable-xhdpi resources
		operationsUtils.updateDirectoryContents("drawable-xhdpi/*.xml",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/drawable-xhdpi"),
				fileManager, cContext, getClass());
		operationsUtils.updateDirectoryContents("drawable-xhdpi/*.png",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/drawable-xhdpi"),
				fileManager, cContext, getClass());

		// Installing all drawable-xxhdpi resources
		operationsUtils.updateDirectoryContents("drawable-xxhdpi/*.xml",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/drawable-xxhdpi"),
				fileManager, cContext, getClass());
		operationsUtils.updateDirectoryContents("drawable-xxhdpi/*.png",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/drawable-xxhdpi"),
				fileManager, cContext, getClass());

		// Installing all menu resources
		operationsUtils.updateDirectoryContents("menu/*.xml",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/menu"), fileManager,
				cContext, getClass());

		// Installing all mipmap-xhdpi resources
		operationsUtils.updateDirectoryContents("mipmap-xhdpi/*.xml",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/mipmap-xhdpi"),
				fileManager, cContext, getClass());
		operationsUtils.updateDirectoryContents("mipmap-xhdpi/*.png",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/mipmap-xhdpi"),
				fileManager, cContext, getClass());

		// Installing all values resources
		operationsUtils.updateDirectoryContents("values/*.xml",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/values"), fileManager,
				cContext, getClass());
		operationsUtils.updateDirectoryContents("values/*.png",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/values"), fileManager,
				cContext, getClass());

		// Installing all values resources
		operationsUtils.updateDirectoryContents("values-w820dp/*.xml",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/values-w820dp"),
				fileManager, cContext, getClass());
		operationsUtils.updateDirectoryContents("values-w820dp/*.png",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/values-w820dp"),
				fileManager, cContext, getClass());

		// Copying basic strings.xml file with current application name
		InputStream stringsXmlFile = FileUtils.getInputStream(getClass(), "values-customized/strings.xml");

		final Document stringsFile = XmlUtils.readXml(stringsXmlFile);
		final Element stringsRoot = stringsFile.getDocumentElement();

		NodeList strings = stringsRoot.getElementsByTagName("string");
		for (int i = 0; i < strings.getLength(); i++) {
			Element item = (Element) strings.item(i);
			if (item.getAttribute("name").equals("app_name")) {
				item.setTextContent(applicationPackage.getLastElement());
			} else if (item.getAttribute("name").equals("welcome_text")) {
				item.setTextContent(
						"Welcome to ".concat(applicationPackage.getLastElement()).concat(" Android application"));
			}
		}

		final String stringsPath = pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RES, "values/strings.xml");
		final MutableFile mutableFile = fileManager.createFile(stringsPath);

		XmlUtils.writeXml(mutableFile.getOutputStream(), stringsFile);

	}

	/**
	 * Method that generates main activity including layouts, controllers and
	 * all necessary files to run application on an android device.
	 */
	private void generatesMainActivity() {

		// Include main activity layout xml file
		operationsUtils.updateDirectoryContents("layout/main_activity.xml",
				pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/layout"), fileManager,
				cContext, getClass());

		// Define main activity on AndroidManifest.xml
		Element mainActivity = manifestOperations.addActivity(".activities.MainActivity", "@string/app_name",
				"orientation", "portrait");
		manifestOperations.addIntentFilterToActivity(mainActivity, "android.intent.action.MAIN",
				"android.intent.category.LAUNCHER");

		// Generating main activity class
		// Getting current package
		String activitiesPath = projectOperations.getFocusedTopLevelPackage().getFullyQualifiedPackageName()
				.concat(".activities");

		int modifier = Modifier.PUBLIC;
		JavaType target = new JavaType(activitiesPath.concat(".MainActivity"));
		final String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(target,
				pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
		File targetFile = new File(typeLocationService.getPhysicalTypeCanonicalPath(declaredByMetadataId));
		Validate.isTrue(!targetFile.exists(), "Type '%s' already exists", target);

		// Prepare class builder
		final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
				declaredByMetadataId, modifier, target, PhysicalTypeCategory.CLASS);

		// DatabaseConfigUtils extends Activity
		cidBuilder.addExtendsTypes(new JavaType("android.app.Activity"));

		// Including AndrooidMainActivity annotation
		cidBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType(AndrooidMainActivity.class)));

		typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

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
		return FEATURE_ANDROOID_ACTIVITY_LAYER;
	}

	public boolean isInstalledInModule(String moduleName) {
		final String manifestPath = pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RES, "mipmap-xhdpi/app_icon.png");
		return fileManager.exists(manifestPath);
	}
}