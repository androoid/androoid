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

  private static final JavaType ARRAY_ADAPTER_JAVATYPE =
      new JavaType("android.widget.ArrayAdapter");
  private static final String PROVIDES_TYPE_STRING = AndrooidActivityListMetadata.class.getName();
  private static final String PROVIDES_TYPE = MetadataIdentificationUtils
      .create(PROVIDES_TYPE_STRING);

  private final ImportRegistrationResolver importResolver;
  private final JavaType entity;
  private final JavaPackage applicationPackage;
  private final JavaType listEntityJavaType;
  private final JavaType arrayListEntityJavaType;
  private final String getIdFieldMethod;
  private final JavaType entityIdFieldType;

  public static String createIdentifier(final JavaType javaType, final LogicalPath path) {
    return PhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, javaType, path);
  }

  public static JavaType getJavaType(final String metadataIdentificationString) {
    return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING,
        metadataIdentificationString);
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
  public AndrooidActivityListMetadata(final String identifier, final JavaType aspectName,
      final PhysicalTypeMetadata governorPhysicalTypeMetadata, JavaPackage projectPackage,
      JavaType entity, String entityIdFieldName, JavaType entityIdFieldType) {
    super(identifier, aspectName, governorPhysicalTypeMetadata);
    Validate.isTrue(isValid(identifier),
        "Metadata identification string '%s' does not appear to be a valid", identifier);

    this.importResolver = builder.getImportRegistrationResolver();
    this.entity = entity;
    this.applicationPackage = projectPackage;
    this.listEntityJavaType =
        new JavaType("java.util.List", 0, DataType.TYPE, null, Arrays.asList(entity));
    this.arrayListEntityJavaType =
        new JavaType("java.util.ArrayList", 0, DataType.TYPE, null, Arrays.asList(entity));
    this.getIdFieldMethod =
        "get".concat(Character.toUpperCase(entityIdFieldName.charAt(0))
            + entityIdFieldName.substring(1));
    this.entityIdFieldType = entityIdFieldType;

    // Adding fields
    addListActivityFields();

    // Adding @Override necessary methods
    builder.addMethod(getOnCreateMethod());
    builder.addMethod(getOnCreateOptionsMenuMethod());
    builder.addMethod(getOnOptionsItemSelectedMethod());
    builder.addMethod(getOnItemCheckedStateChangedMethod());
    builder.addMethod(getOnCreateActionModeMethod());
    builder.addMethod(getOnPrepareActionModeMethod());
    builder.addMethod(getOnActionItemClickedMethod());
    builder.addMethod(getOnDestroyActionModeMethod());
    builder.addMethod(getOnItemClickMethod());

    // Add methods to manage entity data
    builder.addMethod(getFillEntityListMethod());
    builder.addMethod(getRemoveEntityMethod());

    // Create a representation of the desired output ITD
    itdTypeDetails = builder.build();

  }

  /**
   * Method to add all necessary fields to ListActivity .aj file
   */
  private void addListActivityFields() {
    FieldMetadataBuilder adapterField =
        new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName("adapter"),
            ARRAY_ADAPTER_JAVATYPE, null);
    builder.addField(adapterField);

    FieldMetadataBuilder selectedEntitiesField =
        new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName(
            "selected".concat(entity.getSimpleTypeName())), listEntityJavaType, null);
    builder.addField(selectedEntitiesField);

    FieldMetadataBuilder entityListField =
        new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName(entity
            .getSimpleTypeName().toLowerCase().concat("List")), arrayListEntityJavaType, null);
    builder.addField(entityListField);

    FieldMetadataBuilder contextualMenuField =
        new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName("contextualMenu"),
            new JavaType("android.view.Menu"), null);
    builder.addField(contextualMenuField);

    FieldMetadataBuilder actionModeField =
        new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName("actionMode"),
            new JavaType("android.view.ActionMode"), null);
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
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName("onCreate"),
            JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);
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
            .getNameIncludingTypeParameters(false, importResolver), entity.getSimpleTypeName()
            .toLowerCase()));

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
    bodyBuilder.appendFormalLine(String.format(
        "getListView().setChoiceMode(%s.CHOICE_MODE_MULTIPLE_MODAL);", new JavaType(
            "android.widget.ListView").getNameIncludingTypeParameters(false, importResolver)));

    // getListView().setMultiChoiceModeListener(this);
    bodyBuilder.appendFormalLine("getListView().setMultiChoiceModeListener(this);");

    // Fill entity list with Entity from Database
    bodyBuilder.appendFormalLine(String.format("// Fill %s list with %s from Database", entity
        .getSimpleTypeName().toLowerCase(), entity.getSimpleTypeName()));

    // try {
    bodyBuilder.appendFormalLine("try {");
    bodyBuilder.indent();

    // fillEntityList();
    bodyBuilder.appendFormalLine(String.format("fill%sList();", entity.getSimpleTypeName()));
    bodyBuilder.indentRemove();

    // } catch (SQLException e) {
    bodyBuilder.appendFormalLine(String.format("} catch (%s e) {", new JavaType(
        "java.sql.SQLException").getNameIncludingTypeParameters(false, importResolver)));
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
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName(
            "onCreateOptionsMenu"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
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
    bodyBuilder
        .appendFormalLine("// Inflate the menu; this adds items to the action bar if it is present.");

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
    parameterTypes
        .add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.MenuItem")));

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
    parameterNames.add(new JavaSymbolName("item"));

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildOnOptionsItemSelectedMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName(
            "onOptionsItemSelected"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
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
    bodyBuilder.appendFormalLine(String.format("%s.navigateUpFromSameTask(this);", new JavaType(
        "android.support.v4.app.NavUtils").getNameIncludingTypeParameters(false, importResolver)));

    // return true;
    bodyBuilder.appendFormalLine("return true;");
    bodyBuilder.indentRemove();

    // case R.id.action_add:
    bodyBuilder.appendFormalLine("case R.id.action_add:");
    bodyBuilder.indent();

    // Intent intent = new Intent(EntityListActivity.this,
    // EntityFormActivity.class);
    bodyBuilder.appendFormalLine(String.format(
        "%s intent = new Intent(%sListActivity.this, %sFormActivity.class);", new JavaType(
            "android.content.Intent").getNameIncludingTypeParameters(false, importResolver), entity
            .getSimpleTypeName(), entity.getSimpleTypeName()));

    // EntityListActivity.this.startActivity(intent);
    bodyBuilder.appendFormalLine(String.format("%sListActivity.this.startActivity(intent);",
        entity.getSimpleTypeName()));

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
    parameterTypes.add(AnnotatedJavaType
        .convertFromJavaType(new JavaType("android.view.ActionMode")));
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
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName(String.format(
            "onCheckedStateChanged%s", entity.getSimpleTypeName())), JavaType.VOID_PRIMITIVE,
            parameterTypes, parameterNames, bodyBuilder);

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment =
        new JavadocComment(
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
    bodyBuilder.appendFormalLine(String.format(
        "%s %s = (%s) getListView().getItemAtPosition(position);", entity.getSimpleTypeName(),
        entity.getSimpleTypeName().toLowerCase(), entity.getSimpleTypeName()));

    // View child = getListView().getChildAt(position);
    bodyBuilder.appendFormalLine(String.format("%s child = getListView().getChildAt(position);",
        new JavaType("android.view.View").getNameIncludingTypeParameters(false, importResolver)));

    // Checking if current item was checked before
    bodyBuilder.appendFormalLine("// Checking if current item was checked before");

    // if (selectedEntity.indexOf(entity) != -1) {
    bodyBuilder.appendFormalLine(String.format("if (selected%s.indexOf(%s) != -1) {",
        entity.getSimpleTypeName(), entity.getSimpleTypeName().toLowerCase()));
    bodyBuilder.indent();

    // Removing element from selected Entity
    bodyBuilder.appendFormalLine(String.format("// Removing element from selected %s",
        entity.getSimpleTypeName()));

    // selectedEntity.remove(selectedEntity.indexOf(entity));
    bodyBuilder
        .appendFormalLine(String.format("selected%s.remove(selected%s.indexOf(%s));", entity
            .getSimpleTypeName(), entity.getSimpleTypeName(), entity.getSimpleTypeName()
            .toLowerCase()));

    // Removing background
    bodyBuilder.appendFormalLine("// Removing background");

    // if (child != null) {
    bodyBuilder.appendFormalLine("if (child != null) {");
    bodyBuilder.indent();

    // child.setSelected(false);
    bodyBuilder.appendFormalLine("child.setSelected(false);");

    // child.setBackgroundColor(Color.WHITE);
    bodyBuilder.appendFormalLine(String.format("child.setBackgroundColor(%s.WHITE);", new JavaType(
        "android.graphics.Color").getNameIncludingTypeParameters(false, importResolver)));
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}");
    bodyBuilder.indentRemove();

    // } else {
    bodyBuilder.appendFormalLine("} else {");
    bodyBuilder.indent();

    // Adding element to selected Entity
    bodyBuilder.appendFormalLine(String.format("// Adding element to selected %s",
        entity.getSimpleTypeName()));

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
    bodyBuilder.appendFormalLine("mode.setSubtitle(String.format(\"%s "
        + entity.getSimpleTypeName().toLowerCase()
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
    parameterTypes.add(AnnotatedJavaType
        .convertFromJavaType(new JavaType("android.view.ActionMode")));
    parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.Menu")));

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
    parameterNames.add(new JavaSymbolName("mode"));
    parameterNames.add(new JavaSymbolName("menu"));

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildOnCreateActionModeMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName(String.format(
            "onCreateActionMode%s", entity.getSimpleTypeName())), JavaType.BOOLEAN_PRIMITIVE,
            parameterTypes, parameterNames, bodyBuilder);

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment =
        new JavadocComment(
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
    bodyBuilder.appendFormalLine(String.format("mode.setTitle(\"Selected %s\");",
        entity.getSimpleTypeName()));

    // Inflate the menu for the CAB
    bodyBuilder.appendFormalLine("// Inflate the menu for the CAB");

    // MenuInflater inflater = mode.getMenuInflater();
    bodyBuilder.appendFormalLine(String.format("%s inflater = mode.getMenuInflater();",
        new JavaType("android.view.MenuInflater").getNameIncludingTypeParameters(false,
            importResolver)));

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
    parameterTypes.add(AnnotatedJavaType
        .convertFromJavaType(new JavaType("android.view.ActionMode")));
    parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.Menu")));

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
    parameterNames.add(new JavaSymbolName("mode"));
    parameterNames.add(new JavaSymbolName("menu"));

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildOnPrepareActionModeMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName(String.format(
            "onPrepareActionMode%s", entity.getSimpleTypeName())), JavaType.BOOLEAN_PRIMITIVE,
            parameterTypes, parameterNames, bodyBuilder);

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment =
        new JavadocComment(
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
    parameterTypes.add(AnnotatedJavaType
        .convertFromJavaType(new JavaType("android.view.ActionMode")));
    parameterTypes
        .add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.MenuItem")));

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
    parameterNames.add(new JavaSymbolName("mode"));
    parameterNames.add(new JavaSymbolName("item"));

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildonActionItemClickedMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName(String.format(
            "onActionItemClicked%s", entity.getSimpleTypeName())), JavaType.BOOLEAN_PRIMITIVE,
            parameterTypes, parameterNames, bodyBuilder);

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment =
        new JavadocComment("Called to report a user click on an action button. \n \n"
            + "@param mode The current ActionMode \n"
            + "@param item The item that was clicked \n \n"
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
    bodyBuilder.appendFormalLine(String.format(
        "%s intent = new Intent(%sListActivity.this, %sFormActivity.class);", new JavaType(
            "android.content.Intent").getNameIncludingTypeParameters(false, importResolver), entity
            .getSimpleTypeName(), entity.getSimpleTypeName()));

    // Bundle bundle = new Bundle();
    bodyBuilder.appendFormalLine(String.format("%s bundle = new Bundle();", new JavaType(
        "android.os.Bundle").getNameIncludingTypeParameters(false, importResolver)));

    // switch (item.getItemId()) {
    bodyBuilder.appendFormalLine("switch (item.getItemId()) {");
    bodyBuilder.indent();

    // case R.id.item_show:
    bodyBuilder.appendFormalLine("case R.id.item_show:");
    bodyBuilder.indent();

    // Show selected entity
    bodyBuilder.appendFormalLine(String.format("// Show selected %s", entity.getSimpleTypeName()
        .toLowerCase()));

    // bundle.putInt("entityId", selectedEntity.get(0).getEntityIdField());
    bodyBuilder.appendFormalLine(String.format("bundle.put%s(\"%sId\", selected%s.get(0).%s());",
        entityIdFieldType.getSimpleTypeName(), entity.getSimpleTypeName().toLowerCase(),
        entity.getSimpleTypeName(), getIdFieldMethod));

    // bundle.putString("mode", "show");
    bodyBuilder.appendFormalLine("bundle.putString(\"mode\", \"show\");");

    // intent.putExtras(bundle);
    bodyBuilder.appendFormalLine("intent.putExtras(bundle);");

    // EntityListActivity.this.startActivity(intent);
    bodyBuilder.appendFormalLine(String.format("%sListActivity.this.startActivity(intent);",
        entity.getSimpleTypeName()));

    // break;
    bodyBuilder.appendFormalLine("break;");
    bodyBuilder.indentRemove();

    // case R.id.item_edit:
    bodyBuilder.appendFormalLine("case R.id.item_edit:");
    bodyBuilder.indent();

    // Edit selected entity
    bodyBuilder.appendFormalLine(String.format("// Edit selected %s", entity.getSimpleTypeName()
        .toLowerCase()));

    // bundle.putInt("entityId", selectedEntity.get(0).getIdField());
    bodyBuilder.appendFormalLine(String.format("bundle.put%s(\"%sId\", selected%s.get(0).%s());",
        entityIdFieldType.getSimpleTypeName(), entity.getSimpleTypeName().toLowerCase(),
        entity.getSimpleTypeName(), getIdFieldMethod));

    // intent.putExtras(bundle);
    bodyBuilder.appendFormalLine("intent.putExtras(bundle);");

    // EntityListActivity.this.startActivity(intent);
    bodyBuilder.appendFormalLine(String.format("%sListActivity.this.startActivity(intent);",
        entity.getSimpleTypeName()));

    // break;
    bodyBuilder.appendFormalLine("break;");
    bodyBuilder.indentRemove();

    // case R.id.item_delete:
    bodyBuilder.appendFormalLine("case R.id.item_delete:");
    bodyBuilder.indent();

    // Remove all selected entitiy
    bodyBuilder.appendFormalLine(String.format("// Remove all selected %s", entity
        .getSimpleTypeName().toLowerCase()));

    // try {
    bodyBuilder.appendFormalLine("try {");
    bodyBuilder.indent();

    // removeEntity();
    bodyBuilder.appendFormalLine(String.format("remove%s();", entity.getSimpleTypeName()));
    bodyBuilder.indentRemove();

    // } catch (SQLException e) {
    bodyBuilder.appendFormalLine(String.format("} catch (%s e) {", new JavaType(
        "java.sql.SQLException").getNameIncludingTypeParameters(false, importResolver)));
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

  /**
   * Method that generates onDestroyActionMode ListActivity method
   * 
   * @return
   */
  private MethodMetadataBuilder getOnDestroyActionModeMethod() {
    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
    parameterTypes.add(AnnotatedJavaType
        .convertFromJavaType(new JavaType("android.view.ActionMode")));

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
    parameterNames.add(new JavaSymbolName("mode"));

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildOnDestroyActionModeMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName(String.format(
            "onDestroyActionMode%s", entity.getSimpleTypeName())), JavaType.VOID_PRIMITIVE,
            parameterTypes, parameterNames, bodyBuilder);

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment =
        new JavadocComment("Called when an action mode is about to be exited and destroyed. \n \n"
            + "@param mode The current ActionMode being destroyed. \n");
    commentStructure.addComment(comment, CommentLocation.BEGINNING);
    methodBuilder.setCommentStructure(commentStructure);

    return methodBuilder; // Build and return a MethodMetadata
    // instance
  }

  /**
   * Generates onDestroyActionMode ListActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildOnDestroyActionModeMethodBody(InvocableMemberBodyBuilder bodyBuilder) {

    // Cleaning selected entity
    bodyBuilder.appendFormalLine(String.format("// Cleaning selected %s", entity
        .getSimpleTypeName().toLowerCase()));

    // selectedEntity.clear();
    bodyBuilder.appendFormalLine(String.format("selected%s.clear();", entity.getSimpleTypeName()));

    // try {
    bodyBuilder.appendFormalLine("try {");
    bodyBuilder.indent();

    // fillEntityList();
    bodyBuilder.appendFormalLine(String.format("fill%sList();", entity.getSimpleTypeName()));
    bodyBuilder.indentRemove();

    // } catch (SQLException e) {
    bodyBuilder.appendFormalLine(String.format("} catch (%s e) {", new JavaType(
        "java.sql.SQLException").getNameIncludingTypeParameters(false, importResolver)));
    bodyBuilder.indent();

    // e.printStackTrace();
    bodyBuilder.appendFormalLine("e.printStackTrace();");
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}");

  }

  /**
   * Method that generates onItemClick ListActivity method
   * 
   * @return
   */
  private MethodMetadataBuilder getOnItemClickMethod() {
    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
    parameterTypes.add(AnnotatedJavaType.convertFromJavaType(JavaType.wrapperWilcard(new JavaType(
        "android.widget.AdapterView"))));
    parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType("android.view.View")));
    parameterTypes.add(AnnotatedJavaType.convertFromJavaType(JavaType.INT_PRIMITIVE));
    parameterTypes.add(AnnotatedJavaType.convertFromJavaType(JavaType.LONG_PRIMITIVE));

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
    parameterNames.add(new JavaSymbolName("parent"));
    parameterNames.add(new JavaSymbolName("view"));
    parameterNames.add(new JavaSymbolName("position"));
    parameterNames.add(new JavaSymbolName("id"));

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildOnItemClickMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName(String.format(
            "onItemClick%s", entity.getSimpleTypeName())), JavaType.VOID_PRIMITIVE, parameterTypes,
            parameterNames, bodyBuilder);

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment =
        new JavadocComment("Callback method to be invoked when an item in this AdapterView has \n"
            + "been clicked. \n \n"
            + "Implementers can call getItemAtPosition(position) if they need \n"
            + "to access the data associated with the selected item. \n \n"
            + "@param parent   The AdapterView where the click happened. \n"
            + "@param view     The view within the AdapterView that was clicked (this \n"
            + "                will be a view provided by the adapter) \n"
            + "@param position The position of the view in the adapter. \n"
            + "@param id       The row id of the item that was clicked. \n");
    commentStructure.addComment(comment, CommentLocation.BEGINNING);
    methodBuilder.setCommentStructure(commentStructure);

    return methodBuilder; // Build and return a MethodMetadata
    // instance
  }

  /**
   * Generates onItemClick ListActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildOnItemClickMethodBody(InvocableMemberBodyBuilder bodyBuilder) {

    // Show selected entity
    bodyBuilder.appendFormalLine(String.format("// Show selected %s", entity.getSimpleTypeName()
        .toLowerCase()));

    // Intent intent = new Intent(EntityListActivity.this,
    // EntityFormActivity.class);
    bodyBuilder.appendFormalLine(String.format(
        "%s intent = new Intent(%sListActivity.this, %sFormActivity.class);", new JavaType(
            "android.content.Intent").getNameIncludingTypeParameters(false, importResolver), entity
            .getSimpleTypeName(), entity.getSimpleTypeName()));

    // Bundle bundle = new Bundle();
    bodyBuilder.appendFormalLine(String.format("%s bundle = new Bundle();", new JavaType(
        "android.os.Bundle").getNameIncludingTypeParameters(false, importResolver)));

    // Entity entity = (Entity) getListView().getItemAtPosition(position);
    bodyBuilder.appendFormalLine(String.format(
        "%s %s = (%s) getListView().getItemAtPosition(position);", entity.getSimpleTypeName(),
        entity.getSimpleTypeName().toLowerCase(), entity.getSimpleTypeName()));

    // bundle.putInt("entityId", selectedEntity.get(0).getIdField());
    bodyBuilder.appendFormalLine(String.format("bundle.put%s(\"%sId\", %s.%s());",
        entityIdFieldType.getSimpleTypeName(), entity.getSimpleTypeName().toLowerCase(), entity
            .getSimpleTypeName().toLowerCase(), getIdFieldMethod));

    // bundle.putString("mode", "show");
    bodyBuilder.appendFormalLine("bundle.putString(\"mode\", \"show\");");

    // intent.putExtras(bundle);
    bodyBuilder.appendFormalLine("intent.putExtras(bundle);");

    // EntityListActivity.this.startActivity(intent);
    bodyBuilder.appendFormalLine(String.format("%sListActivity.this.startActivity(intent);",
        entity.getSimpleTypeName()));

  }

  /**
   * Method that generates fillEntityList ListActivity method
   * 
   * @return
   */
  private MethodMetadataBuilder getFillEntityListMethod() {
    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildFillEntityListMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName(String.format(
            "fill%sList", entity.getSimpleTypeName())), JavaType.VOID_PRIMITIVE, parameterTypes,
            parameterNames, bodyBuilder);
    methodBuilder.addThrowsType(new JavaType("java.sql.SQLException"));

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment =
        new JavadocComment(String.format(
            "Method that fills %s list with %s getted from Database. \n \n"
                + "@throws SQLException \n", entity.getSimpleTypeName().toLowerCase(), entity
                .getSimpleTypeName().toLowerCase()));
    commentStructure.addComment(comment, CommentLocation.BEGINNING);
    methodBuilder.setCommentStructure(commentStructure);

    return methodBuilder; // Build and return a MethodMetadata
    // instance
  }

  /**
   * Generates fillEntityList ListActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildFillEntityListMethodBody(InvocableMemberBodyBuilder bodyBuilder) {

    // Dao<Entity, Integer> entityDao = getHelper().getEntityDao();
    bodyBuilder.appendFormalLine(String.format("%s<%s, Integer> %sDao = getHelper().get%sDao();",
        new JavaType("com.j256.ormlite.dao.Dao").getNameIncludingTypeParameters(false,
            importResolver), entity.getSimpleTypeName(), entity.getSimpleTypeName().toLowerCase(),
        entity.getSimpleTypeName()));

    // List<Entity> entity = entityDao.queryForAll();
    bodyBuilder.appendFormalLine(String.format("List<%s> %s = %sDao.queryForAll();", entity
        .getSimpleTypeName(), entity.getSimpleTypeName().toLowerCase(), entity.getSimpleTypeName()
        .toLowerCase()));

    // Creating entity ArrayList
    bodyBuilder.appendFormalLine("// Creating entity ArrayList");

    // entityList = new ArrayList<Entity>();
    bodyBuilder.appendFormalLine(String.format("%sList = new ArrayList<%s>();", entity
        .getSimpleTypeName().toLowerCase(), entity.getSimpleTypeName()));

    // for (Entity item : entity) {
    bodyBuilder.appendFormalLine(String.format("for (%s item : %s) {", entity.getSimpleTypeName(),
        entity.getSimpleTypeName().toLowerCase()));
    bodyBuilder.indent();

    // entityList.add(entity);
    bodyBuilder.appendFormalLine(String.format("%sList.add(item);", entity.getSimpleTypeName()
        .toLowerCase()));
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}");

    // Creating array adapter
    bodyBuilder.appendFormalLine("// Creating array adapter");

    // adapter=new
    // ArrayAdapter(this,android.R.layout.simple_list_item_1,entityList);
    bodyBuilder.appendFormalLine(String.format(
        "adapter=new ArrayAdapter(this, android.R.layout.simple_list_item_1, %sList);", entity
            .getSimpleTypeName().toLowerCase()));

    // getListView().setAdapter(adapter);
    bodyBuilder.appendFormalLine("getListView().setAdapter(adapter);");

  }

  /**
   * Method that generates removeEntity ListActivity method
   * 
   * @return
   */
  private MethodMetadataBuilder getRemoveEntityMethod() {
    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildRemoveEntityMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName(String.format(
            "remove%s", entity.getSimpleTypeName())), JavaType.VOID_PRIMITIVE, parameterTypes,
            parameterNames, bodyBuilder);
    methodBuilder.addThrowsType(new JavaType("java.sql.SQLException"));

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment =
        new JavadocComment(String.format("Method that removes all selected %s \n \n"
            + "@throws SQLException \n", entity.getSimpleTypeName().toLowerCase()));
    commentStructure.addComment(comment, CommentLocation.BEGINNING);
    methodBuilder.setCommentStructure(commentStructure);

    return methodBuilder; // Build and return a MethodMetadata
    // instance
  }

  /**
   * Generates removeEntity ListActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildRemoveEntityMethodBody(InvocableMemberBodyBuilder bodyBuilder) {

    // Removing all selected entity
    bodyBuilder.appendFormalLine(String.format("// Removing all selected %s", entity
        .getSimpleTypeName().toLowerCase()));

    // Dao<Entity, Integer> entityDao = getHelper().getEntityDao();
    bodyBuilder.appendFormalLine(String.format("Dao<%s, Integer> %sDao = getHelper().get%sDao();",
        entity.getSimpleTypeName(), entity.getSimpleTypeName().toLowerCase(),
        entity.getSimpleTypeName()));

    // Integer deleted = entityDao.delete(selectedEntity);
    bodyBuilder.appendFormalLine(String.format("Integer deleted = %sDao.delete(selected%s);",
        entity.getSimpleTypeName().toLowerCase(), entity.getSimpleTypeName()));

    // Show message with total deleted items
    bodyBuilder.appendFormalLine("// Show message with total deleted items");

    // String message = String.format("%s %s deleted", deleted, deleted > 1
    // ? "entity were" : "entity was");
    bodyBuilder
        .appendFormalLine("String message = String.format(\"%s %s deleted\", deleted, deleted > 1 ? \""
            + entity.getSimpleTypeName().toLowerCase()
            + " were\" : \""
            + entity.getSimpleTypeName().toLowerCase() + " was\");");

    // Toast toast = Toast.makeText(getApplicationContext(), message,
    // Toast.LENGTH_SHORT);
    bodyBuilder
        .appendFormalLine(String.format(
            "%s toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);",
            new JavaType("android.widget.Toast").getNameIncludingTypeParameters(false,
                importResolver)));

    // toast.show();
    bodyBuilder.appendFormalLine("toast.show();");

    // Close action mode
    bodyBuilder.appendFormalLine("// Close action mode");

    // actionMode.finish();
    bodyBuilder.appendFormalLine("actionMode.finish();");

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
