package io.androoid.roo.addon.suite.addon.activities;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidFormActivity;
import io.androoid.roo.addon.suite.addon.fields.annotations.AndrooidReferencedField;

/**
 * Metadata for {@link AndrooidFormActivity} annotation.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public class AndrooidActivityFormMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

	private static final JavaType ARRAY_ADAPTER_JAVATYPE = new JavaType("android.widget.ArrayAdapter");
	private static final String PROVIDES_TYPE_STRING = AndrooidActivityFormMetadata.class.getName();
	private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

	private final ImportRegistrationResolver importResolver;
	private final JavaType entity;
	private final JavaPackage applicationPackage;
	private final JavaType listEntityJavaType;
	private final JavaType arrayListEntityJavaType;
	private final String getIdFieldMethod;
	private final JavaType entityIdFieldType;
	private final List<FieldMetadata> entityFields;

	public static String createIdentifier(final JavaType javaType, final LogicalPath path) {
		return PhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, javaType, path);
	}

	public static JavaType getJavaType(final String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING, metadataIdentificationString);
	}

	public static String getMetadataIdentiferType() {
		return PROVIDES_TYPE;
	}

	public static LogicalPath getPath(final String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING, metadataIdentificationString);
	}

	public static boolean isValid(final String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING, metadataIdentificationString);
	}

	/**
	 * Constructor
	 * 
	 * @param identifier
	 *            the ID of the metadata to create (must be a valid ID)
	 * @param aspectName
	 *            the name of the ITD to be created (required)
	 * @param governorPhysicalTypeMetadata
	 *            the governor (required)
	 * @param projectPackage
	 * @param entity
	 *            JavaType entity that relates activity with a entity model
	 * @param entityIdFieldName
	 *            String that contains the identifier field name of current
	 *            entity
	 * @param entityIdFieldType
	 *            JavaType that contains the type of the identifier field of the
	 *            current entity
	 * 
	 */
	public AndrooidActivityFormMetadata(final String identifier, final JavaType aspectName,
			final PhysicalTypeMetadata governorPhysicalTypeMetadata, JavaPackage projectPackage, JavaType entity,
			String entityIdFieldName, JavaType entityIdFieldType, List<FieldMetadata> entityFields) {
		super(identifier, aspectName, governorPhysicalTypeMetadata);
		Validate.isTrue(isValid(identifier), "Metadata identification string '%s' does not appear to be a valid",
				identifier);

		this.importResolver = builder.getImportRegistrationResolver();
		this.entity = entity;
		this.applicationPackage = projectPackage;
		this.listEntityJavaType = new JavaType("java.util.List", 0, DataType.TYPE, null, Arrays.asList(entity));
		this.arrayListEntityJavaType = new JavaType("java.util.ArrayList", 0, DataType.TYPE, null,
				Arrays.asList(entity));
		this.getIdFieldMethod = "get"
				.concat(Character.toUpperCase(entityIdFieldName.charAt(0)) + entityIdFieldName.substring(1));
		this.entityIdFieldType = entityIdFieldType;
		this.entityFields = entityFields;

		// Adding fields
		addFormActivityFields();

		// Create a representation of the desired output ITD
		itdTypeDetails = builder.build();

	}

	/**
	 * Method to add all necessary fields to FormActivity .aj file
	 */
	private void addFormActivityFields() {
		FieldMetadataBuilder adapterField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
				new JavaSymbolName(entity.getSimpleTypeName().toLowerCase()), entity, null);
		builder.addField(adapterField);

		boolean hasGeoField = false;

		// Adding fields on current form activity
		for (FieldMetadata field : entityFields) {

			// Checking if current field is a valid Database Field
			AnnotationMetadata databaseFieldAnnotation = field
					.getAnnotation(new JavaType("com.j256.ormlite.field.DatabaseField"));
			if (databaseFieldAnnotation != null) {

				boolean hasReferencedField = false;

				// Checking if field is a generatedId field
				AnnotationAttributeValue<Boolean> generatedIdAttr = databaseFieldAnnotation.getAttribute("generatedId");

				boolean generatedId = false;

				if (generatedIdAttr != null) {
					generatedId = generatedIdAttr.getValue();
				}

				if (!generatedId) {

					// Getting field type
					JavaType fieldType = field.getFieldType();
					JavaType formFieldType = null;

					if (fieldType.equals(JavaType.BOOLEAN_PRIMITIVE) || fieldType.equals(JavaType.BOOLEAN_OBJECT)) {
						formFieldType = new JavaType("android.widget.Switch");
					} else if (fieldType.equals(new JavaType("org.osmdroid.util.GeoPoint"))) {
						formFieldType = new JavaType("org.osmdroid.views.MapView");
						hasGeoField = true;
					} else if (isReferencedField(field)) {
						formFieldType = new JavaType("android.widget.Spinner");
						hasReferencedField = true;
					} else {
						formFieldType = new JavaType("android.widget.EditText");
					}

					String fieldName = entity.getSimpleTypeName().toLowerCase()
							+ Character.toLowerCase(field.getFieldName().getSymbolName().charAt(0))
							+ field.getFieldName().getSymbolName().substring(1)
									.concat(formFieldType.getSimpleTypeName());

					FieldMetadataBuilder entityField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
							new JavaSymbolName(fieldName), formFieldType, null);
					builder.addField(entityField);

					// If is a GEO field, is necessary to add an Edit Text
					// to make some geo search
					if (hasGeoField) {
						fieldName = entity.getSimpleTypeName().toLowerCase()
								+ Character.toLowerCase(field.getFieldName().getSymbolName().charAt(0))
								+ field.getFieldName().getSymbolName().substring(1).concat("EditText");

						FieldMetadataBuilder geoField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
								new JavaSymbolName(fieldName), new JavaType("android.widget.EditText"), null);
						builder.addField(geoField);
					}

					// If is a referenced field, add ArrayList to include
					// results
					if (hasReferencedField) {
						fieldName = fieldType.getSimpleTypeName().toLowerCase().concat("List");

						FieldMetadataBuilder relatedField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
								new JavaSymbolName(fieldName),
								new JavaType("java.util.ArrayList", 0, DataType.TYPE, null, Arrays.asList(fieldType)),
								null);
						builder.addField(relatedField);
					}

				}
			}

		}

		FieldMetadataBuilder modeField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName("mode"),
				JavaType.STRING, null);
		builder.addField(modeField);

		// Check if GeoSearchHelper was included in current
		// class.
		if (hasGeoField) {
			FieldMetadataBuilder geoSearchHelper = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
					new JavaSymbolName("geoSearchHelper"),
					new JavaType(applicationPackage.getFullyQualifiedPackageName().concat(".utils.GeoSearchHelper")),
					null);

			builder.addField(geoSearchHelper);
		}

	}

	/**
	 * Method to check if provided field has @AndrooidReferencedField annotation
	 * 
	 * @param field
	 *            FieldMetadata with the field to check
	 * 
	 * @return true if is annotated with @AndrooidReferencedField
	 */
	private boolean isReferencedField(FieldMetadata field) {
		AnnotationMetadata annotation = field.getAnnotation(new JavaType(AndrooidReferencedField.class));
		return annotation != null;
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("identifier", getId());
		builder.append("valid", valid);
		builder.append("aspectName", aspectName);
		builder.append("destinationType", destination);
		builder.append("governor", governorPhysicalTypeMetadata.getId());
		builder.append("itdTypeDetails", itdTypeDetails);
		return builder.toString();
	}
}
