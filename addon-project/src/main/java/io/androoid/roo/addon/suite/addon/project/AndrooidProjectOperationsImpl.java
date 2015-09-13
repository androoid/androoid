package io.androoid.roo.addon.suite.addon.project;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
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

import io.androoid.roo.addon.suite.addon.manifest.manager.AndrooidManifestOperations;
import io.androoid.roo.addon.suite.addon.project.utils.AvailableSDKs;

/**
 * Implementation of {@link AndrooidProjectOperations} interface.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidProjectOperationsImpl implements AndrooidProjectOperations {

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
	private AndrooidManifestOperations manifestOperations;

	/** {@inheritDoc} */
	public boolean isCreateProjectAvailable() {
		return !projectOperations.isFeatureInstalled(FEATURE_ANDROOID_PROJECT)
				&& !projectOperations.isFocusedProjectAvailable();
	}

	/** {@inheritDoc} */
	public void setup(JavaPackage applicationId, AvailableSDKs minSdkVersion, AvailableSDKs targetSdkVersion) {
		// Create pom.xml file
		createPom(applicationId, minSdkVersion);
		// Create AndroidManifest.xml file
		manifestOperations.createAndroidManifestFile(applicationId);
	}

	/**
	 * Method that generates pom.xml
	 * 
	 * @param applicationId
	 * @param minSdkVersion
	 */
	private void createPom(JavaPackage applicationId, AvailableSDKs minSdkVersion) {
		Validate.isTrue(!fileManager.exists("pom.xml"), "'pom.xml' file exists!");

		// Load the pom template
		final InputStream templateInputStream = FileUtils.getInputStream(getClass(), "pom-template.xml");

		final Document pom = XmlUtils.readXml(templateInputStream);
		final Element root = pom.getDocumentElement();

		Element groupIdElement = (Element) root.getElementsByTagName("groupId").item(0);
		groupIdElement.setTextContent(applicationId.getFullyQualifiedPackageName());

		Element artifactIdElement = (Element) root.getElementsByTagName("artifactId").item(0);
		artifactIdElement.setTextContent(applicationId.getFullyQualifiedPackageName());

		Element platformElement = (Element) root.getElementsByTagName("platform").item(0);
		platformElement.setTextContent(minSdkVersion.getApiLevel().toString());

		final List<Element> versionElements = XmlUtils.findElements("//*[.='JAVA_VERSION']", root);
		for (final Element versionElement : versionElements) {
			versionElement.setTextContent("1.7");
		}

		final MutableFile pomMutableFile = fileManager.createFile(pathResolver.getRoot() + "/pom.xml");

		XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);

	}

	/**
	 * FEATURE METHODS
	 */

	public String getName() {
		return FEATURE_ANDROOID_PROJECT;
	}

	public boolean isInstalledInModule(String moduleName) {
		final String manifestPath = pathResolver.getFocusedIdentifier(Path.SRC_MAIN, "AndroidManifest.xml");
		return fileManager.exists(manifestPath);
	}
}