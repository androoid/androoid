package io.androoid.roo.addon.suite.addon.project;

import io.androoid.roo.addon.suite.addon.project.utils.AvailableSDKs;
import io.androoid.roo.addon.suite.dependency.manager.providers.DependencyManagerProviderId;

import org.springframework.roo.model.JavaPackage;

/**
 * Interface of Androoid project commands that are available via the Roo shell.
 *
 * @author Juan Carlos García
 * @since 1.0.0
 */
public interface AndrooidProjectOperations {

	/**
	 * Indicates if create an androoid project command should be available.
	 * 
	 * @return true if it should be available, otherwise false
	 */
	boolean isCreateProjectAvailable();

	/**
	 * Indicates if exists a generated android project on current folder
	 * 
	 * @return true if exists an android project, otherwise false
	 */
	boolean isAndrooidProjectGenerated();

	/**
	 * This method returns the DependencyManager provider that will be used to
	 * manage dependencies of the Android project
	 * 
	 * @return DependencyManagerProviderId
	 */
	DependencyManagerProviderId getProjectDependencyManager();

	/**
	 * Generates Android project structure.
	 * 
	 * @param applicationId
	 * @param minSdkVersion
	 * @param targetSdkVersion
	 * @param dependencyManager
	 */
	void setup(JavaPackage applicationId, AvailableSDKs minSdkVersion,
			AvailableSDKs targetSdkVersion,
			DependencyManagerProviderId dependencyManager);

}