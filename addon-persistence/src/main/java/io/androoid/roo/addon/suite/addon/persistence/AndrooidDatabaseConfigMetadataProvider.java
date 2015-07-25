package io.androoid.roo.addon.suite.addon.persistence;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides {@link AndrooidDatabaseConfigMetadata}.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidDatabaseConfigMetadataProvider extends AbstractItdMetadataProvider {
	
	protected final static Logger LOGGER = HandlerUtils.getLogger(AndrooidDatabaseConfigMetadataProvider.class);
	
    public static final JavaType ANDROOID_DATABASE_CONFIG = new JavaType(
            "io.androoid.roo.addon.suite.addon.persistence.annotations.AndrooidDatabaseConfig");


	protected void activate(final ComponentContext cContext) {
		context = cContext.getBundleContext();
		getMetadataDependencyRegistry().addNotificationListener(this);
		getMetadataDependencyRegistry().registerDependency(
				PhysicalTypeIdentifier.getMetadataIdentiferType(),
				getProvidesType());
		addMetadataTrigger(ANDROOID_DATABASE_CONFIG);
	}

	@Override
	protected String createLocalIdentifier(final JavaType javaType,
			final LogicalPath path) {
		return AndrooidDatabaseConfigMetadata.createIdentifier(javaType, path);
	}

	protected void deactivate(final ComponentContext context) {
		getMetadataDependencyRegistry().removeNotificationListener(this);
		getMetadataDependencyRegistry().deregisterDependency(
				PhysicalTypeIdentifier.getMetadataIdentiferType(),
				getProvidesType());
		removeMetadataTrigger(ANDROOID_DATABASE_CONFIG);
	}

	@Override
	protected String getGovernorPhysicalTypeIdentifier(
			final String metadataIdentificationString) {
		final JavaType javaType = AndrooidDatabaseConfigMetadata
				.getJavaType(metadataIdentificationString);
		final LogicalPath path = AndrooidDatabaseConfigMetadata
				.getPath(metadataIdentificationString);
		return PhysicalTypeIdentifier.createIdentifier(javaType, path);
	}


	public String getItdUniquenessFilenameSuffix() {
		return "AndrooidDatabaseConfig";
	}

	@Override
	protected ItdTypeDetailsProvidingMetadataItem getMetadata(
			final String metadataIdentificationString,
			final JavaType aspectName,
			final PhysicalTypeMetadata governorPhysicalTypeMetadata,
			final String itdFilename) {


		return new AndrooidDatabaseConfigMetadata(metadataIdentificationString, aspectName,
				governorPhysicalTypeMetadata);
	}

	public String getProvidesType() {
		return AndrooidDatabaseConfigMetadata.getMetadataIdentiferType();
	}

}
