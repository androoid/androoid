package io.androoid.roo.addon.suite.addon.persistence;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides {@link AndrooidDatabaseHelperMetadata}.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidDatabaseHelperMetadataProvider extends
		AbstractItdMetadataProvider {

	protected final static Logger LOGGER = HandlerUtils
			.getLogger(AndrooidDatabaseHelperMetadataProvider.class);

	public static final JavaType ANDROOID_DATABASE_HELPER = new JavaType(
			"io.androoid.roo.addon.suite.addon.persistence.annotations.AndrooidDatabaseHelper");

	@Reference
	ProjectOperations projectOperations;

	protected void activate(final ComponentContext cContext) {
		context = cContext.getBundleContext();
		getMetadataDependencyRegistry().addNotificationListener(this);
		getMetadataDependencyRegistry().registerDependency(
				PhysicalTypeIdentifier.getMetadataIdentiferType(),
				getProvidesType());
		addMetadataTrigger(ANDROOID_DATABASE_HELPER);
	}

	@Override
	protected String createLocalIdentifier(final JavaType javaType,
			final LogicalPath path) {
		return AndrooidDatabaseHelperMetadata.createIdentifier(javaType, path);
	}

	protected void deactivate(final ComponentContext context) {
		getMetadataDependencyRegistry().removeNotificationListener(this);
		getMetadataDependencyRegistry().deregisterDependency(
				PhysicalTypeIdentifier.getMetadataIdentiferType(),
				getProvidesType());
		removeMetadataTrigger(ANDROOID_DATABASE_HELPER);
	}

	@Override
	protected String getGovernorPhysicalTypeIdentifier(
			final String metadataIdentificationString) {
		final JavaType javaType = AndrooidDatabaseHelperMetadata
				.getJavaType(metadataIdentificationString);
		final LogicalPath path = AndrooidDatabaseHelperMetadata
				.getPath(metadataIdentificationString);
		return PhysicalTypeIdentifier.createIdentifier(javaType, path);
	}

	public String getItdUniquenessFilenameSuffix() {
		return "AndrooidDatabaseHelper";
	}

	@Override
	protected ItdTypeDetailsProvidingMetadataItem getMetadata(
			final String metadataIdentificationString,
			final JavaType aspectName,
			final PhysicalTypeMetadata governorPhysicalTypeMetadata,
			final String itdFilename) {
		
		// Getting DB Name
		String dbName = projectOperations.getFocusedTopLevelPackage().getLastElement();


		return new AndrooidDatabaseHelperMetadata(metadataIdentificationString, aspectName,
				governorPhysicalTypeMetadata, dbName);
	}

	public String getProvidesType() {
		return AndrooidDatabaseHelperMetadata.getMetadataIdentiferType();
	}

}
