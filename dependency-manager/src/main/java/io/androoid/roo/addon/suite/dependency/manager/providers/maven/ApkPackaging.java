package io.androoid.roo.addon.suite.dependency.manager.providers.maven;

import java.util.Collection;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.packaging.AbstractPackagingProvider;
import org.springframework.roo.project.packaging.PackagingProvider;

/**
 * The Maven "APK" {@link PackagingProvider}.
 * 
 * Is necessary to include this new Packaging provider on Androoid Spring Roo
 * Add-On Suite to be able to create new APK packaging POMs.
 * 
 * @author Juan Carlos García
 * @since 1.0.0
 */
@Component
@Service
public class ApkPackaging extends AbstractPackagingProvider {

	/**
	 * Constructor
	 */
	public ApkPackaging() {
		super("apk", "apk", "parent-apk-template.xml");
	}

	@Override
	protected void createOtherArtifacts(final JavaPackage topLevelPackage,
			final String module, final ProjectOperations projectOperations) {
		// No artifacts are applicable for POM modules
	}

	public Collection<Path> getPaths() {
		return null;
	}

	public boolean isDefault() {
		// TODO Auto-generated method stub
		return false;
	}
}
