package io.androoid.roo.addon.suite.addon.activities;

import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
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

import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidFormActivity;
import io.androoid.roo.addon.suite.addon.entities.annotations.AndrooidEntity;

/**
 * Provides {@link AndrooidActivityFormMetadata}.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidActivityFormMetadataProvider extends AbstractItdMetadataProvider {

	protected final static Logger LOGGER = HandlerUtils.getLogger(AndrooidActivityFormMetadataProvider.class);

	public static final JavaType ANDROOID_FORM_ACTIVITY_ANNOTATION = new JavaType(AndrooidFormActivity.class);
	public static final JavaType ANDROOID_ENTITY_ANNOTATION = new JavaType(AndrooidEntity.class);

	@Reference
	ProjectOperations projectOperations;

	@Reference
	TypeLocationService typeLocationService;

	protected void activate(final ComponentContext cContext) {
		context = cContext.getBundleContext();
		getMetadataDependencyRegistry().addNotificationListener(this);
		getMetadataDependencyRegistry().registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(),
				getProvidesType());
		addMetadataTrigger(ANDROOID_FORM_ACTIVITY_ANNOTATION);
	}

	@Override
	protected String createLocalIdentifier(final JavaType javaType, final LogicalPath path) {
		return AndrooidActivityFormMetadata.createIdentifier(javaType, path);
	}

	protected void deactivate(final ComponentContext context) {
		getMetadataDependencyRegistry().removeNotificationListener(this);
		getMetadataDependencyRegistry().deregisterDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(),
				getProvidesType());
		removeMetadataTrigger(ANDROOID_FORM_ACTIVITY_ANNOTATION);
	}

	@Override
	protected String getGovernorPhysicalTypeIdentifier(final String metadataIdentificationString) {
		final JavaType javaType = AndrooidActivityFormMetadata.getJavaType(metadataIdentificationString);
		final LogicalPath path = AndrooidActivityFormMetadata.getPath(metadataIdentificationString);
		return PhysicalTypeIdentifier.createIdentifier(javaType, path);
	}

	public String getItdUniquenessFilenameSuffix() {
		return "AndrooidFormActivity";
	}

	@Override
	protected ItdTypeDetailsProvidingMetadataItem getMetadata(final String metadataIdentificationString,
			final JavaType aspectName, final PhysicalTypeMetadata governorPhysicalTypeMetadata,
			final String itdFilename) {

		// Getting Project Package
		JavaPackage projectPackage = projectOperations.getFocusedTopLevelPackage();

		// Getting related entity
		final JavaType formActivity = AndrooidActivityFormMetadata.getJavaType(metadataIdentificationString);

		ClassOrInterfaceTypeDetails formActivityDetails = typeLocationService.getTypeDetails(formActivity);
		AnnotationMetadata annotation = formActivityDetails.getAnnotation(ANDROOID_FORM_ACTIVITY_ANNOTATION);

		AnnotationAttributeValue<JavaType> entityAttribute = annotation.getAttribute("entity");

		Validate.notNull(entityAttribute, "ERROR: @AndrooidFormActivity needs to specify entity attribute.");

		// Getting entity Id field
		JavaType entity = entityAttribute.getValue();

		Validate.notNull(entity, "ERROR: @AndrooidFormctivity needs to specify a valid entity attribute.");

		ClassOrInterfaceTypeDetails entityDetails = typeLocationService.getTypeDetails(entity);

		// Getting @AndrooidEntity annotation and attributes
		AnnotationMetadata entityAnnotation = entityDetails.getAnnotation(ANDROOID_ENTITY_ANNOTATION);

		Validate.notNull(entityAnnotation, "ERROR: Only entities annotated with @AndrooidEntity are allowed.");

		AnnotationAttributeValue<String> identifierFieldNameAttr = entityAnnotation.getAttribute("identifierField");
		AnnotationAttributeValue<Class<?>> identifierFieldTypeAttr = entityAnnotation.getAttribute("identifierType");

		String entityIdFieldName = AndrooidEntity.ID_FIELD_DEFAULT;
		JavaType entityIdFieldType = JavaType.LONG_OBJECT;

		if (identifierFieldNameAttr != null) {
			entityIdFieldName = identifierFieldNameAttr.getValue();
		}

		if (identifierFieldTypeAttr != null) {
			entityIdFieldType = new JavaType(identifierFieldTypeAttr.getValue());
		}

		return new AndrooidActivityFormMetadata(metadataIdentificationString, aspectName, governorPhysicalTypeMetadata,
				projectPackage, entity, entityIdFieldName, entityIdFieldType);
	}

	public String getProvidesType() {
		return AndrooidActivityFormMetadata.getMetadataIdentiferType();
	}

}
