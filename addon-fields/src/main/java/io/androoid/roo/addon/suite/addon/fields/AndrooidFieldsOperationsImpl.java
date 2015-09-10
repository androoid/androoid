package io.androoid.roo.addon.suite.addon.fields;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.jsr303.FieldDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.ReservedWords;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

import io.androoid.roo.addon.suite.addon.entities.annotations.AndrooidEntity;

/**
 * Implementation of {@link AndrooidFieldsOperations} interface.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidFieldsOperationsImpl implements AndrooidFieldsOperations {

	/**
	 * Get hold of a JDK Logger
	 */
	private Logger LOGGER = Logger.getLogger(getClass().getName());

	@Reference
	private ProjectOperations projectOperations;

	@Reference
	private TypeLocationService typeLocationService;

	@Reference
	private TypeManagementService typeManagementService;

	/** {@inheritDoc} */
	public boolean isFieldCreationAvailable() {
		return projectOperations.isFeatureInstalled("androoid-persistence");
	}

	/** {@inheritDoc} */
	public void createField(JavaType entity, JavaSymbolName fieldName, JavaType fieldType) {
		typeManagementService.addField(getFieldMetadata(entity, fieldName, fieldType).build());
	}

	/** {@inheritDoc} */
	public void createReferencedField(JavaType entity, JavaSymbolName fieldName, JavaType entityToReference) {

		// Check if fieldType is an entity
		ClassOrInterfaceTypeDetails details = typeLocationService.getTypeDetails(entityToReference);
		AnnotationMetadata androoidEntityAnnotation = details.getAnnotation(new JavaType(AndrooidEntity.class));

		Validate.notNull(androoidEntityAnnotation, String.format("Referenced type '%s' is not a valid Androoid Entity.",
				entityToReference.getSimpleTypeName()));

		FieldMetadataBuilder newField = getFieldMetadata(entity, fieldName, entityToReference);

		// Including params on @DatabaseField annotation
		AnnotationMetadataBuilder databaseFieldAnnotation = newField
				.getDeclaredTypeAnnotation(new JavaType("com.j256.ormlite.field.DatabaseField"));

		databaseFieldAnnotation.addBooleanAttribute("foreign", true);
		databaseFieldAnnotation.addBooleanAttribute("foreignAutoRefresh", true);
		databaseFieldAnnotation.addBooleanAttribute("canBeNull", true);

		typeManagementService.addField(newField.build());
	}

	/**
	 * Method that generates field metadata to be used on different create
	 * fields methods
	 * 
	 * @param entity
	 * @param fieldName
	 * @param fieldType
	 * @return
	 */
	public FieldMetadataBuilder getFieldMetadata(JavaType entity, JavaSymbolName fieldName, JavaType fieldType) {

		// Check if entity exists
		Set<ClassOrInterfaceTypeDetails> allEntities = typeLocationService
				.findClassesOrInterfaceDetailsWithAnnotation(new JavaType(AndrooidEntity.class));

		Validate.notEmpty(allEntities, "Create an entity before to add new fields!");

		Iterator<ClassOrInterfaceTypeDetails> it = allEntities.iterator();
		boolean entityExists = false;
		boolean fieldExists = false;
		while (it.hasNext()) {
			ClassOrInterfaceTypeDetails projectEntity = it.next();
			if (projectEntity.getType().equals(entity)) {
				entityExists = true;
				// Check if field exists on current entity
				ClassOrInterfaceTypeDetails entityDetails = typeLocationService.getTypeDetails(projectEntity.getType());
				FieldMetadata entityField = entityDetails.getDeclaredField(fieldName);
				if (entityField != null) {
					fieldExists = true;
				}
				break;
			}
		}

		Validate.isTrue(entityExists, String.format("Entity %s doesn't exists on current Android project.",
				entity.getFullyQualifiedTypeName()));

		Validate.isTrue(!fieldExists,
				String.format("Field name %s exists on entity %s", fieldName, entity.getFullyQualifiedTypeName()));

		final ClassOrInterfaceTypeDetails cid = typeLocationService.getTypeDetails(entity);
		final String physicalTypeIdentifier = cid.getDeclaredByMetadataId();
		FieldDetails fieldDetails = new FieldDetails(physicalTypeIdentifier, fieldType, fieldName);
		// Checking not reserved words on fieldName
		ReservedWords.verifyReservedWordsNotPresent(fieldDetails.getFieldName());

		// Adding Annotation @DatabaseField
		List<AnnotationMetadataBuilder> fieldAnnotations = new ArrayList<AnnotationMetadataBuilder>();
		AnnotationMetadataBuilder databaseFieldAnnotation = new AnnotationMetadataBuilder(
				new JavaType("com.j256.ormlite.field.DatabaseField"));
		fieldAnnotations.add(databaseFieldAnnotation);
		fieldDetails.setAnnotations(fieldAnnotations);

		// Adding Modifier
		fieldDetails.setModifiers(Modifier.PRIVATE);

		final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(fieldDetails);

		return fieldBuilder;
	}

	/**
	 * Method that uses configuration.xml file to install dependencies and
	 * properties on current pom.xml
	 */
	private void installDependencies() {
		final Element configuration = XmlUtils.getConfiguration(getClass());

		// Add properties
		List<Element> properties = XmlUtils.findElements("/configuration/androoid/properties/*", configuration);
		for (Element property : properties) {
			projectOperations.addProperty(projectOperations.getFocusedModuleName(), new Property(property));
		}

		// Add dependencies
		List<Element> elements = XmlUtils.findElements("/configuration/androoid/dependencies/dependency",
				configuration);
		List<Dependency> dependencies = new ArrayList<Dependency>();
		for (Element element : elements) {
			Dependency dependency = new Dependency(element);
			dependencies.add(dependency);
		}
		projectOperations.addDependencies(projectOperations.getFocusedModuleName(), dependencies);
	}

}