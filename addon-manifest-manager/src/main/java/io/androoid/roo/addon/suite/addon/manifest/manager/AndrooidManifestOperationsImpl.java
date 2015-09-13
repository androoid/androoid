package io.androoid.roo.addon.suite.addon.manifest.manager;

import java.io.InputStream;
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
}