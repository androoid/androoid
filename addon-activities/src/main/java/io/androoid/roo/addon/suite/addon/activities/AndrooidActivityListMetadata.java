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
		builder.addMethod(getOnItemCheckedStateChangedMethod());
		builder.addMethod(getOnCreateActionModeMethod());
		builder.addMethod(getOnPrepareActionModeMethod());
		builder.addMethod(getOnActionItemClickedMethod());

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

	/**
	 * Method that generates onItemCheckedStateChanged ListActivity method
	 * 
	 * @return
	 */
	private MethodMetadataBuilder getOnItemCheckedStateChangedMethod() {
		// Define method parameter types
		List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.ActionMode")));
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(JavaType.INT_PRIMITIVE));
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(JavaType.LONG_PRIMITIVE));
		parameterTypes.add(AnnotatedJavaType.convertFromJavaType(JavaType.BOOLEAN_PRIMITIVE));

		// Define method parameter names
		List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
		parameterNames.add(new JavaSymbolName("mode"));
		parameterNames.add(new JavaSymbolName("position"));
		parameterNames.add(new JavaSymbolName("id"));
		parameterNames.add(new JavaSymbolName("checked"));

		// Create the method body
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

		buildOnItemCheckedStateChangedMethodBody(bodyBuilder);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onItemCheckedStateChanged"), JavaType.VOID_PRIMITIVE, parameterTypes,
				parameterNames, bodyBuilder);
		methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

		// Including comments
		CommentStructure commentStructure = new CommentStructure();
		JavadocComment comment = new JavadocComment(
				"Called when an item is checked or unchecked during selection mode. \n \n"
						+ "@param mode     The {@link android.view.ActionMode} providing the selection mode. \n"
						+ "@param position Adapter position of the item that was checked or unchecked. \n"
						+ "@param id       Adapter ID of the item that was checked or unchecked \n"
						+ "@param checked  <code>true</code> if the item is now checked, <code>false</code> \n");
		commentStructure.addComment(comment, CommentLocation.BEGINNING);
		methodBuilder.setCommentStructure(commentStructure);

		return methodBuilder; // Build and return a MethodMetadata
		// instance
	}

	/**
	 * Generates onItemCheckedStateChanged ListActivity method body
	 * 
	 * @param bodyBuilder
	 */
	private void buildOnItemCheckedStateChangedMethodBody(InvocableMemberBodyBuilder bodyBuilder) {

		// Getting current item
		bodyBuilder.appendFormalLine("// Getting current item");

		// Entity entity = (Entity) getListView().getItemAtPosition(position);
		bodyBuilder.appendFormalLine(String.format("%s %s = (%s) getListView().getItemAtPosition(position);",
				entity.getSimpleTypeName(), entity.getSimpleTypeName().toLowerCase(), entity.getSimpleTypeName()));

		// View child = getListView().getChildAt(position);
		bodyBuilder.appendFormalLine(String.format("%s child = getListView().getChildAt(position);",
				new JavaType("android.view.View").getNameIncludingTypeParameters(false, importResolver)));

		// Checking if current item was checked before
		bodyBuilder.appendFormalLine("// Checking if current item was checked before");

		// if (selectedEntity.indexOf(entity) != -1) {
		bodyBuilder.appendFormalLine(String.format("if (selected%s.indexOf(%s) != -1) {", entity.getSimpleTypeName(),
				entity.getSimpleTypeName().toLowerCase()));
		bodyBuilder.indent();

		// Removing element from selected Entity
		bodyBuilder.appendFormalLine(String.format("// Removing element from selected %s", entity.getSimpleTypeName()));

		// selectedEntity.remove(selectedEntity.indexOf(entity));
		bodyBuilder.appendFormalLine(String.format("selected%s.remove(selected%s.indexOf(%s));",
				entity.getSimpleTypeName(), entity.getSimpleTypeName(), entity.getSimpleTypeName().toLowerCase()));

		// Removing background
		bodyBuilder.appendFormalLine("// Removing background");

		// if (child != null) {
		bodyBuilder.appendFormalLine("if (child != null) {");
		bodyBuilder.indent();

		// child.setSelected(false);
		bodyBuilder.appendFormalLine("child.setSelected(false);");

		// child.setBackgroundColor(Color.WHITE);
		bodyBuilder.appendFormalLine(String.format("child.setBackgroundColor(%s.WHITE);",
				new JavaType("android.graphics.Color").getNameIncludingTypeParameters(false, importResolver)));
		bodyBuilder.indentRemove();
		bodyBuilder.appendFormalLine("}");
		bodyBuilder.indentRemove();

		// } else {
		bodyBuilder.appendFormalLine("} else {");
		bodyBuilder.indent();

		// Adding element to selected Entity
		bodyBuilder.appendFormalLine(String.format("// Adding element to selected %s", entity.getSimpleTypeName()));

		// selectedEntity.add(entity);
		bodyBuilder.appendFormalLine(String.format("selected%s.add(%s);", entity.getSimpleTypeName(),
				entity.getSimpleTypeName().toLowerCase()));

		// Changing background
		bodyBuilder.appendFormalLine("// Changing background");

		// if (child != null) {
		bodyBuilder.appendFormalLine("if (child != null) {");
		bodyBuilder.indent();

		// child.setSelected(true);
		bodyBuilder.appendFormalLine("child.setSelected(true);");

		// child.setBackgroundColor(Color.parseColor("#6DCAEC"));
		bodyBuilder.appendFormalLine("child.setBackgroundColor(Color.parseColor(\"#6DCAEC\"));");

		bodyBuilder.indentRemove();
		bodyBuilder.appendFormalLine("}");
		bodyBuilder.indentRemove();
		bodyBuilder.appendFormalLine("}");

		// int checkedItems = getListView().getCheckedItemCount();
		bodyBuilder.appendFormalLine("int checkedItems = getListView().getCheckedItemCount();");

		// if(checkedItems>0)
		bodyBuilder.appendFormalLine("if(checkedItems>0){");
		bodyBuilder.indent();

		// mode.setSubtitle(String.format("%s entity%s selected", checkedItems,
		// checkedItems > 1 ? "s" : ""));
		bodyBuilder.appendFormalLine("mode.setSubtitle(String.format(\"%s " + entity.getSimpleTypeName().toLowerCase()
				+ "%s selected\", checkedItems, checkedItems > 1 ? \"s\" : \"\"));");

		bodyBuilder.indentRemove();
		bodyBuilder.appendFormalLine("}");

		// If there are more than one selected item, is not possible edit or
		// show elements
		bodyBuilder
				.appendFormalLine("// If there are more than one selected item, is not possible edit or show elements");

		// if (contextualMenu != null){
		bodyBuilder.appendFormalLine("if (contextualMenu != null) {");
		bodyBuilder.indent();

		// MenuItem showMenuItem = contextualMenu.getItem(0);
		bodyBuilder.appendFormalLine("MenuItem showMenuItem = contextualMenu.getItem(0);");

		// MenuItem editMenuItem = contextualMenu.getItem(1);
		bodyBuilder.appendFormalLine("MenuItem editMenuItem = contextualMenu.getItem(1);");

		// boolean toShow = true;
		bodyBuilder.appendFormalLine("boolean toShow = true;");

		// if (checkedItems > 1) {
		bodyBuilder.appendFormalLine("if (checkedItems > 1) {");
		bodyBuilder.indent();

		// toShow = false;
		bodyBuilder.appendFormalLine("toShow = false;");
		bodyBuilder.indentRemove();
		bodyBuilder.appendFormalLine("}");

		// showMenuItem.setVisible(toShow);
		bodyBuilder.appendFormalLine("showMenuItem.setVisible(toShow);");

		// editMenuItem.setVisible(toShow);
		bodyBuilder.appendFormalLine("editMenuItem.setVisible(toShow);");
		bodyBuilder.indentRemove();
		bodyBuilder.appendFormalLine("}");

	}

	/**
	 * Method that generates onCreateActionMode ListActivity method
	 * 
	 * @return
	 */
	private MethodMetadataBuilder getOnCreateActionModeMethod() {
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
				new JavaSymbolName("onCreateActionMode"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
				bodyBuilder);
		methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

		// Including comments
		CommentStructure commentStructure = new CommentStructure();
		JavadocComment comment = new JavadocComment(
				"Called when action mode is first created. The menu supplied will be used to \n"
						+ "generate action buttons for the action mode. \n \n"
						+ "@param mode ActionMode being created. \n"
						+ "@param menu Menu used to populate action buttons. \n \n"
						+ "@return true if the action mode should be created, false if entering this \n"
						+ "mode should be aborted. \n");
		commentStructure.addComment(comment, CommentLocation.BEGINNING);
		methodBuilder.setCommentStructure(commentStructure);

		return methodBuilder; // Build and return a MethodMetadata
		// instance
	}

	/**
	 * Generates onCreateActionMode ListActivity method body
	 * 
	 * @param bodyBuilder
	 */
	private void buildOnCreateActionModeMethodBody(InvocableMemberBodyBuilder bodyBuilder) {

		// Setting title
		bodyBuilder.appendFormalLine("// Setting title");

		// mode.setTitle("Selected Entitiy");
		bodyBuilder.appendFormalLine(String.format("mode.setTitle(\"Selected %s\");", entity.getSimpleTypeName()));

		// Inflate the menu for the CAB
		bodyBuilder.appendFormalLine("// Inflate the menu for the CAB");

		// MenuInflater inflater = mode.getMenuInflater();
		bodyBuilder.appendFormalLine(String.format("%s inflater = mode.getMenuInflater();",
				new JavaType("android.view.MenuInflater").getNameIncludingTypeParameters(false, importResolver)));

		// inflater.inflate(R.menu.contextual_menu, menu);
		bodyBuilder.appendFormalLine("inflater.inflate(R.menu.contextual_menu, menu);");

		// contextualMenu = menu;
		bodyBuilder.appendFormalLine("contextualMenu = menu;");

		// actionMode = mode;
		bodyBuilder.appendFormalLine("actionMode = mode;");

		// return true
		bodyBuilder.appendFormalLine("return true;");

	}

	/**
	 * Method that generates onPrepareActionMode ListActivity method
	 * 
	 * @return
	 */
	private MethodMetadataBuilder getOnPrepareActionModeMethod() {
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

		buildOnPrepareActionModeMethodBody(bodyBuilder);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onPrepareActionMode"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
				bodyBuilder);
		methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

		// Including comments
		CommentStructure commentStructure = new CommentStructure();
		JavadocComment comment = new JavadocComment(
				"Called to refresh an action mode's action menu whenever it is invalidated. \n \n"
						+ "@param mode ActionMode being prepared. \n"
						+ "@param menu Menu used to populate action buttons. \n \n"
						+ "@return true if the menu or action mode was updated, false otherwise. \n");
		commentStructure.addComment(comment, CommentLocation.BEGINNING);
		methodBuilder.setCommentStructure(commentStructure);

		return methodBuilder; // Build and return a MethodMetadata
		// instance
	}

	/**
	 * Generates onPrepareActionMode ListActivity method body
	 * 
	 * @param bodyBuilder
	 */
	private void buildOnPrepareActionModeMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
		// return false;
		bodyBuilder.appendFormalLine("return false;");
	}

	/**
	 * Method that generates onActionItemClicked ListActivity method
	 * 
	 * @return
	 */
	private MethodMetadataBuilder getOnActionItemClickedMethod() {
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

		buildonActionItemClickedMethodBody(bodyBuilder);

		// Use the MethodMetadataBuilder for easy creation of MethodMetadata
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onActionItemClicked"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
				bodyBuilder);
		methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

		// Including comments
		CommentStructure commentStructure = new CommentStructure();
		JavadocComment comment = new JavadocComment("Called to report a user click on an action button. \n \n"
				+ "@param mode The current ActionMode \n" + "@param item The item that was clicked \n \n"
				+ "@return true if this callback handled the event, false if the standard MenuItem \n"
				+ "invocation should continue. \n");
		commentStructure.addComment(comment, CommentLocation.BEGINNING);
		methodBuilder.setCommentStructure(commentStructure);

		return methodBuilder; // Build and return a MethodMetadata
		// instance
	}

	/**
	 * Generates onActionItemClicked ListActivity method body
	 * 
	 * @param bodyBuilder
	 */
	private void buildonActionItemClickedMethodBody(InvocableMemberBodyBuilder bodyBuilder) {

		// Respond to clicks on the actions in the CAB
		bodyBuilder.appendFormalLine("// Respond to clicks on the actions in the CAB");

		// Intent intent = new Intent(EntityListActivity.this,
		// EntityFormActivity.class);
		bodyBuilder.appendFormalLine(String.format("%s intent = new Intent(%sListActivity.this, %sFormActivity.class);",
				new JavaType("android.content.Intent").getNameIncludingTypeParameters(false, importResolver),
				entity.getSimpleTypeName(), entity.getSimpleTypeName()));

		// Bundle bundle = new Bundle();
		bodyBuilder.appendFormalLine(String.format("%s bundle = new Bundle();",
				new JavaType("android.os.Bundle").getNameIncludingTypeParameters(false, importResolver)));

		// switch (item.getItemId()) {
		bodyBuilder.appendFormalLine("switch (item.getItemId()) {");
		bodyBuilder.indent();

		// case R.id.item_show:
		bodyBuilder.appendFormalLine("case R.id.item_show:");
		bodyBuilder.indent();

		// Show selected entity
		bodyBuilder.appendFormalLine(String.format("// Show selected %s", entity.getSimpleTypeName().toLowerCase()));

		// bundle.putInt("entityId", selectedEntity.get(0).getId());
		bodyBuilder.appendFormalLine(String.format("bundle.putInt(\"%sId\", selected%s.get(0).getId());",
				entity.getSimpleTypeName().toLowerCase(), entity.getSimpleTypeName()));

		// bundle.putString("mode", "show");
		bodyBuilder.appendFormalLine("bundle.putString(\"mode\", \"show\");");

		// intent.putExtras(bundle);
		bodyBuilder.appendFormalLine("intent.putExtras(bundle);");

		// EntityListActivity.this.startActivity(intent);
		bodyBuilder.appendFormalLine(
				String.format("%sListActivity.this.startActivity(intent);", entity.getSimpleTypeName()));

		// break;
		bodyBuilder.appendFormalLine("break;");
		bodyBuilder.indentRemove();

		// case R.id.item_edit:
		bodyBuilder.appendFormalLine("case R.id.item_edit:");
		bodyBuilder.indent();

		// Edit selected entity
		bodyBuilder.appendFormalLine(String.format("// Edit selected %s", entity.getSimpleTypeName().toLowerCase()));

		// bundle.putInt("entityId", selectedEntity.get(0).getId());
		bodyBuilder.appendFormalLine(String.format("bundle.putInt(\"%sId\", selected%s.get(0).getId());",
				entity.getSimpleTypeName().toLowerCase(), entity.getSimpleTypeName()));

		// intent.putExtras(bundle);
		bodyBuilder.appendFormalLine("intent.putExtras(bundle);");

		// EntityListActivity.this.startActivity(intent);
		bodyBuilder.appendFormalLine(
				String.format("%sListActivity.this.startActivity(intent);", entity.getSimpleTypeName()));

		// break;
		bodyBuilder.appendFormalLine("break;");
		bodyBuilder.indentRemove();

		// case R.id.item_delete:
		bodyBuilder.appendFormalLine("case R.id.item_delete:");
		bodyBuilder.indent();

		// Remove all selected entitiy
		bodyBuilder
				.appendFormalLine(String.format("// Remove all selected %s", entity.getSimpleTypeName().toLowerCase()));

		// try {
		bodyBuilder.appendFormalLine("try {");
		bodyBuilder.indent();

		// removeEntity();
		bodyBuilder.appendFormalLine(String.format("remove%s();", entity.getSimpleTypeName()));
		bodyBuilder.indentRemove();

		// } catch (SQLException e) {
		bodyBuilder.appendFormalLine(String.format("} catch (%s e) {",
				new JavaType("java.sql.SQLException").getNameIncludingTypeParameters(false, importResolver)));
		bodyBuilder.indent();

		// e.printStackTrace();
		bodyBuilder.appendFormalLine("e.printStackTrace();");
		bodyBuilder.indentRemove();
		bodyBuilder.appendFormalLine("}");

		// break;
		bodyBuilder.appendFormalLine("break;");
		bodyBuilder.indentRemove();
		bodyBuilder.indentRemove();
		bodyBuilder.appendFormalLine("}");

		// return true;
		bodyBuilder.appendFormalLine("return true;");

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
