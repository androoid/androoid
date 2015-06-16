package io.androoid.roo.addon.suite.dependency.manager.providers.maven;

import io.androoid.roo.addon.suite.dependency.manager.providers.DependencyManagerProvider;

import java.io.InputStream;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Maven dependency manager provider.
 * 
 * This class provides all necessary functionalities to manage your project
 * using maven.
 * 
 * @author Juan Carlos García
 * @since 1.0.0
 */
@Component
@Service
public class MavenDependencyManagerProvider implements
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

	public static final String NAME = "MAVEN";

	public static final String DESCRIPTION = "Uses Maven to manage dependencies of your generated project.";

	private static final Logger LOGGER = Logger
			.getLogger(MavenDependencyManagerProvider.class.getName());

	public String getName() {
		return NAME;
	}

	public String getDescription() {
		return DESCRIPTION;
	}

	public void addDependency(String groupId, String artifactId, String version) {
		// Checking that pom.xml exists
		Validate.isTrue(fileManager.exists("pom.xml"),
				"'pom.xml' file doesn't exists!");

		// Add dependency to pom.xml
		final InputStream templateInputStream = fileManager
				.getInputStream("pom.xml");

		final Document pom = XmlUtils.readXml(templateInputStream);
		final Element root = pom.getDocumentElement();

		Element dependenciesElement = (Element) root.getElementsByTagName(
				"dependencies").item(0);
		
		Element dependencyElement = pom.createElement("dependency");
		Element groupIdElement = pom.createElement("groupId");
		Element artifactIdElement = pom.createElement("artifactId");
		Element versionElement = pom.createElement("version");
		
		groupIdElement.setTextContent(groupId);
		artifactIdElement.setTextContent(artifactId);
		versionElement.setTextContent(version);

		dependencyElement.appendChild(groupIdElement);
		dependencyElement.appendChild(artifactIdElement);
		dependencyElement.appendChild(versionElement);
		
		dependenciesElement.appendChild(dependencyElement);
		
		final MutableFile mutableFile = fileManager.updateFile(pathResolver
				.getRoot() + "/pom.xml");

		XmlUtils.writeXml(mutableFile.getOutputStream(), pom);
	}

	public void install(JavaPackage applicationId, String minSdkVersion,
			String targetSdkVersion) {
		// Checking that pom.xml doesn't exists
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
		platformElement.setTextContent(minSdkVersion);

		final MutableFile mutableFile = fileManager.createFile(pathResolver
				.getRoot() + "/pom.xml");

		XmlUtils.writeXml(mutableFile.getOutputStream(), pom);

	}

	/** {@inheritDoc} */
	public boolean isInstalled() {
		return fileManager.exists("pom.xml");
	}

}
