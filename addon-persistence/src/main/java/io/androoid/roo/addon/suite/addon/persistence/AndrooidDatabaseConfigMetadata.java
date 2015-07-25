package io.androoid.roo.addon.suite.addon.persistence;

import io.androoid.roo.addon.suite.addon.persistence.annotations.AndrooidDatabaseConfig;

import java.lang.reflect.Modifier;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Metadata for {@link AndrooidDatabaseConfig}.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public class AndrooidDatabaseConfigMetadata extends
		AbstractItdTypeDetailsProvidingMetadataItem {

	private static final String PROVIDES_TYPE_STRING = AndrooidDatabaseConfigMetadata.class
			.getName();
	private static final String PROVIDES_TYPE = MetadataIdentificationUtils
			.create(PROVIDES_TYPE_STRING);

	public static String createIdentifier(final JavaType javaType,
			final LogicalPath path) {
		return PhysicalTypeIdentifierNamingUtils.createIdentifier(
				PROVIDES_TYPE_STRING, javaType, path);
	}

	public static JavaType getJavaType(final String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.getJavaType(
				PROVIDES_TYPE_STRING, metadataIdentificationString);
	}

	public static String getMetadataIdentiferType() {
		return PROVIDES_TYPE;
	}

	public static LogicalPath getPath(final String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
				metadataIdentificationString);
	}

	public static boolean isValid(final String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
				metadataIdentificationString);
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
	public AndrooidDatabaseConfigMetadata(final String identifier,
			final JavaType aspectName,
			final PhysicalTypeMetadata governorPhysicalTypeMetadata) {
		super(identifier, aspectName, governorPhysicalTypeMetadata);
		Validate.isTrue(
				isValid(identifier),
				"Metadata identification string '%s' does not appear to be a valid",
				identifier);
		// Generating main method
		final MethodMetadataBuilder mainMethod = getMainMethod();
		builder.addMethod(mainMethod);

		// Create a representation of the desired output ITD
		itdTypeDetails = builder.build();

	}

	/**
	 * Generates main method for DatabaseConfigUtils .aj
	 * 
	 * @return the main method
	 */
	private MethodMetadataBuilder getMainMethod() {

		// Compute the mutator method name
		final JavaSymbolName methodName = new JavaSymbolName("main");

		// See if the type itself declared the main method
		if (governorHasMethod(methodName)) {
			return null;
		}

		final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		bodyBuilder
				.appendFormalLine("writeConfigFile(\"ormlite_config.txt\");");

		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
				getId(), Modifier.PUBLIC + Modifier.STATIC, methodName,
				JavaType.VOID_PRIMITIVE, bodyBuilder);

		// Adding throws
		methodBuilder.addThrowsType(new JavaType("java.io.IOException"));
		methodBuilder.addThrowsType(new JavaType("java.sql.SQLException"));

		// Adding params
		methodBuilder.addParameter("args", JavaType.STRING_ARRAY);

		return methodBuilder;

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
