package io.androoid.roo.addon.suite.addon.activities;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

	protected void activate(final ComponentContext componentContext) {
		cContext = componentContext;
	}

	/** {@inheritDoc} */
	public boolean isSetupAvailable() {
		return projectOperations.isFeatureInstalled("androoid-project");

	}

	/** {@inheritDoc} */
	public void setup() {
		// Including basic files
		addBasicFiles(projectOperations.getFocusedTopLevelPackage());

		// Update AndroidManifest.xml with basic configuration
		updateBasicManifestConfiguration();

		// Update AndroidManifest.xml file with basic permissions
		addBasicPermissions();

	}

	/**
	 * Method that update application tag on AndroidManifest.xml file
	 */
	private void updateBasicManifestConfiguration() {

		try {
			MutableFile androidManifestXmlMutableFile = operationsUtils.getAndroidManifestMutableFile(projectOperations,
					fileManager);
			Document androidManifestXml = XmlUtils.getDocumentBuilder()
					.parse(androidManifestXmlMutableFile.getInputStream());
			Element root = androidManifestXml.getDocumentElement();

			// Getting application tag
			Element applicationElement = (Element) root.getElementsByTagName("application").item(0);

			// Including basic configuration
			applicationElement.setAttribute("android:icon", "@mipmap/app_icon");
			applicationElement.setAttribute("android:label", "@string/app_name");
			applicationElement.setAttribute("android:theme", "@style/AppTheme");

			XmlUtils.writeXml(androidManifestXmlMutableFile.getOutputStream(), androidManifestXml);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method that adds permissions definition on AndroidManifest.xml file
	 */
	private void addBasicPermissions() {

		try {
			MutableFile androidManifestXmlMutableFile = operationsUtils.getAndroidManifestMutableFile(projectOperations,
					fileManager);
			Document androidManifestXml = XmlUtils.getDocumentBuilder()
					.parse(androidManifestXmlMutableFile.getInputStream());
			Element root = androidManifestXml.getDocumentElement();

			// Create permissions
			Map<String, String> attributesLocation = new HashMap<String, String>();
			attributesLocation.put("android:name", "android.permission.ACCESS_COARSE_LOCATION");
			operationsUtils.insertXmlElement(androidManifestXml, root, "uses-permission", attributesLocation);

			Map<String, String> attributesFineLocation = new HashMap<String, String>();
			attributesFineLocation.put("android:name", "android.permission.ACCESS_FINE_LOCATION");
			operationsUtils.insertXmlElement(androidManifestXml, root, "uses-permission", attributesFineLocation);

			Map<String, String> attributesWifiState = new HashMap<String, String>();
			attributesWifiState.put("android:name", "android.permission.ACCESS_WIFI_STATE");
			operationsUtils.insertXmlElement(androidManifestXml, root, "uses-permission", attributesWifiState);

			Map<String, String> attributesNetworkState = new HashMap<String, String>();
			attributesNetworkState.put("android:name", "android.permission.ACCESS_NETWORK_STATE");
			operationsUtils.insertXmlElement(androidManifestXml, root, "uses-permission", attributesNetworkState);

			Map<String, String> attributesInternet = new HashMap<String, String>();
			attributesInternet.put("android:name", "android.permission.INTERNET");
			operationsUtils.insertXmlElement(androidManifestXml, root, "uses-permission", attributesInternet);

			Map<String, String> attributesWriteExternal = new HashMap<String, String>();
			attributesWriteExternal.put("android:name", "android.permission.WRITE_EXTERNAL_STORAGE");
			operationsUtils.insertXmlElement(androidManifestXml, root, "uses-permission", attributesWriteExternal);

			XmlUtils.writeXml(androidManifestXmlMutableFile.getOutputStream(), androidManifestXml);

		} catch (Exception e) {
			e.printStackTrace();
		}

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