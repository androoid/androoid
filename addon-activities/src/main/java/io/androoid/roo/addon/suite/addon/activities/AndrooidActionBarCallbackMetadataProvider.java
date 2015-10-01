package io.androoid.roo.addon.suite.addon.activities;

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

import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidActionBarCallback;

/**
 * Provides {@link AndrooidActionBarCallbackMetadata}.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidActionBarCallbackMetadataProvider extends AbstractItdMetadataProvider {

	protected final static Logger LOGGER = HandlerUtils.getLogger(AndrooidActionBarCallbackMetadataProvider.class);

	public static final JavaType ANDROOID_ACTION_BAR_CALLBACK_ANNOTATION = new JavaType(
			AndrooidActionBarCallback.class);

	protected void activate(final ComponentContext cContext) {
		context = cContext.getBundleContext();
		getMetadataDependencyRegistry().addNotificationListener(this);
		getMetadataDependencyRegistry().registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(),
				getProvidesType());
		addMetadataTrigger(ANDROOID_ACTION_BAR_CALLBACK_ANNOTATION);
	}

	@Override
	protected String createLocalIdentifier(final JavaType javaType, final LogicalPath path) {
		return AndrooidActionBarCallbackMetadata.createIdentifier(javaType, path);
	}

	protected void deactivate(final ComponentContext context) {
		getMetadataDependencyRegistry().removeNotificationListener(this);
		getMetadataDependencyRegistry().deregisterDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(),
				getProvidesType());
		removeMetadataTrigger(ANDROOID_ACTION_BAR_CALLBACK_ANNOTATION);
	}

	@Override
	protected String getGovernorPhysicalTypeIdentifier(final String metadataIdentificationString) {
		final JavaType javaType = AndrooidActionBarCallbackMetadata.getJavaType(metadataIdentificationString);
		final LogicalPath path = AndrooidActionBarCallbackMetadata.getPath(metadataIdentificationString);
		return PhysicalTypeIdentifier.createIdentifier(javaType, path);
	}

	public String getItdUniquenessFilenameSuffix() {
		return "AndrooidActionBarCallback";
	}

	@Override
	protected ItdTypeDetailsProvidingMetadataItem getMetadata(final String metadataIdentificationString,
			final JavaType aspectName, final PhysicalTypeMetadata governorPhysicalTypeMetadata,
			final String itdFilename) {

		return new AndrooidActionBarCallbackMetadata(metadataIdentificationString, aspectName,
				governorPhysicalTypeMetadata);
	}

	public String getProvidesType() {
		return AndrooidActionBarCallbackMetadata.getMetadataIdentiferType();
	}

}
