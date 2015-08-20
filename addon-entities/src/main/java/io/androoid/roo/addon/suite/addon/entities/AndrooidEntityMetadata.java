package io.androoid.roo.addon.suite.addon.entities;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

import io.androoid.roo.addon.suite.addon.entities.annotations.AndrooidEntity;

/**
 * Metadata for {@link AndrooidEntity}.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public class AndrooidEntityMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

	private static final String PROVIDES_TYPE_STRING = AndrooidEntityMetadata.class.getName();
	private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

	private FieldMetadata identifierField;
	private final MemberDetails entityMemberDetails;

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
	 */
	public AndrooidEntityMetadata(final String identifier, final JavaType aspectName,
			final PhysicalTypeMetadata governorPhysicalTypeMetadata, String physicalTypeIdentifier,
			MemberDetails entityMemberDetails, JavaSymbolName identifierFieldName, JavaType identifierType) {
		super(identifier, aspectName, governorPhysicalTypeMetadata);
		Validate.isTrue(isValid(identifier), "Metadata identification string '%s' does not appear to be a valid",
				identifier);

		this.entityMemberDetails = entityMemberDetails;

		// Add identifier field and accessor
		identifierField = getIdentifierField(identifierType, identifierFieldName);
		builder.addField(identifierField);
		builder.addMethod(getIdentifierAccessor());
		builder.addMethod(getIdentifierMutator());

		// Create a representation of the desired output ITD
		itdTypeDetails = builder.build();

	}

	/**
	 * Method that generates identifierField
	 * 
	 * @param identifierType
	 * @param fieldName
	 * @return
	 */
	private FieldMetadata getIdentifierField(JavaType identifierType, JavaSymbolName fieldName) {
		// Generate annotations array
		final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
		
		// Generating @DatabaseField(generatedId = true) annotation
		AnnotationMetadataBuilder databaseFieldAnnotation = new AnnotationMetadataBuilder(new JavaType("com.j256.ormlite.field.DatabaseField"));
		databaseFieldAnnotation.addBooleanAttribute("generatedId", true);
		
		annotations.add(databaseFieldAnnotation);
		
		FieldMetadata identifierField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE, annotations, fieldName,
				identifierType).build();
		return identifierField;
	}

	/**
	 * Locates the identifier mutator method.
	 * <p>
	 * If {@link #getIdentifierField()} returns a field created by this ITD or
	 * if the field is declared within the entity itself, a public mutator will
	 * automatically be produced in the declaring class.
	 * 
	 * @return the mutator (never returns null)
	 */
	private MethodMetadataBuilder getIdentifierMutator() {

		// Locate the identifier field, and compute the name of the accessor
		// that will be produced
		JavaSymbolName requiredMutatorName = BeanInfoUtils.getMutatorMethodName(identifierField);

		final List<JavaType> parameterTypes = Arrays.asList(identifierField.getFieldType());
		final List<JavaSymbolName> parameterNames = Arrays.asList(new JavaSymbolName("id"));

		// See if the user provided the field
		if (!getId().equals(identifierField.getDeclaredByMetadataId())) {
			// Locate an existing mutator
			final MethodMetadata method = entityMemberDetails.getMethod(requiredMutatorName, parameterTypes);
			if (method != null) {
				if (Modifier.isPublic(method.getModifier())) {
					// Method exists and is public so return it
					return new MethodMetadataBuilder(method);
				}

				// Method is not public so make the required mutator name unique
				requiredMutatorName = new JavaSymbolName(requiredMutatorName.getSymbolName() + "_");
			}
		}

		// We declared the field in this ITD, so produce a public mutator for it
		final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		bodyBuilder.appendFormalLine("this." + identifierField.getFieldName().getSymbolName() + " = id;");

		return new MethodMetadataBuilder(getId(), Modifier.PUBLIC, requiredMutatorName, JavaType.VOID_PRIMITIVE,
				AnnotatedJavaType.convertFromJavaTypes(parameterTypes), parameterNames, bodyBuilder);
	}

	/**
	 * Locates the identifier accessor method.
	 * <p>
	 * If {@link #getIdentifierField()} returns a field created by this ITD or
	 * if the field is declared within the entity itself, a public accessor will
	 * automatically be produced in the declaring class.
	 * 
	 * @return the accessor (never returns null)
	 */
	private MethodMetadataBuilder getIdentifierAccessor() {

		// Locate the identifier field, and compute the name of the accessor
		// that will be produced
		JavaSymbolName requiredAccessorName = BeanInfoUtils.getAccessorMethodName(identifierField);

		// See if the user provided the field
		if (!getId().equals(identifierField.getDeclaredByMetadataId())) {
			// Locate an existing accessor
			final MethodMetadata method = entityMemberDetails.getMethod(requiredAccessorName,
					new ArrayList<JavaType>());
			if (method != null) {
				if (Modifier.isPublic(method.getModifier())) {
					// Method exists and is public so return it
					return new MethodMetadataBuilder(method);
				}

				// Method is not public so make the required accessor name
				// unique
				requiredAccessorName = new JavaSymbolName(requiredAccessorName.getSymbolName() + "_");
			}
		}

		// We declared the field in this ITD, so produce a public accessor for
		// it
		final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		bodyBuilder.appendFormalLine("return this." + identifierField.getFieldName().getSymbolName() + ";");

		return new MethodMetadataBuilder(getId(), Modifier.PUBLIC, requiredAccessorName, identifierField.getFieldType(),
				bodyBuilder);
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
