package io.androoid.roo.addon.suite.addon.manifest.manager;

import org.springframework.roo.model.JavaPackage;

/**
 * Interface that defines all available methods on Add-On Manifest Manager
 *
 * @author Juan Carlos García
 * @since 1.0
 */
public interface AndrooidManifestOperations {

	/**
	 * Generates a basic AndroidManifest.xml file with application tag declared
	 * but not configured
	 * 
	 * @param applicationPackage
	 *            that will be included on AndroidManifest.xml file
	 */
	void createAndroidManifestFile(JavaPackage applicationPackage);

}