package io.androoid.roo.addon.suite.addon.activities;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;

import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidListActivity;

/**
 * Provides {@link AndrooidActivityListMetadata}.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidActivityListMetadataProvider extends AbstractItdMetadataProvider {

	protected final static Logger LOGGER = HandlerUtils.getLogger(AndrooidActivityListMetadataProvider.class);

	public static final JavaType ANDROOID_LIST_ACTIVITY = new JavaType(AndrooidListActivity.class);

	@Reference
	ProjectOperations projectOperations;
	
	@Reference
	TypeLocationService typeLocationService;

	protected void activate(final ComponentContext cContext) {
		context = cContext.getBundleContext();
		getMetadataDependencyRegistry().addNotificationListener(this);
		getMetadataDependencyRegistry().registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(),
				getProvidesType());
		addMetadataTrigger(ANDROOID_LIST_ACTIVITY);
	}

	@Override
	protected String createLocalIdentifier(final JavaType javaType, final LogicalPath path) {
		return AndrooidActivityListMetadata.createIdentifier(javaType, path);
	}

	protected void deactivate(final ComponentContext context) {
		getMetadataDependencyRegistry().removeNotificationListener(this);
		getMetadataDependencyRegistry().deregisterDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(),
				getProvidesType());
		removeMetadataTrigger(ANDROOID_LIST_ACTIVITY);
	}

	@Override
	protected String getGovernorPhysicalTypeIdentifier(final String metadataIdentificationString) {
		final JavaType javaType = AndrooidActivityListMetadata.getJavaType(metadataIdentificationString);
		final LogicalPath path = AndrooidActivityListMetadata.getPath(metadataIdentificationString);
		return PhysicalTypeIdentifier.createIdentifier(javaType, path);
	}

	public String getItdUniquenessFilenameSuffix() {
		return "AndrooidListActivity";
	}

	@Override
	protected ItdTypeDetailsProvidingMetadataItem getMetadata(final String metadataIdentificationString,
			final JavaType aspectName, final PhysicalTypeMetadata governorPhysicalTypeMetadata,
			final String itdFilename) {

		// Getting Project Package
		JavaPackage projectPackage = projectOperations.getFocusedTopLevelPackage();

		// Getting related entity
		final JavaType listActivity = AndrooidActivityListMetadata.getJavaType(metadataIdentificationString);

		ClassOrInterfaceTypeDetails listActivityDetails = typeLocationService.getTypeDetails(listActivity);
		AnnotationMetadata annotation = listActivityDetails.getAnnotation(ANDROOID_LIST_ACTIVITY);

		AnnotationAttributeValue<JavaType> entityAttribute = annotation.getAttribute("entity");


		return new AndrooidActivityListMetadata(metadataIdentificationString, aspectName,
				governorPhysicalTypeMetadata, projectPackage, entityAttribute.getValue());
	}

	public String getProvidesType() {
		return AndrooidActivityListMetadata.getMetadataIdentiferType();
	}

}
