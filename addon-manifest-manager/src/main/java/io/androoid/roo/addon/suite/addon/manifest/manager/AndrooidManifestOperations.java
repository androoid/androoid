package io.androoid.roo.addon.suite.addon.manifest.manager;

import java.util.List;
import java.util.Map;

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.ProjectOperations;

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
	
	/**
	 * Returns AndroidManifest file
	 * 
	 * @param projectOperations
	 * @param fileManager
	 * @return MutableFile
	 */
	public MutableFile getAndroidManifestMutableFile(ProjectOperations projectOperations, FileManager fileManager);
	
	/**
	 * Add new attributes to application tag on AndroidManifest.xml file.
	 * 
	 * @param attributes
	 */
	void addApplicationConfig(Map<String, String> attributes);

	
	/**
	 * Add new system permission to AndroidManifest.xml file
	 * 
	 * @param permissionName
	 */
	void addPermission(String permissionName);
	
	/**
	 * Add multiple system permissions to AndroidManifest.xml file
	 * 
	 * @param List of permissionsNames
	 */
	void addPermissions(List<String> permissionsNames);

}