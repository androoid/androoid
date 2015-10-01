package io.androoid.roo.addon.suite.addon.activities;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ConstructorMetadata;
import org.springframework.roo.classpath.details.ConstructorMetadataBuilder;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidActionBarCallback;

/**
 * Metadata for {@link AndrooidActionBarCallback} annotation.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public class AndrooidActionBarCallbackMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

	private static final String PROVIDES_TYPE_STRING = AndrooidActionBarCallbackMetadata.class.getName();
	private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

	private final ImportRegistrationResolver importResolver;

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
	 */
	public AndrooidActionBarCallbackMetadata(final String identifier, final JavaType aspectName,
			final PhysicalTypeMetadata governorPhysicalTypeMetadata) {
		super(identifier, aspectName, governorPhysicalTypeMetadata);
		Validate.isTrue(isValid(identifier), "Metadata identification string '%s' does not appear to be a valid",
				identifier);

		this.importResolver = builder.getImportRegistrationResolver();

		// Including contextualMenu field
		FieldMetadataBuilder contextualMenuField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
				new JavaSymbolName("contextualMenu"), JavaType.INT_PRIMITIVE, null);
		builder.addField(contextualMenuField);

		// Including constructor
		builder.addConstructor(getConstructor());

		// Including necessary methods
		builder.addMethod(getOnCreateAndrooidActionModeMethod());
		builder.addMethod(getOnPrepareAndrooidActionModeMethod());
		builder.addMethod(getOnAndrooidActionItemClickedMethod());
		builder.addMethod(getOnAndrooidDestroyActionModeMethod());

		// Create a representation of the desired output ITD
		itdTypeDetails = builder.build();

	}

	/**
	 * Method that generates onCreateActionMode method
	 * 
	 * @return MethodMetadata that will be added to builder
	 */
	private MethodMetadata getOnCreateAndrooidActionModeMethod() {

		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.ActionMode")));
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.Menu")));

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
		parameterNames.add(new JavaSymbolName("mode"));
		parameterNames.add(new JavaSymbolName("menu"));

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

		buildOnCreateActionModeMethodBody(bodyBuilder);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onCreateAndrooidActionMode"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes,
				parameterNames, bodyBuilder);

		return methodBuilder.build(); // Build and return a MethodMetadata
		// instance

	}

	/**
	 * Method that generates onCreateAndrooidActionMode method body
	 * 
	 * @param bodyBuilder
	 */
	private void buildOnCreateActionModeMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
		// Inflate a menu resource providing context menu items
		bodyBuilder.appendFormalLine("// Inflate a menu resource providing context menu items");

		// MenuInflater inflater = mode.getMenuInflater();
		bodyBuilder.appendFormalLine(String.format("%s inflater = mode.getMenuInflater();",
				new JavaType("android.view.MenuInflater").getNameIncludingTypeParameters(false, importResolver)));

		// inflater.inflate(contextualMenu, menu);
		bodyBuilder.appendFormalLine("inflater.inflate(contextualMenu, menu);");

		// return true;
		bodyBuilder.appendFormalLine("return true;");

	}

	/**
	 * Method that generates onPrepareAndrooidActionMode method
	 * 
	 * @return MethodMetadata that will be added to builder
	 */
	private MethodMetadata getOnPrepareAndrooidActionModeMethod() {

		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.ActionMode")));
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.Menu")));

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
		parameterNames.add(new JavaSymbolName("mode"));
		parameterNames.add(new JavaSymbolName("menu"));

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		bodyBuilder.appendFormalLine("return false;");

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onPrepareAndrooidActionMode"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes,
				parameterNames, bodyBuilder);

		return methodBuilder.build(); // Build and return a MethodMetadata
		// instance

	}

	/**
	 * Method that generates onAndrooidActionItemClicked method
	 * 
	 * @return MethodMetadata that will be added to builder
	 */
	private MethodMetadata getOnAndrooidActionItemClickedMethod() {

		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.ActionMode")));
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.MenuItem")));

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
		parameterNames.add(new JavaSymbolName("mode"));
		parameterNames.add(new JavaSymbolName("item"));

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		bodyBuilder.appendFormalLine("return false;");

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onAndrooidActionItemClicked"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes,
				parameterNames, bodyBuilder);

		return methodBuilder.build(); // Build and return a MethodMetadata
		// instance

	}

	/**
	 * Method that generates onAndrooidDestroyActionMode method
	 * 
	 * @return MethodMetadata that will be added to builder
	 */
	private MethodMetadata getOnAndrooidDestroyActionModeMethod() {

		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.ActionMode")));

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
		parameterNames.add(new JavaSymbolName("mode"));

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onAndrooidDestroyActionMode"), JavaType.VOID_PRIMITIVE, parameterTypes,
				parameterNames, null);

		return methodBuilder.build(); // Build and return a MethodMetadata
		// instance

	}

	/**
	 * Method that generates ActionBarCallback class constructor
	 * 
	 * @return ConstructorMetadata that will be added to builder
	 */
	private ConstructorMetadata getConstructor() {

		// Generating constructor builder
		ConstructorMetadataBuilder constructorBuilder = new ConstructorMetadataBuilder(getId());

		// Append menu param
		constructorBuilder.addParameter("menu", JavaType.INT_PRIMITIVE);

		// Generating body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		bodyBuilder.appendFormalLine("this.contextualMenu = menu;");
		constructorBuilder.setBodyBuilder(bodyBuilder);

		return constructorBuilder.build();
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
