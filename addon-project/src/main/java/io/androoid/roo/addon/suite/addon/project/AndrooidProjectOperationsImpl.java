package io.androoid.roo.addon.suite.addon.project;

import io.androoid.roo.addon.suite.addon.project.utils.AvailableSDKs;
import io.androoid.roo.addon.suite.dependency.manager.providers.DependencyManagerProviderId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
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
 * @since 1.0.0
 */
@Component
@Service
public class AndrooidProjectOperationsImpl implements AndrooidProjectOperations {

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
	 * Get a reference to the PathResolver from the undelying OSGi container
	 */
	@Reference
	private PathResolver pathResolver;

	/** {@inheritDoc} */
	public boolean isCreateProjectAvailable() {
		return !fileManager.exists(pathResolver.getRoot().concat(
				"/src/main/AndroidManifest.xml"))
				&& !projectOperations.isFocusedProjectAvailable();
	}

	/** {@inheritDoc} */
	public void setup(JavaPackage applicationId, AvailableSDKs minSdkVersion,
			AvailableSDKs targetSdkVersion,
			DependencyManagerProviderId dependencyManager) {

		// Prints Android logo and start message
		printAndroidLogo();

		// Creates dependency manager file
		dependencyManager.getProvider().install(applicationId,
				minSdkVersion.getApiLevel().toString(),
				targetSdkVersion.getApiLevel().toString());

		// Creates project structure
		createProjectStructure(applicationId);

		// Creates AndroidManifest.xml file
		createAndroidManifestFile(applicationId);
	}

	/** {@inheritDoc} */
	public boolean isAndrooidProjectGenerated() {
		return fileManager.exists(pathResolver.getRoot().concat(
						"/src/main/AndroidManifest.xml"));
	}

	public DependencyManagerProviderId getProjectDependencyManager() {
		// TODO Auto-generated method stub
		return null;
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

		final MutableFile mutableFile = fileManager.createFile(pathResolver
				.getRoot() + "/src/main/AndroidManifest.xml");

		XmlUtils.writeXml(mutableFile.getOutputStream(), androidManifest);

	}

	/**
	 * A private method which creates Android project folder structure
	 * 
	 * @param applicationId
	 */
	private void createProjectStructure(JavaPackage applicationId) {

		// Creating JAVA folder
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/java"));
		// Getting package name
		List<String> foldersToCreate = applicationId.getElements();
		String folderToCreate = "";
		for (String folder : foldersToCreate) {
			folderToCreate += folder.concat("/");
		}
		fileManager.createDirectory(pathResolver.getRoot()
				.concat("/src/main/java/").concat(folderToCreate));
		// Creating RESOURCES folders
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/res"));
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/res/drawable-hdpi"));
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/res/drawable-mdpi"));
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/res/drawable-xhdpi"));
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/res/drawable-xxhdpi"));
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/res/drawable"));
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/res/layout"));
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/res/menu"));
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/res/mipmap-xhdpi"));
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/res/raw"));
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/res/values-w820dp"));
		fileManager.createDirectory(pathResolver.getRoot().concat(
				"/src/main/res/values"));

		// Is necessary to include some basic elements on generated project
		// to allow developer to compile project after execute project setup
		// command.

		// Copying App icon
		final String appIconFile = pathResolver.getRoot().concat(
				"/src/main/res/mipmap-xhdpi/app_icon.png");

		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = FileUtils.getInputStream(getClass(),
					"mipmap-xhdpi/app_icon.png");
			if (!fileManager.exists(appIconFile)) {
				outputStream = fileManager.createFile(appIconFile)
						.getOutputStream();
			}
			if (outputStream != null) {
				IOUtils.copy(inputStream, outputStream);
			}
		} catch (final IOException ioe) {
			throw new IllegalStateException(ioe);
		} finally {
			IOUtils.closeQuietly(inputStream);
			if (outputStream != null) {
				IOUtils.closeQuietly(outputStream);
			}

		}

		// Copying White app icon
		final String whiteIconFile = pathResolver.getRoot().concat(
				"/src/main/res/drawable/logo_white.png");

		try {
			inputStream = FileUtils.getInputStream(getClass(),
					"drawable/logo_white.png");
			if (!fileManager.exists(whiteIconFile)) {
				outputStream = fileManager.createFile(whiteIconFile)
						.getOutputStream();
			}
			if (outputStream != null) {
				IOUtils.copy(inputStream, outputStream);
			}
		} catch (final IOException ioe) {
			throw new IllegalStateException(ioe);
		} finally {
			IOUtils.closeQuietly(inputStream);
			if (outputStream != null) {
				IOUtils.closeQuietly(outputStream);
			}

		}

		// Copying basic strings.xml file
		InputStream stringsXmlFile = FileUtils.getInputStream(getClass(),
				"values/strings.xml");

		final Document stringsFile = XmlUtils.readXml(stringsXmlFile);
		final Element root = stringsFile.getDocumentElement();

		NodeList strings = root.getElementsByTagName("string");
		for (int i = 0; i < strings.getLength(); i++) {
			Element item = (Element) strings.item(i);
			if (item.getAttribute("name").equals("app_name")) {
				item.setTextContent(applicationId.getLastElement());
			}
		}

		final MutableFile mutableFile = fileManager.createFile(pathResolver
				.getRoot() + "/src/main/res/values/strings.xml");

		XmlUtils.writeXml(mutableFile.getOutputStream(), stringsFile);

		// Copying basic styles.xml file
		InputStream stylesXmlFile = FileUtils.getInputStream(getClass(),
				"values/styles.xml");

		final Document stylesFile = XmlUtils.readXml(stylesXmlFile);
		final MutableFile mutableStylesFile = fileManager
				.createFile(pathResolver.getRoot()
						+ "/src/main/res/values/styles.xml");

		XmlUtils.writeXml(mutableStylesFile.getOutputStream(), stylesFile);

	}

	/**
	 * A private method which prints android logo on Spring Roo Shell only to
	 * show user that android project will be generated
	 * 
	 */
	private void printAndroidLogo() {
		LOGGER.log(Level.INFO, "Generating Android project...");

		// TODO: Print Android logo with more info about Androoid version
	}
}