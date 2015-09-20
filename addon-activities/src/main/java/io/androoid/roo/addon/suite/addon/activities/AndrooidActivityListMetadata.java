package io.androoid.roo.addon.suite.addon.activities;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidListActivity;

/**
 * Metadata for {@link AndrooidListActivity} annotation.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public class AndrooidActivityListMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

	private static final JavaType ARRAY_ADAPTER_JAVATYPE = new JavaType("android.widget.ArrayAdapter");
	private static final String PROVIDES_TYPE_STRING = AndrooidActivityListMetadata.class.getName();
	private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

	private final ImportRegistrationResolver importResolver;
	private final JavaType entity;
	private final JavaPackage applicationPackage;
	private final JavaType listEntityJavaType;
	private final JavaType arrayListEntityJavaType;

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
	 * 
	 */
	public AndrooidActivityListMetadata(final String identifier, final JavaType aspectName,
			final PhysicalTypeMetadata governorPhysicalTypeMetadata, JavaPackage projectPackage, JavaType entity) {
		super(identifier, aspectName, governorPhysicalTypeMetadata);
		Validate.isTrue(isValid(identifier), "Metadata identification string '%s' does not appear to be a valid",
				identifier);

		this.importResolver = builder.getImportRegistrationResolver();
		this.entity = entity;
		this.applicationPackage = projectPackage;
		this.listEntityJavaType = new JavaType("java.util.List", 0, DataType.TYPE, null, Arrays.asList(entity));
		this.arrayListEntityJavaType = new JavaType("java.util.ArrayList", 0, DataType.TYPE, null,
				Arrays.asList(entity));

		// Adding fields
		addListActivityFields();

		// Adding necessary methods
		builder.addMethod(getOnCreateMethod());
		builder.addMethod(getOnCreateOptionsMenuMethod());
		builder.addMethod(getOnOptionsItemSelectedMethod());

		// Create a representation of the desired output ITD
		itdTypeDetails = builder.build();

	}

	/**
	 * Method to add all necessary fields to ListActivity .aj file
	 */
	private void addListActivityFields() {
		FieldMetadataBuilder adapterField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
				new JavaSymbolName("adapter"), ARRAY_ADAPTER_JAVATYPE, null);
		builder.addField(adapterField);

		FieldMetadataBuilder selectedEntitiesField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
				new JavaSymbolName("selected".concat(entity.getSimpleTypeName())), listEntityJavaType, null);
		builder.addField(selectedEntitiesField);

		FieldMetadataBuilder entityListField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
				new JavaSymbolName(entity.getSimpleTypeName().toLowerCase().concat("List")), arrayListEntityJavaType,
				null);
		builder.addField(entityListField);

		FieldMetadataBuilder contextualMenuField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
				new JavaSymbolName("contextualMenu"), new JavaType("android.view.Menu"), null);
		builder.addField(contextualMenuField);

		FieldMetadataBuilder actionModeField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
				new JavaSymbolName("actionMode"), new JavaType("android.view.ActionMode"), null);
		builder.addField(actionModeField);
	}

	/**
	 * Method that generates onCreate ListActivity method
	 * 
	 * @return
	 */
	private MethodMetadataBuilder getOnCreateMethod() {
		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.os.Bundle")));

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
		parameterNames.add(new JavaSymbolName("savedInstanceState"));

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

		buildOnCreateMethodBody(bodyBuilder);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onCreate"), JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);
		methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

		return methodBuilder; // Build and return a MethodMetadata
		// instance
	}

	/**
	 * Generates onCreate ListActivity method body
	 * 
	 * @param bodyBuilder
	 */
	private void buildOnCreateMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
		// super.onCreate(savedInstanceState);
		bodyBuilder.appendFormalLine("super.onCreate(savedInstanceState);");

		// setContentView(R.layout.entity_list_activity);
		bodyBuilder.appendFormalLine(String.format("setContentView(%s.layout.main_activity);",
				new JavaType(applicationPackage.getFullyQualifiedPackageName().concat(".R"))
						.getNameIncludingTypeParameters(false, importResolver),
				entity.getSimpleTypeName().toLowerCase()));

		// Adding back button
		bodyBuilder.appendFormalLine("// Adding back button");

		// getActionBar().setDisplayHomeAsUpEnabled(true);
		bodyBuilder.appendFormalLine("getActionBar().setDisplayHomeAsUpEnabled(true);");

		// Adding selector
		bodyBuilder.appendFormalLine("// Adding selector");

		// this.getListView().setSelector(R.drawable.selector);
		bodyBuilder.appendFormalLine("this.getListView().setSelector(R.drawable.selector);");

		// Creating short click listener
		bodyBuilder.appendFormalLine("// Creating short click listener");

		// getListView().setOnItemClickListener(this);
		bodyBuilder.appendFormalLine("getListView().setOnItemClickListener(this);");

		// Creating multiple choice view
		bodyBuilder.appendFormalLine("// Creating multiple choice view");

		// getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		bodyBuilder.appendFormalLine(String.format("getListView().setChoiceMode(%s.CHOICE_MODE_MULTIPLE_MODAL);",
				new JavaType("android.widget.ListView").getNameIncludingTypeParameters(false, importResolver)));

		// getListView().setMultiChoiceModeListener(this);
		bodyBuilder.appendFormalLine("getListView().setMultiChoiceModeListener(this);");

		// Fill entity list with Entity from Database
		bodyBuilder.appendFormalLine(String.format("// Fill %s list with %s from Database",
				entity.getSimpleTypeName().toLowerCase(), entity.getSimpleTypeName()));

		// try {
		bodyBuilder.appendFormalLine("try {");
		bodyBuilder.indent();

		// fillEntityList();
		bodyBuilder.appendFormalLine(String.format("fill%sList();", entity.getSimpleTypeName()));
		bodyBuilder.indentRemove();

		// } catch (SQLException e) {
		bodyBuilder.appendFormalLine(String.format("} catch (%s e) {",
				new JavaType("java.sql.SQLException").getNameIncludingTypeParameters(false, importResolver)));
		bodyBuilder.indent();

		// e.printStackTrace();
		bodyBuilder.appendFormalLine("e.printStackTrace();");
		bodyBuilder.indentRemove();
		bodyBuilder.appendFormalLine("}");
	}

	/**
	 * Method that generates onCreateOptionsMenu ListActivity method
	 * 
	 * @return
	 */
	private MethodMetadataBuilder getOnCreateOptionsMenuMethod() {
		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.Menu")));

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
		parameterNames.add(new JavaSymbolName("menu"));

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

		buildOnCreateOptionsMenuMethodBody(bodyBuilder);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onCreateOptionsMenu"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
				bodyBuilder);
		methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

		return methodBuilder; // Build and return a MethodMetadata
		// instance
	}

	/**
	 * Generates onCreateOptionsMenu ListActivity method body
	 * 
	 * @param bodyBuilder
	 */
	private void buildOnCreateOptionsMenuMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
		// Inflate the menu; this adds items to the action bar if it is present.
		bodyBuilder.appendFormalLine("// Inflate the menu; this adds items to the action bar if it is present.");

		// getMenuInflater().inflate(R.menu.menu_list, menu);
		bodyBuilder.appendFormalLine("getMenuInflater().inflate(R.menu.menu_list, menu);");

		// return true;
		bodyBuilder.appendFormalLine("return true;");
	}

	/**
	 * Method that generates onOptionsItemSelected ListActivity method
	 * 
	 * @return
	 */
	private MethodMetadataBuilder getOnOptionsItemSelectedMethod() {
		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.MenuItem")));

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
		parameterNames.add(new JavaSymbolName("item"));

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

		buildOnOptionsItemSelectedMethodBody(bodyBuilder);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onOptionsItemSelected"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
				bodyBuilder);
		methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

		return methodBuilder; // Build and return a MethodMetadata
		// instance
	}

	/**
	 * Generates onOptionsItemSelected ListActivity method body
	 * 
	 * @param bodyBuilder
	 */
	private void buildOnOptionsItemSelectedMethodBody(InvocableMemberBodyBuilder bodyBuilder) {

		// switch (item.getItemId()) {
		bodyBuilder.appendFormalLine("switch (item.getItemId()) {");
		bodyBuilder.indent();

		// Respond to the action bar's Up/Home button
		bodyBuilder.appendFormalLine("// Respond to the action bar's Up/Home button");

		// case android.R.id.home:
		bodyBuilder.appendFormalLine("case android.R.id.home:");
		bodyBuilder.indent();

		// NavUtils.navigateUpFromSameTask(this);
		bodyBuilder.appendFormalLine(String.format("%s.navigateUpFromSameTask(this);",
				new JavaType("android.support.v4.app.NavUtils").getNameIncludingTypeParameters(false, importResolver)));

		// return true;
		bodyBuilder.appendFormalLine("return true;");
		bodyBuilder.indentRemove();

		// case R.id.action_add:
		bodyBuilder.appendFormalLine("case R.id.action_add:");
		bodyBuilder.indent();

		// Intent intent = new Intent(EntityListActivity.this,
		// EntityFormActivity.class);
		bodyBuilder.appendFormalLine(String.format("%s intent = new Intent(%sListActivity.this, %sFormActivity.class);",
				new JavaType("android.content.Intent").getNameIncludingTypeParameters(false, importResolver),
				entity.getSimpleTypeName(), entity.getSimpleTypeName()));

		// EntityListActivity.this.startActivity(intent);
		bodyBuilder.appendFormalLine(
				String.format("%sListActivity.this.startActivity(intent);", entity.getSimpleTypeName()));

		// return true;
		bodyBuilder.appendFormalLine("return true;");
		bodyBuilder.indentRemove();
		bodyBuilder.indentRemove();
		bodyBuilder.appendFormalLine("}");

		// return super.onOptionsItemSelected(item);
		bodyBuilder.appendFormalLine("return super.onOptionsItemSelected(item);");

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
