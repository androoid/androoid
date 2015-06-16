package io.androoid.roo.addon.suite.dependency.manager;

import java.util.List;

import io.androoid.roo.addon.suite.dependency.manager.providers.DependencyManagerProviderId;

/**
 * DependencyManager operations interface defines all necessary operations to
 * locate available Dependency manager providers
 * 
 * @author Juan Carlos García
 * @since 1.0.0
 */
public interface DependencyManagerOperations {

	/**
	 * Method that locates a Dependency Manager Provider using the name that
	 * identifies it
	 * 
	 * @param name
	 *            DependencyManager provider name that identifies the provider
	 * 
	 * @return A dependencyManagerProviderId if exists provider, otherwhise
	 *         returns null
	 */
	DependencyManagerProviderId getProviderByName(String name);

	/**
	 * Method that returns all available Dependency Manager providers
	 * 
	 * @return A List of DependencyManagerProviderId
	 */
	List<DependencyManagerProviderId> getProvidersId();
}
