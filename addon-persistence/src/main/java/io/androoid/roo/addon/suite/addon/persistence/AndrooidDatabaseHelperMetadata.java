package io.androoid.roo.addon.suite.addon.persistence;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
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
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

import io.androoid.roo.addon.suite.addon.persistence.annotations.AndrooidDatabaseHelper;

/**
 * Metadata for {@link AndrooidDatabaseHelper} annotation.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public class AndrooidDatabaseHelperMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

	private static final String PROVIDES_TYPE_STRING = AndrooidDatabaseHelperMetadata.class.getName();
	private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

	private final ImportRegistrationResolver importResolver;
	private final List<JavaType> entitiesToInclude;

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
	 * 
	 */
	public AndrooidDatabaseHelperMetadata(final String identifier, final JavaType aspectName,
			final PhysicalTypeMetadata governorPhysicalTypeMetadata, JavaPackage projectPackage,
			List<JavaType> entitiesToInclude) {
		super(identifier, aspectName, governorPhysicalTypeMetadata);
		Validate.isTrue(isValid(identifier), "Metadata identification string '%s' does not appear to be a valid",
				identifier);

		this.importResolver = builder.getImportRegistrationResolver();
		this.entitiesToInclude = entitiesToInclude;

		// Adding constants
		FieldMetadataBuilder databaseName = new FieldMetadataBuilder(getId(),
				Modifier.PUBLIC + Modifier.STATIC + Modifier.FINAL, new JavaSymbolName("DATABASE_NAME"),
				JavaType.STRING, "\"".concat(projectPackage.getLastElement()).concat(".db").concat("\""));
		FieldMetadataBuilder databaseVersion = new FieldMetadataBuilder(getId(),
				Modifier.PUBLIC + Modifier.STATIC + Modifier.FINAL, new JavaSymbolName("DATABASE_VERSION"),
				JavaType.INT_PRIMITIVE, "1");
		builder.addField(databaseName);
		builder.addField(databaseVersion);

		// Generate DAOs using annotation entities
		for (JavaType entity : entitiesToInclude) {
			builder.addField(getEntityDao(entity));
			builder.addField(getEntityRuntimeExceptionDao(entity));

			// Generating getters
			builder.addMethod(getEntityDaoGetter(entity));
			builder.addMethod(getEntityRuntimeExceptionDaoGetter(entity));
		}

		// Generate necessary methods
		builder.addMethod(getOnCreateMethod());
		builder.addMethod(getOnUpgradeMethod());
		builder.addMethod(getCloseMethod());

		// Create a representation of the desired output ITD
		itdTypeDetails = builder.build();

	}

	/**
	 * Method that generates entity DAO getter using received entity JavaType
	 * 
	 * @param entity
	 * @return
	 */
	private MethodMetadataBuilder getEntityDaoGetter(JavaType entity) {
		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

		final List<JavaType> parameters = new ArrayList<JavaType>();

		parameters.add(entity);
		parameters.add(JavaType.INT_OBJECT);
		JavaType returnType = new JavaType("com.j256.ormlite.dao.Dao", 0, DataType.TYPE, null, parameters);

		String daoName = entity.getSimpleTypeName().concat("Dao");
		String fieldDaoName = Character.toLowerCase(entity.getSimpleTypeName().charAt(0))
				+ entity.getSimpleTypeName().substring(1).concat("Dao");

		buildEntityDaoGetterMethodBody(bodyBuilder, fieldDaoName, entity);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("get".concat(daoName)), returnType, parameterTypes, parameterNames, bodyBuilder);
		methodBuilder.addThrowsType(new JavaType("java.sql.SQLException"));

		return methodBuilder; // Build and return a MethodMetadata
		// instance
	}

	/**
	 * Generates entity DAO getter body
	 * 
	 * @param bodyBuilder
	 */
	private void buildEntityDaoGetterMethodBody(InvocableMemberBodyBuilder bodyBuilder, String daoName,
			JavaType entity) {

		// if(daoName == null){
		bodyBuilder.appendFormalLine(String.format("if(%s == null){", daoName));
		bodyBuilder.indent();

		// daoName = getDao(EntityX.class);
		bodyBuilder.appendFormalLine(String.format("%s = getDao(%s.class);", daoName, entity.getSimpleTypeName()));
		bodyBuilder.indentRemove();

		// }
		bodyBuilder.appendFormalLine("}");

		// return daoName;
		bodyBuilder.appendFormalLine(String.format("return %s;", daoName));

	}

	/**
	 * Method that generates entity RuntimeExceptionDAO getter using received
	 * entity JavaType
	 * 
	 * @param entity
	 * @return
	 */
	private MethodMetadataBuilder getEntityRuntimeExceptionDaoGetter(JavaType entity) {
		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

		final List<JavaType> parameters = new ArrayList<JavaType>();

		parameters.add(entity);
		parameters.add(JavaType.INT_OBJECT);
		JavaType returnType = new JavaType("com.j256.ormlite.dao.RuntimeExceptionDao", 0, DataType.TYPE, null,
				parameters);

		String daoName = "runtimeException".concat(entity.getSimpleTypeName()).concat("Dao");

		buildEntityRuntimeExceptionDaoGetterMethodBody(bodyBuilder, daoName, entity);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("get".concat(daoName)), returnType, parameterTypes, parameterNames, bodyBuilder);
		methodBuilder.addThrowsType(new JavaType("java.sql.SQLException"));

		return methodBuilder; // Build and return a MethodMetadata
		// instance
	}

	/**
	 * Generates entity RuntimeExceptionDAO getter body
	 * 
	 * @param bodyBuilder
	 */
	private void buildEntityRuntimeExceptionDaoGetterMethodBody(InvocableMemberBodyBuilder bodyBuilder, String daoName,
			JavaType entity) {

		// if(daoName == null){
		bodyBuilder.appendFormalLine(String.format("if(%s == null){", daoName));
		bodyBuilder.indent();

		// daoName = getRuntimeExceptionDao(EntityX.class);
		bodyBuilder.appendFormalLine(
				String.format("%s = getRuntimeExceptionDao(%s.class);", daoName, entity.getSimpleTypeName()));
		bodyBuilder.indentRemove();

		// }
		bodyBuilder.appendFormalLine("}");

		// return daoName;
		bodyBuilder.appendFormalLine(String.format("return %s;", daoName));

	}

	/**
	 * 
	 * Method that generates entity DAO using received entity Javatype
	 * 
	 * @param entity
	 * @return
	 */
	private FieldMetadataBuilder getEntityDao(JavaType entity) {

		final List<JavaType> parameters = new ArrayList<JavaType>();

		parameters.add(entity);
		parameters.add(JavaType.INT_OBJECT);

		JavaSymbolName daoName = new JavaSymbolName(Character.toLowerCase(entity.getSimpleTypeName().charAt(0))
				+ entity.getSimpleTypeName().substring(1).concat("Dao"));

		FieldMetadataBuilder entityDao = new FieldMetadataBuilder(getId(), Modifier.PRIVATE, daoName,
				new JavaType("com.j256.ormlite.dao.Dao", 0, DataType.TYPE, null, parameters), null);

		return entityDao;
	}

	/**
	 * 
	 * Method that generates entity RuntimeExceptionDAO using received entity
	 * Javatype
	 * 
	 * @param entity
	 * @return
	 */
	private FieldMetadataBuilder getEntityRuntimeExceptionDao(JavaType entity) {

		final List<JavaType> parameters = new ArrayList<JavaType>();

		parameters.add(entity);
		parameters.add(JavaType.INT_OBJECT);

		JavaSymbolName daoName = new JavaSymbolName(
				"runtimeException".concat(entity.getSimpleTypeName()).concat("Dao"));

		FieldMetadataBuilder entityDao = new FieldMetadataBuilder(getId(), Modifier.PRIVATE, daoName,
				new JavaType("com.j256.ormlite.dao.RuntimeExceptionDao", 0, DataType.TYPE, null, parameters), null);

		return entityDao;
	}

	/**
	 * Gets <code>close</code> method. <br>
	 * 
	 * @return
	 */
	private MethodMetadataBuilder getCloseMethod() {
		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

		// Define method annotations
		List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
		annotations.add(new AnnotationMetadataBuilder(new JavaType("Override")));

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		buildCloseMethodBody(bodyBuilder);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("close"), JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);
		methodBuilder.setAnnotations(annotations);

		return methodBuilder; // Build and return a MethodMetadata
		// instance
	}

	/**
	 * Gets <code>onCreate</code> method. <br>
	 * 
	 * @return
	 */
	private MethodMetadataBuilder getOnCreateMethod() {
		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
		parameterTypes.add(new AnnotatedJavaType(new JavaType("android.database.sqlite.SQLiteDatabase")));
		parameterTypes.add(new AnnotatedJavaType(new JavaType("com.j256.ormlite.support.ConnectionSource")));

		// Define method annotations
		List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
		annotations.add(new AnnotationMetadataBuilder(new JavaType("Override")));

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
		parameterNames.add(new JavaSymbolName("database"));
		parameterNames.add(new JavaSymbolName("connectionSource"));

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		buildOnCreateMethodBody(bodyBuilder);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onCreate"), JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);
		methodBuilder.setAnnotations(annotations);

		// Including comments
		CommentStructure commentStructure = new CommentStructure();
		JavadocComment comment = new JavadocComment(
				"What to do when your database needs to be created. Usually this entails creating the tables and loading any \n initial data. \n \n \n "
						+ "<b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one \n returned by getConnectionSource(). "
						+ "If you use your own, a recursive call or other unexpected results may result. \n \n"
						+ "@param database         Database being created. \n" + "@param connectionSource \n");
		commentStructure.addComment(comment, CommentLocation.BEGINNING);
		methodBuilder.setCommentStructure(commentStructure);

		return methodBuilder; // Build and return a MethodMetadata
		// instance
	}

	/**
	 * Gets <code>onUpgrade</code> method. <br>
	 * 
	 * @return
	 */
	private MethodMetadataBuilder getOnUpgradeMethod() {
		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
		parameterTypes.add(new AnnotatedJavaType(new JavaType("android.database.sqlite.SQLiteDatabase")));
		parameterTypes.add(new AnnotatedJavaType(new JavaType("com.j256.ormlite.support.ConnectionSource")));
		parameterTypes.add(new AnnotatedJavaType(JavaType.INT_PRIMITIVE));
		parameterTypes.add(new AnnotatedJavaType(JavaType.INT_PRIMITIVE));

		// Define method annotations
		List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
		annotations.add(new AnnotationMetadataBuilder(new JavaType("Override")));

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
		parameterNames.add(new JavaSymbolName("database"));
		parameterNames.add(new JavaSymbolName("connectionSource"));
		parameterNames.add(new JavaSymbolName("oldVersion"));
		parameterNames.add(new JavaSymbolName("newVersion"));

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		buildOnUpgradeMethodBody(bodyBuilder);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onUpgrade"), JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);
		methodBuilder.setAnnotations(annotations);

		// Including comments
		CommentStructure commentStructure = new CommentStructure();
		JavadocComment comment = new JavadocComment(
				"What to do when your database needs to be updated. This could mean careful migration of old data to new data.\n"
						+ "Maybe adding or deleting database columns, etc.. \n" + "\n" + "\n"
						+ "<b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one \n"
						+ "returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.\n"
						+ "\n" + "\n" + "@param database         Database being upgraded.\n"
						+ "@param connectionSource To use get connections to the database to be updated.\n"
						+ "@param oldVersion       The version of the current database so we can know what to do to the database.\n"
						+ "@param newVersion\n");
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
		// Check if is necessary to generate method body
		if (!this.entitiesToInclude.isEmpty()) {
			// Generate method body

			// try {
			bodyBuilder.appendFormalLine("try {");
			bodyBuilder.indent();

			// Generating dynamic tables
			for (JavaType entity : this.entitiesToInclude) {

				// TableUtils.createTable(connectionSource, EntityX.class);
				bodyBuilder.appendFormalLine(String.format(
						"%s.createTable(connectionSource, %s.class);", new JavaType("com.j256.ormlite.table.TableUtils")
								.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()),
						entity.getSimpleTypeName()));
			}

			// } catch (SQLException e) {
			bodyBuilder.indentRemove();
			bodyBuilder.appendFormalLine(String.format("} catch (%s e) {", new JavaType("java.sql.SQLException")
					.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver())));
			bodyBuilder.indent();

			// e.printStackTrace();
			bodyBuilder.appendFormalLine("e.printStackTrace();");
			bodyBuilder.indentRemove();

			// }
			bodyBuilder.appendFormalLine("}");
		}
	}

	/**
	 * Builds body method for <code>onUpgrade</code> method. <br>
	 * 
	 * @param bodyBuilder
	 */
	private void buildOnUpgradeMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
		// Check if is necessary to generate method body
		if (!this.entitiesToInclude.isEmpty()) {
			// Generate method body

			// try {
			bodyBuilder.appendFormalLine("try {");
			bodyBuilder.indent();

			// Generating dynamic tables
			for (JavaType entity : this.entitiesToInclude) {

				// TableUtils.createTable(connectionSource, EntityX.class);
				bodyBuilder.appendFormalLine(String.format("%s.dropTable(connectionSource, %s.class, true);",
						new JavaType("com.j256.ormlite.table.TableUtils").getNameIncludingTypeParameters(false,
								builder.getImportRegistrationResolver()),
						entity.getSimpleTypeName()));
			}

			// onCreate(database, connectionSource);
			bodyBuilder.appendFormalLine("onCreate(database, connectionSource);");

			// } catch (SQLException e) {
			bodyBuilder.indentRemove();
			bodyBuilder.appendFormalLine(String.format("} catch (%s e) {", new JavaType("java.sql.SQLException")
					.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver())));
			bodyBuilder.indent();

			// e.printStackTrace();
			bodyBuilder.appendFormalLine("e.printStackTrace();");
			bodyBuilder.indentRemove();

			// }
			bodyBuilder.appendFormalLine("}");
		}
	}

	/**
	 * Builds body method for <code>close</code> method. <br>
	 * 
	 * @param bodyBuilder
	 */
	private void buildCloseMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
		// super.close();
		bodyBuilder.appendFormalLine("super.close();");

		// Check if is necessary to continue with method body generation
		if (!this.entitiesToInclude.isEmpty()) {

			for (JavaType entity : this.entitiesToInclude) {

				// Including entityDao
				JavaSymbolName daoName = new JavaSymbolName(Character.toLowerCase(entity.getSimpleTypeName().charAt(0))
						+ entity.getSimpleTypeName().substring(1).concat("Dao"));
				bodyBuilder.appendFormalLine(String.format("%s = null;", daoName));

				// Including runtimeExceptionDaoName
				JavaSymbolName runtimeExceptionDaoName = new JavaSymbolName(
						"runtimeException".concat(entity.getSimpleTypeName()).concat("Dao"));
				bodyBuilder.appendFormalLine(String.format("%s = null;", runtimeExceptionDaoName));
			}

		}

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
