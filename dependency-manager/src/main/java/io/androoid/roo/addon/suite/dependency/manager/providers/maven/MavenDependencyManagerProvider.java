package io.androoid.roo.addon.suite.dependency.manager.providers.maven;

import io.androoid.roo.addon.suite.dependency.manager.providers.DependencyManagerProvider;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;

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
		// TODO Auto-generated method stub

	}

	public void install(JavaPackage applicationId, String minSdkVersion,
			String targetSdkVersion) {
		// TODO Auto-generated method stub
		
	}

}
