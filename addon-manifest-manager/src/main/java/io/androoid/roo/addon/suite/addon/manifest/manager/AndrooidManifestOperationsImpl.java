package io.androoid.roo.addon.suite.addon.manifest.manager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.androoid.roo.addon.suite.support.AndrooidOperationsUtils;

/**
 * Implementation of {@link AndrooidManifestOperations} interface.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidManifestOperationsImpl implements AndrooidManifestOperations {

	/**
	 * Get hold of a JDK Logger
	 */
	private Logger LOGGER = Logger.getLogger(getClass().getName());

	@Reference
	private FileManager fileManager;
	@Reference
	private ProjectOperations projectOperations;
	@Reference
	private PathResolver pathResolver;
	@Reference
	private AndrooidOperationsUtils operationsUtils;

	/** {@inheritDoc} */
	public void createAndroidManifestFile(JavaPackage applicationPackage) {

		// Check if AndroidManifest file is already created
		Validate.isTrue(!fileManager.exists(pathResolver.getRoot().concat("/src/main/AndroidManifest.xml")),
				"'AndroidManifest.xml' file exists!");

		// Load the AndroidManifest template
		final InputStream templateInputStream = FileUtils.getInputStream(getClass(), "AndroidManifest-template.xml");

		final Document androidManifest = XmlUtils.readXml(templateInputStream);
		final Element root = androidManifest.getDocumentElement();

		root.setAttribute("package", applicationPackage.getFullyQualifiedPackageName());

		final String manifestPath = pathResolver.getFocusedIdentifier(Path.SRC_MAIN, "AndroidManifest.xml");
		final MutableFile mutableFile = fileManager.createFile(manifestPath);

		XmlUtils.writeXml(mutableFile.getOutputStream(), androidManifest);

	}

	/** {@inheritDoc} */
	public MutableFile getAndroidManifestMutableFile(ProjectOperations projectOperations, FileManager fileManager) {
		LogicalPath resourcesPath = operationsUtils.getMainPath(projectOperations);
		String androidManifestXmlPath = projectOperations.getPathResolver().getIdentifier(resourcesPath,
				"AndroidManifest.xml");
		Validate.isTrue(fileManager.exists(androidManifestXmlPath), "src/main/AndroidManifest.xml not found");

		MutableFile androidManifestXmlMutableFile = null;

		try {
			androidManifestXmlMutableFile = fileManager.updateFile(androidManifestXmlPath);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		return androidManifestXmlMutableFile;
	}

	/** {@inheriDoc} */
	public void addApplicationConfig(Map<String, String> attributes) {

		try {
			MutableFile androidManifestXmlMutableFile = getAndroidManifestMutableFile(projectOperations, fileManager);
			Document androidManifestXml = XmlUtils.getDocumentBuilder()
					.parse(androidManifestXmlMutableFile.getInputStream());
			Element root = androidManifestXml.getDocumentElement();

			// Getting application tag
			Element applicationElement = (Element) root.getElementsByTagName("application").item(0);

			// Including basic configuration
			for (Entry<String, String> attribute : attributes.entrySet()) {
				applicationElement.setAttribute(attribute.getKey(), attribute.getValue());
			}

			XmlUtils.writeXml(androidManifestXmlMutableFile.getOutputStream(), androidManifestXml);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/** {@inheritDoc} */
	public void addPermission(String permissionName) {
		try {
			MutableFile androidManifestXmlMutableFile = getAndroidManifestMutableFile(projectOperations, fileManager);
			Document androidManifestXml = XmlUtils.getDocumentBuilder()
					.parse(androidManifestXmlMutableFile.getInputStream());
			Element root = androidManifestXml.getDocumentElement();

			Map<String, String> permissionAttr = new HashMap<String, String>();
			permissionAttr.put("android:name", permissionName);
			operationsUtils.insertXmlElement(androidManifestXml, root, "uses-permission", permissionAttr);

			XmlUtils.writeXml(androidManifestXmlMutableFile.getOutputStream(), androidManifestXml);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/** {@inheritDoc} */
	public void addPermissions(List<String> permissionsNames) {
		try {
			MutableFile androidManifestXmlMutableFile = getAndroidManifestMutableFile(projectOperations, fileManager);
			Document androidManifestXml = XmlUtils.getDocumentBuilder()
					.parse(androidManifestXmlMutableFile.getInputStream());
			Element root = androidManifestXml.getDocumentElement();

			for (String permissionName : permissionsNames) {
				Map<String, String> permissionAttr = new HashMap<String, String>();
				permissionAttr.put("android:name", permissionName);
				operationsUtils.insertXmlElement(androidManifestXml, root, "uses-permission", permissionAttr);
			}

			XmlUtils.writeXml(androidManifestXmlMutableFile.getOutputStream(), androidManifestXml);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	public Element addActivity(String name, String label, String configChanges, String screenOrientation) {
		try {
			MutableFile androidManifestXmlMutableFile = getAndroidManifestMutableFile(projectOperations, fileManager);
			Document androidManifestXml = XmlUtils.getDocumentBuilder()
					.parse(androidManifestXmlMutableFile.getInputStream());

			NodeList applicationElements = androidManifestXml.getElementsByTagName("application");

			if (applicationElements.getLength() < 0) {
				throw new RuntimeException("Error getting application element from AndroidManifest.xml file");
			}

			// Getting first application element
			Element applicationElement = (Element) applicationElements.item(0);

			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("android:name", name);
			attributes.put("android:label", label);
			attributes.put("android:configChanges", configChanges);
			attributes.put("android:screenOrientation", screenOrientation);
			Element activity = operationsUtils.insertXmlElement(androidManifestXml, applicationElement, "activity",
					attributes);

			XmlUtils.writeXml(androidManifestXmlMutableFile.getOutputStream(), androidManifestXml);

			return activity;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	/** {@inheritDoc} */
	public void addIntentFilterToActivity(Element activity, String actionName, String categoryName) {
		try {
			MutableFile androidManifestXmlMutableFile = getAndroidManifestMutableFile(projectOperations, fileManager);
			Document androidManifestXml = XmlUtils.getDocumentBuilder()
					.parse(androidManifestXmlMutableFile.getInputStream());

			NodeList applicationElements = androidManifestXml.getElementsByTagName("application");

			if (applicationElements.getLength() < 0) {
				throw new RuntimeException("Error getting application element from AndroidManifest.xml file");
			}

			// Getting first application element
			Element applicationElement = (Element) applicationElements.item(0);

			NodeList allChilds = applicationElement.getChildNodes();
			for (int i = 0; i < allChilds.getLength(); i++) {
				Node child = allChilds.item(i);

				if (child.isEqualNode(activity)) {
					Element intentFilter = androidManifestXml.createElement("intent-filter");

					Element actionElement = androidManifestXml.createElement("action");
					actionElement.setAttribute("android:name", actionName);

					Element categoryElement = androidManifestXml.createElement("category");
					categoryElement.setAttribute("android:name", categoryName);

					intentFilter.appendChild(actionElement);
					intentFilter.appendChild(categoryElement);

					// Append intent-filter element to current activity
					child.appendChild(intentFilter);
				}
			}

			XmlUtils.writeXml(androidManifestXmlMutableFile.getOutputStream(), androidManifestXml);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}