package io.androoid.roo.addon.suite.dependency.manager.providers;

import org.springframework.roo.model.JavaPackage;

/**
 * Interface of Androoid dependency manager provider.
 * 
 * With this interface, Androoid will be able to implement different types of
 * dependency managers like gradle or maven.
 * 
 * The provider to use on some generated project will be selected on project
 * generation.
 * 
 * Androoid project operations will implement a method that provides information
 * about which dependency manager is configured on generated project
 *
 * @author Juan Carlos García
 * @since 1.0.0
 */
public interface DependencyManagerProvider {

	/**
	 * All providers needs to be identidied by an unique name. This method gets
	 * the name that identifies current dependency manager provider.
	 * 
	 * @return A string with the provider name
	 */
	String getName();

	/**
	 * All providers have a basic description that allow developers to know
	 * which provider they want to use. This method gets the description that
	 * identifies current dependency manager provider.
	 * 
	 * @return A String with the provider description
	 */
	String getDescription();

	/**
	 * Install dependency manager on generated project
	 * 
	 * @param applicationId
	 * @param minSdkVersion
	 * @param targetSdkVersion
	 */
	void install(JavaPackage applicationId, String minSdkVersion,
			String targetSdkVersion);

	/**
	 * Checks if current DependencyManagerProvider is installed on current
	 * generated project
	 * 
	 * @return boolean
	 */
	boolean isInstalled();

	/**
	 * Add new dependency to generated project
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param version
	 */
	void addDependency(String groupId, String artifactId, String version);

}