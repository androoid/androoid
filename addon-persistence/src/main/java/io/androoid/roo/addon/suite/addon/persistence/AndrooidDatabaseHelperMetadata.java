package io.androoid.roo.addon.suite.addon.persistence;

import io.androoid.roo.addon.suite.addon.persistence.annotations.AndrooidDatabaseHelper;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ConstructorMetadataBuilder;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.comments.CommentStructure;
import org.springframework.roo.classpath.details.comments.CommentStructure.CommentLocation;
import org.springframework.roo.classpath.details.comments.JavadocComment;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Metadata for {@link AndrooidDatabaseHelper} annotation.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public class AndrooidDatabaseHelperMetadata extends
		AbstractItdTypeDetailsProvidingMetadataItem {

	private static final String PROVIDES_TYPE_STRING = AndrooidDatabaseHelperMetadata.class
			.getName();
	private static final String PROVIDES_TYPE = MetadataIdentificationUtils
			.create(PROVIDES_TYPE_STRING);

	private final ImportRegistrationResolver importResolver;

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
	 * @param dbName
	 */
	public AndrooidDatabaseHelperMetadata(final String identifier,
			final JavaType aspectName,
			final PhysicalTypeMetadata governorPhysicalTypeMetadata,
			String dbName) {
		super(identifier, aspectName, governorPhysicalTypeMetadata);
		Validate.isTrue(
				isValid(identifier),
				"Metadata identification string '%s' does not appear to be a valid",
				identifier);

		this.importResolver = builder.getImportRegistrationResolver();

		// Adding constants
		FieldMetadataBuilder databaseName = new FieldMetadataBuilder(getId(),
				Modifier.PRIVATE + Modifier.STATIC + Modifier.FINAL,
				new JavaSymbolName("DATABASE_NAME"), JavaType.STRING,
				dbName.concat(".db"));
		FieldMetadataBuilder databaseVersion = new FieldMetadataBuilder(
				getId(), Modifier.PRIVATE + Modifier.STATIC + Modifier.FINAL,
				new JavaSymbolName("DATABASE_VERSION"), JavaType.INT_PRIMITIVE,
				"1");
		builder.addField(databaseName);
		builder.addField(databaseVersion);

		// TODO: Generate dynamic fields

		// Generating constructor
		ConstructorMetadataBuilder constructor = new ConstructorMetadataBuilder(
				getId());
		List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>();
		List<AnnotatedJavaType> paramTypes = new ArrayList<AnnotatedJavaType>();
		paramNames.add(new JavaSymbolName("context"));
		paramTypes.add(new AnnotatedJavaType(new JavaType(
				"android.content.Context")));
		constructor.setParameterNames(paramNames);
		constructor.setParameterTypes(paramTypes);
		constructor.setBodyBuilder(getConstructorBody());
		builder.addConstructor(constructor);

		// Generate necessary methods
		builder.addMethod(getOnCreateMethod());

		// Create a representation of the desired output ITD
		itdTypeDetails = builder.build();

	}

	/**
	 * Gets <code>onCreate</code> method. <br>
	 * 
	 * @return
	 */
	private MethodMetadataBuilder getOnCreateMethod() {
		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
		parameterTypes.add(new AnnotatedJavaType(new JavaType(
				"android.database.sqlite.SQLiteDatabase")));
		parameterTypes.add(new AnnotatedJavaType(new JavaType(
				"com.j256.ormlite.support.ConnectionSource")));

		// Define method annotations
		List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
		annotations
				.add(new AnnotationMetadataBuilder(new JavaType("Override")));

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
		parameterNames.add(new JavaSymbolName("database"));
		parameterNames.add(new JavaSymbolName("connectionSource"));

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		buildOnCreateMethodBody(bodyBuilder);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
				getId(), Modifier.PUBLIC, new JavaSymbolName("onCreate"),
				JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
				bodyBuilder);
		methodBuilder.setAnnotations(annotations);

		// Including comments
		CommentStructure commentStructure = new CommentStructure();
		JavadocComment comment = new JavadocComment(
				"What to do when your database needs to be created. Usually this entails creating the tables and loading any initial data. "
						+ "<b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one returned by getConnectionSource(). "
						+ "If you use your own, a recursive call or other unexpected results may result."
						+ "@param database         Database being created. "
						+ "@param connectionSource");
		commentStructure.addComment(comment, CommentLocation.BEGINNING);
		methodBuilder.setCommentStructure(commentStructure);

		return methodBuilder; // Build and return a MethodMetadata
		// instance
	}

	/**
	 * Builds body method for <code>onCreate</code> method. <br>
	 * 
	 * @param bodyBuilder
	 */
	private void buildOnCreateMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
		// try {
		bodyBuilder.appendFormalLine("try {");
		bodyBuilder.indent();

		// TODO: Generate dynamic tables
		bodyBuilder.indentRemove();

		// } catch (SQLException e) {
		bodyBuilder
				.appendFormalLine(String.format("} catch (%s e) {",
						new JavaType("java.sql.SQLException")
								.getNameIncludingTypeParameters(false,
										importResolver)));
		bodyBuilder.indent();

		// e.printStackTrace();
		bodyBuilder.appendFormalLine("e.printStackTrace();");
		bodyBuilder.indentRemove();

		// }
		bodyBuilder.appendFormalLine("}");

	}

	/**
	 * Generates constructor body of DatabaseHelper .aj
	 * 
	 * @return bodybuilder
	 */
	private InvocableMemberBodyBuilder getConstructorBody() {
		final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		bodyBuilder
				.appendFormalLine("super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);");

		return bodyBuilder;
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
