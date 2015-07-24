package io.androoid.roo.addon.suite.addon.project;

import io.androoid.roo.addon.suite.addon.project.utils.AvailableSDKs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
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
import org.w3c.dom.NodeList;

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

	/** {@inheritDoc} */
	public boolean isCreateProjectAvailable() {
		return !projectOperations.isFeatureInstalled(FEATURE_ANDROOID_PROJECT)
				&& !projectOperations.isFocusedProjectAvailable();
	}

	/** {@inheritDoc} */
	public void setup(JavaPackage applicationId, AvailableSDKs minSdkVersion,
			AvailableSDKs targetSdkVersion) {
		// Create pom.xml file
		createPom(applicationId, minSdkVersion);
		// Create AndroidManifest.xml file
		createAndroidManifestFile(applicationId);
		// Add extra artifacts
		addConfigurationFiles(applicationId);
		addAppIcon();
	}

	/**
	 * Method that generates necessary configuration files like strings.xml and
	 * styles.xml
	 * 
	 * @param applicationId
	 */
	private void addConfigurationFiles(JavaPackage applicationId) {
		// Copying basic strings.xml file
		InputStream stringsXmlFile = FileUtils.getInputStream(getClass(),
				"values/strings.xml");

		final Document stringsFile = XmlUtils.readXml(stringsXmlFile);
		final Element stringsRoot = stringsFile.getDocumentElement();

		NodeList strings = stringsRoot.getElementsByTagName("string");
		for (int i = 0; i < strings.getLength(); i++) {
			Element item = (Element) strings.item(i);
			if (item.getAttribute("name").equals("app_name")) {
				item.setTextContent(applicationId.getLastElement());
			}
		}

		final String stringsPath = pathResolver.getFocusedIdentifier(
				Path.SRC_MAIN_RES, "values/strings.xml");
		final MutableFile mutableFile = fileManager.createFile(stringsPath);

		XmlUtils.writeXml(mutableFile.getOutputStream(), stringsFile);

		// Copying basic styles.xml file
		InputStream stylesXmlFile = FileUtils.getInputStream(getClass(),
				"values/styles.xml");

		final Document stylesFile = XmlUtils.readXml(stylesXmlFile);

		final String stylesPath = pathResolver.getFocusedIdentifier(
				Path.SRC_MAIN_RES, "values/styles.xml");
		final MutableFile mutableStylesFile = fileManager
				.createFile(stylesPath);

		XmlUtils.writeXml(mutableStylesFile.getOutputStream(), stylesFile);

	}

	/**
	 * Method that includes generated App Icon
	 */
	private void addAppIcon() {
		// Copying App icon
		final String appIconFile = pathResolver.getFocusedIdentifier(
				Path.SRC_MAIN_RES, "mipmap-xhdpi/app_icon.png");
		final String whiteIconFile = pathResolver.getFocusedIdentifier(
				Path.SRC_MAIN_RES, "drawable/logo_white.png");

		InputStream inputStream = null;
		InputStream inputStreamWhite = null;
		OutputStream outputStream = null;
		OutputStream outputStreamWhite = null;

		try {
			inputStream = FileUtils.getInputStream(getClass(),
					"mipmap-xhdpi/app_icon.png");
			inputStreamWhite = FileUtils.getInputStream(getClass(),
					"drawable/logo_white.png");
			if (!fileManager.exists(appIconFile)) {
				outputStream = fileManager.createFile(appIconFile)
						.getOutputStream();
				outputStreamWhite = fileManager.createFile(whiteIconFile)
						.getOutputStream();
			}
			if (outputStream != null) {
				IOUtils.copy(inputStream, outputStream);
			}
			if (outputStreamWhite != null) {
				IOUtils.copy(inputStreamWhite, outputStreamWhite);
			}
		} catch (final IOException ioe) {
			throw new IllegalStateException(ioe);
		} finally {
			IOUtils.closeQuietly(inputStream);
			if (outputStream != null) {
				IOUtils.closeQuietly(outputStream);
			}
			if (outputStreamWhite != null) {
				IOUtils.closeQuietly(outputStreamWhite);
			}

		}

	}

	/**
	 * Method that generates pom.xml
	 * 
	 * @param applicationId
	 * @param minSdkVersion
	 */
	private void createPom(JavaPackage applicationId,
			AvailableSDKs minSdkVersion) {
		Validate.isTrue(!fileManager.exists("pom.xml"),
				"'pom.xml' file exists!");

		// Load the pom template
		final InputStream templateInputStream = FileUtils.getInputStream(
				getClass(), "pom-template.xml");

		final Document pom = XmlUtils.readXml(templateInputStream);
		final Element root = pom.getDocumentElement();

		Element groupIdElement = (Element) root.getElementsByTagName("groupId")
				.item(0);
		groupIdElement.setTextContent(applicationId
				.getFullyQualifiedPackageName());

		Element artifactIdElement = (Element) root.getElementsByTagName(
				"artifactId").item(0);
		artifactIdElement.setTextContent(applicationId
				.getFullyQualifiedPackageName());

		Element platformElement = (Element) root.getElementsByTagName(
				"platform").item(0);
		platformElement.setTextContent(minSdkVersion.getApiLevel().toString());

		final MutableFile pomMutableFile = fileManager.createFile(pathResolver
				.getRoot() + "/pom.xml");

		XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);

	}

	/**
	 * A private method which creates AndroidManifest.xml File
	 * 
	 * @param applicationId
	 */
	private void createAndroidManifestFile(JavaPackage applicationId) {

		// Check if AndroidManifest file is already created
		Validate.isTrue(
				!fileManager.exists(pathResolver.getRoot().concat(
						"/src/main/AndroidManifest.xml")),
				"'AndroidManifest.xml' file exists!");

		// Load the AndroidManifest template
		final InputStream templateInputStream = FileUtils.getInputStream(
				getClass(), "AndroidManifest-template.xml");

		final Document androidManifest = XmlUtils.readXml(templateInputStream);
		final Element root = androidManifest.getDocumentElement();

		root.setAttribute("package",
				applicationId.getFullyQualifiedPackageName());

		final String manifestPath = pathResolver.getFocusedIdentifier(
				Path.SRC_MAIN, "AndroidManifest.xml");
		final MutableFile mutableFile = fileManager.createFile(manifestPath);

		XmlUtils.writeXml(mutableFile.getOutputStream(), androidManifest);

	}

	/**
	 * FEATURE METHODS
	 */

	public String getName() {
		return FEATURE_ANDROOID_PROJECT;
	}

	public boolean isInstalledInModule(String moduleName) {
		final String manifestPath = pathResolver.getFocusedIdentifier(
				Path.SRC_MAIN, "AndroidManifest.xml");
		return fileManager.exists(manifestPath);
	}
}