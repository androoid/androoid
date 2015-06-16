package io.androoid.roo.addon.suite.dependency.manager.providers;


/**
 * Dependency Manager class created to define the Dependency Manager provider
 * object.
 *
 * @author Juan Carlos García
 * @since 1.0.0
 */
public class DependencyManagerProviderId {

	private DependencyManagerProvider provider;
	private String name;
	private String description;
	private String className;

	public DependencyManagerProviderId(DependencyManagerProvider provider) {
		this.provider = provider;
		this.name = provider.getName();
		this.description = provider.getDescription();
		this.className = provider.getClass().getCanonicalName();
	}
	
	public DependencyManagerProvider getProvider(){
		return this.provider;
	}

	public String getId() {
		return this.name;
	}

	public String getDescription() {
		return description;
	}

	public boolean is(DependencyManagerProvider provider) {
		return name.equals(provider.getName())
				&& className.equals(provider.getClass().getCanonicalName());
	}

}