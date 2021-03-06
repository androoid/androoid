package io.androoid.roo.addon.suite.addon.activities;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
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

import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidFormActivity;

/**
 * Metadata for {@link AndrooidFormActivity} annotation.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public class AndrooidActivityFormMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

  private static final JavaType ARRAY_ADAPTER_JAVATYPE =
      new JavaType("android.widget.ArrayAdapter");
  private static final String PROVIDES_TYPE_STRING = AndrooidActivityFormMetadata.class.getName();
  private static final String PROVIDES_TYPE = MetadataIdentificationUtils
      .create(PROVIDES_TYPE_STRING);

  private final ImportRegistrationResolver importResolver;
  private final JavaType entity;
  private final JavaPackage applicationPackage;
  private final List<FieldMetadata> entityFields;
  private final Map<String, String> fieldNameLayout;

  private boolean hasSpinners;
  private boolean hasGeoFields;

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
  public AndrooidActivityFormMetadata(final String identifier, final JavaType aspectName,
      final PhysicalTypeMetadata governorPhysicalTypeMetadata, JavaPackage projectPackage,
      JavaType entity, String entityIdFieldName, JavaType entityIdFieldType,
      List<FieldMetadata> entityFields) {
    super(identifier, aspectName, governorPhysicalTypeMetadata);
    Validate.isTrue(isValid(identifier),
        "Metadata identification string '%s' does not appear to be a valid", identifier);

    this.importResolver = builder.getImportRegistrationResolver();
    this.entity = entity;
    this.applicationPackage = projectPackage;
    this.entityFields = entityFields;
    this.fieldNameLayout = new HashMap<String, String>();
    this.hasSpinners = false;
    this.hasGeoFields = false;

    // Adding fields
    addFormActivityFields();

    // Adding necessary methods
    builder.addMethod(getOnCreateMethod());
    // If some spinners were detected onCreateMethod, is necessary to
    // include populateSpinners method
    if (hasSpinners) {
      builder.addMethod(getPopulateSpinnersMethod());
    }
    builder.addMethod(getDisableFormElementsMethod());
    builder.addMethod(getPopulateFormMethod());
    builder.addMethod(getCreateMethod());
    builder.addMethod(getUpdateMethod());
    builder.addMethod(getOnCreateOptionsMenuMethod());
    builder.addMethod(getOnOptionsItemSelectedMethod());

    // If has some GEO field means that should Override ProcessFinish method
    if (hasGeoFields) {
      builder.addMethod(getProcessFinishMethod());
    }

    // Create a representation of the desired output ITD
    itdTypeDetails = builder.build();

  }

  /**
   * Method that generates onCreate FormActivity method
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
   * Generates onCreate FormActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildOnCreateMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
    // super.onCreate(savedInstanceState);
    bodyBuilder.appendFormalLine("super.onCreate(savedInstanceState);");

    // setContentView(R.layout.entity_list_activity);
    bodyBuilder.appendFormalLine(String.format("setContentView(%s.layout.%s_form_activity);",
        new JavaType(applicationPackage.getFullyQualifiedPackageName().concat(".R"))
            .getNameIncludingTypeParameters(false, importResolver), entity.getSimpleTypeName()
            .toLowerCase()));

    // Adding back button
    bodyBuilder.appendFormalLine("");
    bodyBuilder.appendFormalLine("// Adding back button");

    // getActionBar().setDisplayHomeAsUpEnabled(true);
    bodyBuilder.appendFormalLine("getActionBar().setDisplayHomeAsUpEnabled(true);");

    // Getting form items
    bodyBuilder.appendFormalLine("");
    bodyBuilder.appendFormalLine("// Getting form items");

    // Using saved fieldNameLayout to include view items
    for (Entry<String, String> entry : fieldNameLayout.entrySet()) {
      String fieldName = entry.getKey();
      String fieldId = entry.getValue();
      String fieldType = "";

      boolean isMapView = false;
      if (fieldName.endsWith("EditText")) {
        fieldType = "EditText";
      } else if (fieldName.endsWith("Switch")) {
        fieldType = "Switch";
      } else if (fieldName.endsWith("MapView")) {
        fieldType = "MapView";
        isMapView = true;
        hasGeoFields = true;
      } else if (fieldName.endsWith("Spinner")) {
        fieldType = "Spinner";
        hasSpinners = true;
      }

      // fieldName = (fieldType) findViewById(R.id.fieldId);
      bodyBuilder.appendFormalLine(String.format("%s = (%s) findViewById(R.id.%s);", fieldName,
          fieldType, fieldId));

      // If is a MapView item needs some extra configuration
      if (isMapView) {

        // Including mapview text
        String textFieldName = fieldName.replaceFirst("MapView", "EditText");

        // fieldName = (fieldType) findViewById(R.id.fieldId);
        fieldId = fieldId.substring(0, fieldId.lastIndexOf("_")).concat("_text");
        bodyBuilder.appendFormalLine(String.format("%s = (%s) findViewById(R.id.%s);",
            textFieldName, "EditText", fieldId));

        // Initializing Map element
        bodyBuilder.appendFormalLine("// Initializing Map element");

        // fieldName.setTileSource(TileSourceFactory.MAPNIK);
        bodyBuilder.appendFormalLine(String.format("%s.setTileSource(%s.MAPNIK);", fieldName,
            new JavaType("org.osmdroid.tileprovider.tilesource.TileSourceFactory")
                .getNameIncludingTypeParameters(false, importResolver)));
        // fieldName.setMultiTouchControls(true);
        bodyBuilder.appendFormalLine(String.format("%s.setMultiTouchControls(true);", fieldName));

        // fieldName.setClickable(true);
        bodyBuilder.appendFormalLine(String.format("%s.setClickable(true);", fieldName));

        // Initial map position
        bodyBuilder.appendFormalLine("// Initial map position");

        // IMapController mapController = fieldName.getController();
        bodyBuilder.appendFormalLine(String.format("%s mapController = %s.getController();",
            new JavaType("org.osmdroid.api.IMapController").getNameIncludingTypeParameters(false,
                importResolver), fieldName));

        // mapController.setZoom(5);
        bodyBuilder.appendFormalLine("mapController.setZoom(5);");

        // GeoPoint startPoint = new GeoPoint(45.416775400000000000,
        // -7.703790199999957600);
        bodyBuilder.appendFormalLine(String.format(
            "%s startPoint = new GeoPoint(45.416775400000000000, -7.703790199999957600);",
            new JavaType("org.osmdroid.util.GeoPoint").getNameIncludingTypeParameters(false,
                importResolver)));

        // mapController.setCenter(startPoint);
        bodyBuilder.appendFormalLine("mapController.setCenter(startPoint);");

        bodyBuilder.appendFormalLine("");

        // Adding event on street input
        bodyBuilder.appendFormalLine("// Adding event on street input");

        // final Handler mHandler = new Handler();
        bodyBuilder.appendFormalLine(String.format("final %s mHandler = new Handler();",
            new JavaType("android.os.Handler")
                .getNameIncludingTypeParameters(false, importResolver)));

        // textFieldName.addTextChangedListener(new TextWatcher() {
        bodyBuilder.appendFormalLine(String.format("%s.addTextChangedListener(new %s() {",
            textFieldName, new JavaType("android.text.TextWatcher").getNameIncludingTypeParameters(
                false, importResolver)));

        // public void afterTextChanged(Editable s) {
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(String.format("public void afterTextChanged(%s s) {",
            new JavaType("android.text.Editable").getNameIncludingTypeParameters(false,
                importResolver)));

        // mHandler.removeCallbacks(mFilterTask);
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("mHandler.removeCallbacks(mFilterTask);");

        // mHandler.postDelayed(mFilterTask, 1000);
        bodyBuilder.appendFormalLine("mHandler.postDelayed(mFilterTask, 1000);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // public void beforeTextChanged(CharSequence s, int start, int
        // count, int after) {
        bodyBuilder.appendFormalLine("");
        bodyBuilder
            .appendFormalLine(String
                .format("public void beforeTextChanged(CharSequence s, int start, int count, int after) {"));
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("");

        // public void onTextChanged(CharSequence s, int start, int
        // before, int count) {
        bodyBuilder.appendFormalLine("");
        bodyBuilder
            .appendFormalLine(String
                .format("public void onTextChanged(CharSequence s, int start, int before, int count) {"));
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("");

        // Runnable mFilterTask = new Runnable() {
        bodyBuilder.appendFormalLine("Runnable mFilterTask = new Runnable() {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("@Override");

        // public void run() {
        bodyBuilder.appendFormalLine("public void run() {");
        bodyBuilder.indent();

        // // Creating AsyncTask to allow address search in async mode
        bodyBuilder.appendFormalLine("// Creating AsyncTask to allow address search in async mode");

        // addressSearchHelper = new AddressSearchHelper();
        bodyBuilder.appendFormalLine("addressSearchHelper = new AddressSearchHelper();");

        // addressSearchHelper.delegate = EntityFormActivity.this;
        bodyBuilder.appendFormalLine(String.format(
            "addressSearchHelper.delegate = %sFormActivity.this;", entity.getSimpleTypeName()));

        // // Update map with address location
        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine(" // Update map with address location");

        // String address = entityLocationText.getText().toString();
        bodyBuilder.appendFormalLine(String.format("String address = %s.getText().toString();",
            textFieldName));

        // if (address != null) {
        bodyBuilder.appendFormalLine("if (address != null) {");
        bodyBuilder.indent();

        // addressSearchHelper.execute(address);
        bodyBuilder.appendFormalLine("addressSearchHelper.execute(address);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("};");

        bodyBuilder.indentRemove();

        // });
        bodyBuilder.appendFormalLine("});");
      }

    }

    bodyBuilder.appendFormalLine("");

    // Populate spinners
    if (hasSpinners) {
      bodyBuilder.appendFormalLine("");
      bodyBuilder.appendFormalLine("// Populate spinners");
      bodyBuilder.appendFormalLine("populateSpinners();");
      bodyBuilder.appendFormalLine("");
    }

    // // Checking if is create view, update view or show view
    bodyBuilder.appendFormalLine("// Checking if is create view, update view or show view");

    // Bundle bundle = getIntent().getExtras();
    bodyBuilder.appendFormalLine("Bundle bundle = getIntent().getExtras();");

    // if(bundle != null){
    bodyBuilder.appendFormalLine("if(bundle != null){");
    bodyBuilder.indent();

    // // Getting entity id
    bodyBuilder.appendFormalLine(String.format("// Getting entity id"));

    // int entityId = bundle.getInt("entityId");
    bodyBuilder.appendFormalLine(String.format("int %s = bundle.getInt(\"%s\");", entity
        .getSimpleTypeName().toLowerCase().concat("Id"), entity.getSimpleTypeName().toLowerCase()
        .concat("Id")));

    // // Getting entity by id
    bodyBuilder.appendFormalLine("// Getting entity by id");

    // populateForm(entityId);
    bodyBuilder.appendFormalLine(String.format("populateForm(%s);", entity.getSimpleTypeName()
        .toLowerCase().concat("Id")));

    // // Disabling elements if is show view
    bodyBuilder.appendFormalLine("// Disabling elements if is show view");

    // mode = bundle.getString("mode");
    bodyBuilder.appendFormalLine("mode = bundle.getString(\"mode\");");

    // if("show".equals(mode)){
    bodyBuilder.appendFormalLine("if(\"show\".equals(mode)){");
    bodyBuilder.indent();

    // // Updating title to show
    bodyBuilder.appendFormalLine("// Updating title to show");

    // setTitle(R.string.title_activity_entity_form_show);
    bodyBuilder.appendFormalLine(String.format("setTitle(R.string.title_activity_%s_form_show);",
        entity.getSimpleTypeName().toLowerCase()));

    // disableFormElements();
    bodyBuilder.appendFormalLine("disableFormElements();");

    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}else{");
    bodyBuilder.indent();

    // // Updating title to edit
    bodyBuilder.appendFormalLine("// Updating title to edit");

    // setTitle(R.string.title_activity_entity_form_update);
    bodyBuilder.appendFormalLine(String.format("setTitle(R.string.title_activity_%s_form_update);",
        entity.getSimpleTypeName().toLowerCase()));

    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}");

    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}");
  }

  /**
   * Method that generates populateSpinners FormActivity method
   * 
   * @return
   */
  private MethodMetadataBuilder getPopulateSpinnersMethod() {
    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildPopulateSpinnersMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName("populateSpinners"),
            JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment = new JavadocComment("This method will populate all form Spinners \n");
    commentStructure.addComment(comment, CommentLocation.BEGINNING);
    methodBuilder.setCommentStructure(commentStructure);

    return methodBuilder; // Build and return a MethodMetadata
    // instance
  }

  /**
   * Generates populateSpinners FormActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildPopulateSpinnersMethodBody(InvocableMemberBodyBuilder bodyBuilder) {

    // try{
    bodyBuilder.appendFormalLine("try{");
    bodyBuilder.indent();

    // Getting all defined fields
    for (FieldMetadata field : entityFields) {

      // Checking if current field is a valid Database Field
      AnnotationMetadata databaseFieldAnnotation =
          field.getAnnotation(new JavaType("com.j256.ormlite.field.DatabaseField"));
      if (databaseFieldAnnotation != null) {

        // Checking if field is a generatedId field
        AnnotationAttributeValue<Boolean> generatedIdAttr =
            databaseFieldAnnotation.getAttribute("generatedId");

        boolean generatedId = false;

        if (generatedIdAttr != null) {
          generatedId = generatedIdAttr.getValue();
        }

        if (!generatedId) {
          // Getting fieldName
          String fieldName = getFieldNameOnActivity(field);
          JavaType fieldType = getFieldTypeOnActivity(field);

          // Populating all Spinner fields
          if (fieldType.equals(new JavaType("android.widget.Spinner"))) {

            JavaType relatedFieldType = field.getFieldType();
            String relatedFieldName = relatedFieldType.getSimpleTypeName().toLowerCase();

            // Populate relatedFieldName spinner
            bodyBuilder.appendFormalLine(String.format("// Populate %s spinner", relatedFieldName));

            // Dao<RelatedField, Integer> relatedFieldDao =
            // getHelper().getRelatedFieldDao();
            bodyBuilder.appendFormalLine(String.format(
                "Dao<%s, Integer> %sDao = getHelper().get%sDao();",
                relatedFieldType.getSimpleTypeName(), relatedFieldName,
                relatedFieldType.getSimpleTypeName()));

            // List<RelatedFields> results =
            // relatedFieldDao.queryForAll();
            bodyBuilder.appendFormalLine(String.format("%s<%s> %sResults = %sDao.queryForAll();",
                new JavaType("java.util.List")
                    .getNameIncludingTypeParameters(false, importResolver), relatedFieldType
                    .getSimpleTypeName(), relatedFieldName, relatedFieldName));

            // Creating relatedField ArrayList
            bodyBuilder.appendFormalLine(String
                .format("// Creating %s ArrayList", relatedFieldName));

            // relatedFieldList = new ArrayList<RelatedField>();
            bodyBuilder.appendFormalLine(String.format("%sList = new ArrayList<%s>();",
                relatedFieldName, relatedFieldType.getSimpleTypeName()));

            // for(RelatedFieldType result : results){
            bodyBuilder.appendFormalLine(String.format("for(%s result : %s){",
                relatedFieldType.getSimpleTypeName(), relatedFieldName.concat("Results")));
            bodyBuilder.indent();

            // relatedFieldList.add(result);
            bodyBuilder.appendFormalLine(String.format("%sList.add(result);", relatedFieldName));
            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

            // // Creating array adapter
            bodyBuilder.appendFormalLine("// Creating array adapter");

            // ArrayAdapter adapter = new ArrayAdapter(this,
            // android.R.layout.simple_list_item_1,
            // relatedFieldList);
            bodyBuilder
                .appendFormalLine(String
                    .format(
                        "%s %sAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, %sList);",
                        new JavaType("android.widget.ArrayAdapter").getNameIncludingTypeParameters(
                            false, importResolver), relatedFieldName, relatedFieldName));

            // // Setting adapter on spinner
            bodyBuilder.appendFormalLine("// Setting adapter on spinner");

            // fieldSpinner.setAdapter(adapter);
            bodyBuilder.appendFormalLine(String.format("%s.setAdapter(%sAdapter);", fieldName,
                relatedFieldName));

            bodyBuilder.appendFormalLine("");
            bodyBuilder.appendFormalLine("");

          }

        }
      }
    }

    // }catch (Exception e){
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}catch (Exception e){");
    bodyBuilder.indent();

    // e.printStackTrace();
    bodyBuilder.appendFormalLine("e.printStackTrace();");
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}");

  }

  /**
   * Method that generates disableFormElements FormActivity method
   * 
   * @return
   */
  private MethodMetadataBuilder getDisableFormElementsMethod() {
    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildDisableFormElementsMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName(
            "disableFormElements"), JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
            bodyBuilder);

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment = new JavadocComment("Method that disables all form items. \n");
    commentStructure.addComment(comment, CommentLocation.BEGINNING);
    methodBuilder.setCommentStructure(commentStructure);

    return methodBuilder; // Build and return a MethodMetadata
    // instance
  }

  /**
   * Generates disableFormElements FormActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildDisableFormElementsMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
    // // Disabling all form fields
    bodyBuilder.appendFormalLine("// Disabling all form fields");

    // Getting all defined fields
    for (FieldMetadata field : entityFields) {

      // Checking if current field is a valid Database Field
      AnnotationMetadata databaseFieldAnnotation =
          field.getAnnotation(new JavaType("com.j256.ormlite.field.DatabaseField"));
      if (databaseFieldAnnotation != null) {

        // Checking if field is a generatedId field
        AnnotationAttributeValue<Boolean> generatedIdAttr =
            databaseFieldAnnotation.getAttribute("generatedId");

        boolean generatedId = false;

        if (generatedIdAttr != null) {
          generatedId = generatedIdAttr.getValue();
        }

        if (!generatedId) {
          // Getting fieldName
          String fieldName = getFieldNameOnActivity(field);

          // Checking MapView elements
          if (isGeoField(field)) {
            String auxFieldName =
                Character.toLowerCase(field.getFieldName().getSymbolName().charAt(0))
                    + field.getFieldName().getSymbolName().substring(1).concat("EditText");
            bodyBuilder.appendFormalLine(String.format("%s.setEnabled(false);", auxFieldName));
          }

          bodyBuilder.appendFormalLine(String.format("%s.setEnabled(false);", fieldName));
        }
      }
    }
  }

  /**
   * Method that generates populateForm FormActivity method
   * 
   * @return
   */
  private MethodMetadataBuilder getPopulateFormMethod() {
    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
    parameterTypes.add(AnnotatedJavaType.convertFromJavaType(JavaType.INT_PRIMITIVE));

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
    parameterNames.add(new JavaSymbolName("id"));

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildPopulateFormMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName("populateForm"),
            JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment =
        new JavadocComment(String.format(
            "Method that populate form with selected %s id \n\n@param id",
            entity.getSimpleTypeName()));
    commentStructure.addComment(comment, CommentLocation.BEGINNING);
    methodBuilder.setCommentStructure(commentStructure);

    return methodBuilder; // Build and return a MethodMetadata
    // instance
  }

  /**
   * Generates populateForm FormActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildPopulateFormMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
    // // Getting entity object
    bodyBuilder.appendFormalLine("// Getting entity object");

    // try {
    bodyBuilder.appendFormalLine("try {");
    bodyBuilder.indent();

    // Dao<Entity, Integer> entityDao = getHelper().getEntityDao();
    bodyBuilder.appendFormalLine(String.format("%s<%s, Integer> %sDao = getHelper().get%sDao();",
        new JavaType("com.j256.ormlite.dao.Dao").getNameIncludingTypeParameters(false,
            importResolver), entity.getSimpleTypeName(), entity.getSimpleTypeName().toLowerCase(),
        entity.getSimpleTypeName()));

    // entity = entityDao.queryForId(id);
    bodyBuilder.appendFormalLine(String.format("%s = %sDao.queryForId(id);", entity
        .getSimpleTypeName().toLowerCase(), entity.getSimpleTypeName().toLowerCase()));

    // Getting all defined fields
    for (FieldMetadata field : entityFields) {

      // Checking if current field is a valid Database Field
      AnnotationMetadata databaseFieldAnnotation =
          field.getAnnotation(new JavaType("com.j256.ormlite.field.DatabaseField"));
      if (databaseFieldAnnotation != null) {

        // Checking if field is a generatedId field
        AnnotationAttributeValue<Boolean> generatedIdAttr =
            databaseFieldAnnotation.getAttribute("generatedId");

        boolean generatedId = false;

        if (generatedIdAttr != null) {
          generatedId = generatedIdAttr.getValue();
        }

        if (!generatedId) {
          // Getting fieldName
          String fieldName = getFieldNameOnActivity(field);
          JavaType fieldType = getFieldTypeOnActivity(field);
          // Getting accessor method
          MethodMetadata accessor = getAccessorMethod(field);

          // Checking Spinners
          if (fieldType.equals(new JavaType("android.widget.Spinner"))) {

            JavaType relatedFieldType = field.getFieldType();
            String relatedFieldName = relatedFieldType.getSimpleTypeName().toLowerCase();

            // ArrayAdapter fieldAdapter = (ArrayAdapter)
            // spinner.getAdapter();
            bodyBuilder.appendFormalLine(String.format(
                "ArrayAdapter %sAdapter = (ArrayAdapter) %s.getAdapter();", relatedFieldName,
                fieldName));

            // int relatedFieldPosition = -1;
            bodyBuilder.appendFormalLine(String.format("int %sPosition = -1;", relatedFieldName));

            // for(int i = 0; i < adapter.getCount(); i++){
            bodyBuilder.appendFormalLine(String.format(
                "for(int i = 0; i < %sAdapter.getCount(); i++){", relatedFieldName));
            bodyBuilder.indent();

            // RelatedField item = (RelatedField)
            // adapter.getItem(i);
            bodyBuilder.appendFormalLine(String.format("%s item = (%s) %sAdapter.getItem(i);",
                relatedFieldType.getSimpleTypeName(), relatedFieldType.getSimpleTypeName(),
                relatedFieldName));

            // if(item.getId().equals(entity.getField().getId())){
            bodyBuilder.appendFormalLine(String.format("if(item.getId().equals(%s.%s().getId())){",
                entity.getSimpleTypeName().toLowerCase(), accessor.getMethodName()));
            bodyBuilder.indent();

            // relatedFieldPosition = i;
            bodyBuilder.appendFormalLine(String.format("%sPosition = i;", relatedFieldName));
            bodyBuilder.appendFormalLine("break;");

            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

            // field.setSelection(relatedFieldPosition);
            bodyBuilder.appendFormalLine(String.format("%s.setSelection(%sPosition);", fieldName,
                relatedFieldName));

          } else if (isGeoField(field)) {
            // Checking if is a GEO field

            // // Populate map elements if exists
            bodyBuilder.appendFormalLine("// Populate map elements if exists");

            // if(accessorResult != null){
            bodyBuilder.appendFormalLine(String.format("if(%s.%s() != null){", entity
                .getSimpleTypeName().toLowerCase(), accessor.getMethodName()));
            bodyBuilder.indent();

            // ArrayList<OverlayItem> items = new
            // ArrayList<OverlayItem>();
            bodyBuilder.appendFormalLine(String.format(
                "%s<%s> items = new ArrayList<OverlayItem>();", new JavaType("java.util.ArrayList")
                    .getNameIncludingTypeParameters(false, importResolver), new JavaType(
                    "org.osmdroid.views.overlay.OverlayItem").getNameIncludingTypeParameters(false,
                    importResolver)));

            // // Adding items
            bodyBuilder.appendFormalLine("// Adding items");

            // items.add(new OverlayItem(entity.toString(), "",
            // accessor));
            bodyBuilder.appendFormalLine(String.format(
                "items.add(new OverlayItem(%s.toString(), \"\", %s.%s()));", entity
                    .getSimpleTypeName().toLowerCase(), entity.getSimpleTypeName().toLowerCase(),
                accessor.getMethodName()));

            // /* OnTapListener for the Markers, shows a simple
            // Toast. */
            bodyBuilder
                .appendFormalLine("/* OnTapListener for the Markers, shows a simple Toast. */");

            // ItemizedOverlay<OverlayItem> mMyLocationOverlay = new
            // ItemizedIconOverlay<OverlayItem>(items,
            bodyBuilder.appendFormalLine(String.format(
                "%s<OverlayItem> mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,",
                new JavaType("org.osmdroid.views.overlay.ItemizedIconOverlay")
                    .getNameIncludingTypeParameters(false, importResolver)));
            bodyBuilder.indent();

            // new
            // ItemizedIconOverlay.OnItemGestureListener<OverlayItem>()
            // {
            bodyBuilder.appendFormalLine(String
                .format("new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {"));
            bodyBuilder.indent();

            // @Override
            bodyBuilder.appendFormalLine("@Override");

            // public boolean onItemSingleTapUp(final int index,
            // final OverlayItem item) {
            bodyBuilder
                .appendFormalLine("public boolean onItemSingleTapUp(final int index, final OverlayItem item) {");
            bodyBuilder.indent();

            // return true; // We 'handled' this event.
            bodyBuilder.appendFormalLine("return true; // We 'handled' this event.");

            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

            // @Override
            bodyBuilder.appendFormalLine("@Override");

            // public boolean onItemLongPress(final int index, final
            // OverlayItem item) {
            bodyBuilder
                .appendFormalLine("public boolean onItemLongPress(final int index, final OverlayItem item) {");
            bodyBuilder.indent();

            // return false;
            bodyBuilder.appendFormalLine("return false;");

            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

            // }, new
            // DefaultResourceProxyImpl(getApplicationContext()));
            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine(String.format("}, new %s(getApplicationContext()));",
                new JavaType("org.osmdroid.DefaultResourceProxyImpl")
                    .getNameIncludingTypeParameters(false, importResolver)));

            bodyBuilder.indentRemove();

            // field.getOverlays().add(mMyLocationOverlay);
            bodyBuilder.appendFormalLine(String.format("%s.getOverlays().add(mMyLocationOverlay);",
                fieldName));

            // field.invalidate();
            bodyBuilder.appendFormalLine(String.format("%s.invalidate();", fieldName));

            // // Initial map position
            bodyBuilder.appendFormalLine("// Initial map position");

            // IMapController mapController = field.getController();
            bodyBuilder.appendFormalLine(String.format("%s mapController = %s.getController();",
                new JavaType("org.osmdroid.api.IMapController").getNameIncludingTypeParameters(
                    false, importResolver), fieldName));

            // mapController.setZoom(15);
            bodyBuilder.appendFormalLine("mapController.setZoom(15);");

            // mapController.setCenter(accessor);
            bodyBuilder.appendFormalLine(String.format("mapController.setCenter(%s.%s());", entity
                .getSimpleTypeName().toLowerCase(), accessor.getMethodName()));

            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

          } else if (fieldType.equals(new JavaType("android.widget.Switch"))) {
            // Check if is a boolean field

            // fieldName.setChecked(entity.getField());
            bodyBuilder.appendFormalLine(String.format("%s.setChecked(%s.%s());", fieldName, entity
                .getSimpleTypeName().toLowerCase(), accessor.getMethodName()));

          } else {
            // fieldName.setText(entity.getField());
            bodyBuilder.appendFormalLine(String.format("%s.setText(%s.%s());", fieldName, entity
                .getSimpleTypeName().toLowerCase(), accessor.getMethodName()));
          }

        }
      }
    }

    // }catch (Exception e){
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}catch (Exception e){");
    bodyBuilder.indent();

    // e.printStackTrace();
    bodyBuilder.appendFormalLine("e.printStackTrace();");
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}");

  }

  /**
   * Method that generates create FormActivity method
   * 
   * @return
   */
  private MethodMetadataBuilder getCreateMethod() {
    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildCreateMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName("create"),
            JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment =
        new JavadocComment(String.format("Method to create new %s item\n",
            entity.getSimpleTypeName()));
    commentStructure.addComment(comment, CommentLocation.BEGINNING);
    methodBuilder.setCommentStructure(commentStructure);

    return methodBuilder; // Build and return a MethodMetadata
    // instance
  }

  /**
   * Generates create FormActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildCreateMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
    // try {
    bodyBuilder.appendFormalLine("try {");
    bodyBuilder.indent();

    // Dao<Entity, Integer> entityDao = getHelper().getEntityDao();
    bodyBuilder.appendFormalLine(String.format("%s<%s, Integer> %sDao = getHelper().get%sDao();",
        new JavaType("com.j256.ormlite.dao.Dao").getNameIncludingTypeParameters(false,
            importResolver), entity.getSimpleTypeName(), entity.getSimpleTypeName().toLowerCase(),
        entity.getSimpleTypeName()));

    // entity = new Entity();
    bodyBuilder.appendFormalLine(String.format("%s = new %s();", entity.getSimpleTypeName()
        .toLowerCase(), entity.getSimpleTypeName()));

    // Getting all defined fields
    for (FieldMetadata field : entityFields) {

      // Checking if current field is a valid Database Field
      AnnotationMetadata databaseFieldAnnotation =
          field.getAnnotation(new JavaType("com.j256.ormlite.field.DatabaseField"));
      if (databaseFieldAnnotation != null) {

        // Checking if field is a generatedId field
        AnnotationAttributeValue<Boolean> generatedIdAttr =
            databaseFieldAnnotation.getAttribute("generatedId");

        boolean generatedId = false;

        if (generatedIdAttr != null) {
          generatedId = generatedIdAttr.getValue();
        }

        if (!generatedId) {
          // Getting fieldName
          String fieldName = getFieldNameOnActivity(field);
          JavaType fieldType = getFieldTypeOnActivity(field);

          // Getting mutator method
          MethodMetadataBuilder mutator =
              getMutatorMethod(field.getFieldName(), field.getFieldType());

          // Checking Spinners
          if (fieldType.equals(new JavaType("android.widget.Spinner"))) {

            JavaType relatedFieldType = field.getFieldType();

            // entity.setField((RelatedFieldType)
            // fieldName.getSelectedItem());
            bodyBuilder.appendFormalLine(String.format("%s.%s((%s) %s.getSelectedItem());", entity
                .getSimpleTypeName().toLowerCase(), mutator.getMethodName(), relatedFieldType
                .getSimpleTypeName(), fieldName));
          } else if (isNumericField(field)) {
            // entity.setField(fieldName.getText().toString());
            bodyBuilder.appendFormalLine(String.format(
                "%s.%s(Integer.parseInt(%s.getText().toString()));", entity.getSimpleTypeName()
                    .toLowerCase(), mutator.getMethodName(), fieldName));
          } else if (isGeoField(field)) {
            // Check if is GEO Field

            // if(field.getOverlays().size() > 0){
            bodyBuilder.appendFormalLine(String.format("if(%s.getOverlays().size() > 0){",
                fieldName));
            bodyBuilder.indent();

            // entity.setField((GeoPoint) field.getMapCenter());
            bodyBuilder.appendFormalLine(String.format("%s.%s((GeoPoint) %s.getMapCenter());",
                entity.getSimpleTypeName().toLowerCase(), mutator.getMethodName(), fieldName));

            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

          } else if (fieldType.equals(new JavaType("android.widget.Switch"))) {
            // Check if is a boolean field

            // entity.setField((boolean) field.isChecked());
            bodyBuilder.appendFormalLine(String.format("%s.%s((boolean) %s.isChecked());", entity
                .getSimpleTypeName().toLowerCase(), mutator.getMethodName(), fieldName));
          } else {
            // entity.setField(fieldName.getText().toString());
            bodyBuilder.appendFormalLine(String.format("%s.%s(%s.getText().toString());", entity
                .getSimpleTypeName().toLowerCase(), mutator.getMethodName(), fieldName));
          }

        }
      }
    }

    // entityDao.create(entity);
    bodyBuilder.appendFormalLine(String.format("%sDao.create(%s);", entity.getSimpleTypeName()
        .toLowerCase(), entity.getSimpleTypeName().toLowerCase()));

    // }catch (Exception e){
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}catch (Exception e){");
    bodyBuilder.indent();

    // e.printStackTrace();
    bodyBuilder.appendFormalLine("e.printStackTrace();");
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}");

  }

  /**
   * Method that generates create FormActivity method
   * 
   * @return
   */
  private MethodMetadataBuilder getUpdateMethod() {
    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildUpdateMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName("update"),
            JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment =
        new JavadocComment(String.format("Method to update selected %s item\n",
            entity.getSimpleTypeName()));
    commentStructure.addComment(comment, CommentLocation.BEGINNING);
    methodBuilder.setCommentStructure(commentStructure);

    return methodBuilder; // Build and return a MethodMetadata
    // instance
  }

  /**
   * Generates update FormActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildUpdateMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
    // try {
    bodyBuilder.appendFormalLine("try {");
    bodyBuilder.indent();

    // Dao<Entity, Integer> entityDao = getHelper().getEntityDao();
    bodyBuilder.appendFormalLine(String.format("%s<%s, Integer> %sDao = getHelper().get%sDao();",
        new JavaType("com.j256.ormlite.dao.Dao").getNameIncludingTypeParameters(false,
            importResolver), entity.getSimpleTypeName(), entity.getSimpleTypeName().toLowerCase(),
        entity.getSimpleTypeName()));

    // Getting all defined fields
    for (FieldMetadata field : entityFields) {

      // Checking if current field is a valid Database Field
      AnnotationMetadata databaseFieldAnnotation =
          field.getAnnotation(new JavaType("com.j256.ormlite.field.DatabaseField"));
      if (databaseFieldAnnotation != null) {

        // Checking if field is a generatedId field
        AnnotationAttributeValue<Boolean> generatedIdAttr =
            databaseFieldAnnotation.getAttribute("generatedId");

        boolean generatedId = false;

        if (generatedIdAttr != null) {
          generatedId = generatedIdAttr.getValue();
        }

        if (!generatedId) {
          // Getting fieldName
          String fieldName = getFieldNameOnActivity(field);
          JavaType fieldType = getFieldTypeOnActivity(field);

          // Getting mutator method
          MethodMetadataBuilder mutator =
              getMutatorMethod(field.getFieldName(), field.getFieldType());

          // Checking Spinners
          if (fieldType.equals(new JavaType("android.widget.Spinner"))) {

            JavaType relatedFieldType = field.getFieldType();

            // entity.setField((RelatedFieldType)
            // fieldName.getSelectedItem());
            bodyBuilder.appendFormalLine(String.format("%s.%s((%s) %s.getSelectedItem());", entity
                .getSimpleTypeName().toLowerCase(), mutator.getMethodName(), relatedFieldType
                .getSimpleTypeName(), fieldName));
          } else if (isNumericField(field)) {
            // entity.setField(fieldName.getText().toString());
            bodyBuilder.appendFormalLine(String.format(
                "%s.%s(Integer.parseInt(%s.getText().toString()));", entity.getSimpleTypeName()
                    .toLowerCase(), mutator.getMethodName(), fieldName));
          } else if (isGeoField(field)) {
            // Check if is GEO Field

            // if(field.getOverlays().size() > 0){
            bodyBuilder.appendFormalLine(String.format("if(%s.getOverlays().size() > 0){",
                fieldName));
            bodyBuilder.indent();

            // entity.setField((GeoPoint) field.getMapCenter());
            bodyBuilder.appendFormalLine(String.format("%s.%s((GeoPoint) %s.getMapCenter());",
                entity.getSimpleTypeName().toLowerCase(), mutator.getMethodName(), fieldName));

            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

          } else if (fieldType.equals(new JavaType("android.widget.Switch"))) {
            // Check if is a boolean field

            // entity.setField((boolean) field.isChecked());
            bodyBuilder.appendFormalLine(String.format("%s.%s((boolean) %s.isChecked());", entity
                .getSimpleTypeName().toLowerCase(), mutator.getMethodName(), fieldName));
          } else {
            // entity.setField(fieldName.getText().toString());
            bodyBuilder.appendFormalLine(String.format("%s.%s(%s.getText().toString());", entity
                .getSimpleTypeName().toLowerCase(), mutator.getMethodName(), fieldName));
          }

        }
      }
    }

    // entityDao.update(entity);
    bodyBuilder.appendFormalLine(String.format("%sDao.update(%s);", entity.getSimpleTypeName()
        .toLowerCase(), entity.getSimpleTypeName().toLowerCase()));

    // }catch (Exception e){
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}catch (Exception e){");
    bodyBuilder.indent();

    // e.printStackTrace();
    bodyBuilder.appendFormalLine("e.printStackTrace();");
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}");

  }

  /**
   * Method that generates onCreateOptionsMenu FormActivity method
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
   * Generates onCreateOptionsMenu FormActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildOnCreateOptionsMenuMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
    // // Inflate the menu; this adds items to the action bar if it is
    // present.
    bodyBuilder
        .appendFormalLine("// Inflate the menu; this adds items to the action bar if it is present.");

    // if(mode == null){
    bodyBuilder.appendFormalLine("if(mode == null){");
    bodyBuilder.indent();

    // getMenuInflater().inflate(R.menu.menu_form, menu);
    bodyBuilder.appendFormalLine("getMenuInflater().inflate(R.menu.menu_form, menu);");
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}");

    // return true;
    bodyBuilder.appendFormalLine("return true;");

  }

  /**
   * Method that generates onOptionsItemSelected FormActivity method
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
   * Generates onOptionsItemSelected FormActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildOnOptionsItemSelectedMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
    // switch (item.getItemId()) {
    bodyBuilder.appendFormalLine("switch (item.getItemId()) {");
    bodyBuilder.indent();

    // // Respond to the action bar's Up/Home button
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

    // case R.id.action_save:
    bodyBuilder.appendFormalLine("case R.id.action_save:");
    bodyBuilder.indent();

    // // Checks if there is a selected entity or is necessary to create a
    // new one
    bodyBuilder
        .appendFormalLine("// Checks if there is a selected entity or is necessary to create a new one");

    // if(entity != null){
    bodyBuilder.appendFormalLine(String.format("if(%s != null){", entity.getSimpleTypeName()
        .toLowerCase()));
    bodyBuilder.indent();

    // // Update existing Entity
    bodyBuilder
        .appendFormalLine(String.format("// Update existing %s", entity.getSimpleTypeName()));

    // update();
    bodyBuilder.appendFormalLine("update();");
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}else{");
    bodyBuilder.indent();

    // // Create newEntity
    bodyBuilder.appendFormalLine(String.format("// Create new %s", entity.getSimpleTypeName()));

    // create();
    bodyBuilder.appendFormalLine("create();");
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}");

    // Return to list
    bodyBuilder.appendFormalLine("// Return to list");

    // NavUtils.navigateUpFromSameTask(this);
    bodyBuilder.appendFormalLine("NavUtils.navigateUpFromSameTask(this);");

    // return true;
    bodyBuilder.appendFormalLine("return true;");
    bodyBuilder.indentRemove();
    bodyBuilder.indentRemove();
    bodyBuilder.appendFormalLine("}");

    // return super.onOptionsItemSelected(item);
    bodyBuilder.appendFormalLine("return super.onOptionsItemSelected(item);");

  }

  /**
   * Method to add all necessary fields to FormActivity .aj file
   */
  private void addFormActivityFields() {
    FieldMetadataBuilder adapterField =
        new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName(entity
            .getSimpleTypeName().toLowerCase()), entity, null);
    builder.addField(adapterField);

    boolean hasGeoField = false;

    // Adding fields on current form activity
    for (FieldMetadata field : entityFields) {

      // Checking if current field is a valid Database Field
      AnnotationMetadata databaseFieldAnnotation =
          field.getAnnotation(new JavaType("com.j256.ormlite.field.DatabaseField"));
      if (databaseFieldAnnotation != null) {

        // Checking if field is a generatedId field
        AnnotationAttributeValue<Boolean> generatedIdAttr =
            databaseFieldAnnotation.getAttribute("generatedId");

        boolean generatedId = false;

        if (generatedIdAttr != null) {
          generatedId = generatedIdAttr.getValue();
        }

        if (!generatedId) {

          JavaType fieldType = field.getFieldType();
          String fieldName = getFieldNameOnActivity(field);
          JavaType formFieldType = getFieldTypeOnActivity(field);
          String fieldViewType = getFieldViewTypeOnActivity(field);

          FieldMetadataBuilder entityField =
              new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName(fieldName),
                  formFieldType, null);
          builder.addField(entityField);

          // Saving fieldNameLayout that will be used on findViewById
          // method
          fieldNameLayout.put(
              fieldName,
              entity.getSimpleTypeName().toLowerCase().concat("_")
                  .concat(field.getFieldName().getSymbolName().toLowerCase()).concat("_")
                  .concat(fieldViewType));

          // If is a GEO field, is necessary to add an Edit Text
          // to make some geo search
          if (isGeoField(field)) {
            fieldName =
                Character.toLowerCase(field.getFieldName().getSymbolName().charAt(0))
                    + field.getFieldName().getSymbolName().substring(1).concat("EditText");

            FieldMetadataBuilder geoField =
                new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName(fieldName),
                    new JavaType("android.widget.EditText"), null);
            builder.addField(geoField);
            hasGeoField = true;
          }

          // If is a referenced field, add ArrayList to include
          // results
          if (isReferencedField(field)) {
            fieldName = fieldType.getSimpleTypeName().toLowerCase().concat("List");

            FieldMetadataBuilder relatedField =
                new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName(fieldName),
                    new JavaType("java.util.ArrayList", 0, DataType.TYPE, null,
                        Arrays.asList(fieldType)), null);
            builder.addField(relatedField);
          }

        }
      }

    }

    FieldMetadataBuilder modeField =
        new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName("mode"),
            JavaType.STRING, null);
    builder.addField(modeField);

    // Check if exists some geo field
    if (hasGeoField) {
      FieldMetadataBuilder geoSearchHelper =
          new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName(
              "addressSearchHelper"), new JavaType("io.androoid.geo.search.AddressSearchHelper"),
              null);

      builder.addField(geoSearchHelper);
    }

  }

  /**
   * Method that generates processFinish FormActivity method
   * 
   * @return
   */
  private MethodMetadataBuilder getProcessFinishMethod() {
    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
    parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
        "org.osmdroid.util.GeoPoint")));

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
    parameterNames.add(new JavaSymbolName("output"));

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    buildProcessFinishMethodBody(bodyBuilder);

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, new JavaSymbolName(
            "processFinish".concat(entity.getSimpleTypeName())), JavaType.VOID_PRIMITIVE,
            parameterTypes, parameterNames, bodyBuilder);

    // Including comments
    CommentStructure commentStructure = new CommentStructure();
    JavadocComment comment =
        new JavadocComment("Method that will be executed before AsyncGeoTasks\n\n@param output\n");
    commentStructure.addComment(comment, CommentLocation.BEGINNING);
    methodBuilder.setCommentStructure(commentStructure);

    return methodBuilder; // Build and return a MethodMetadata
    // instance
  }

  /**
   * Generates processFinish FormActivity method body
   * 
   * @param bodyBuilder
   */
  private void buildProcessFinishMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
    // Adding fields on current form activity
    for (FieldMetadata field : entityFields) {

      // Checking if current field is a valid Database Field
      AnnotationMetadata databaseFieldAnnotation =
          field.getAnnotation(new JavaType("com.j256.ormlite.field.DatabaseField"));
      if (databaseFieldAnnotation != null) {

        // Checking if field is a generatedId field
        AnnotationAttributeValue<Boolean> generatedIdAttr =
            databaseFieldAnnotation.getAttribute("generatedId");

        boolean generatedId = false;

        if (generatedIdAttr != null) {
          generatedId = generatedIdAttr.getValue();
        }

        // Check if is GEO field
        if (!generatedId && isGeoField(field)) {
          String fieldName = getFieldNameOnActivity(field);
          String textFieldName = fieldName.replaceFirst("MapView", "EditText");

          // field.getOverlays().clear();
          bodyBuilder.appendFormalLine(String.format("%s.getOverlays().clear();", fieldName));

          // if(output == null){
          bodyBuilder.appendFormalLine("if(output == null){");
          bodyBuilder.indent();

          // fieldText.setBackgroundColor(Color.parseColor("#ff9090"));
          bodyBuilder.appendFormalLine(String.format(
              "%s.setBackgroundColor(%s.parseColor(\"#ff9090\"));", textFieldName, new JavaType(
                  "android.graphics.Color").getNameIncludingTypeParameters(false, importResolver)));

          // return;
          bodyBuilder.appendFormalLine("return;");
          bodyBuilder.indentRemove();

          // }else{
          bodyBuilder.appendFormalLine("}else{");
          bodyBuilder.indent();

          // fieldText.setBackgroundColor(Color.WHITE);
          bodyBuilder.appendFormalLine(String.format("%s.setBackgroundColor(Color.WHITE);",
              textFieldName));

          bodyBuilder.indentRemove();
          bodyBuilder.appendFormalLine("}");

          // ArrayList<OverlayItem> items = new
          // ArrayList<OverlayItem>();
          bodyBuilder
              .appendFormalLine(" ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();");

          // // Adding items
          bodyBuilder.appendFormalLine("// Adding items");

          // items.add(new OverlayItem("", "", output));
          bodyBuilder.appendFormalLine("items.add(new OverlayItem(\"\", \"\", output));");

          // /* OnTapListener for the Markers, shows a simple
          // Toast. */
          bodyBuilder
              .appendFormalLine("/* OnTapListener for the Markers, shows a simple Toast. */");

          // ItemizedOverlay<OverlayItem> mMyLocationOverlay = new
          // ItemizedIconOverlay<OverlayItem>(items,
          bodyBuilder.appendFormalLine(String.format(
              "%s<OverlayItem> mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,",
              new JavaType("org.osmdroid.views.overlay.ItemizedIconOverlay")
                  .getNameIncludingTypeParameters(false, importResolver)));
          bodyBuilder.indent();

          // new
          // ItemizedIconOverlay.OnItemGestureListener<OverlayItem>()
          // {
          bodyBuilder.appendFormalLine(String
              .format("new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {"));
          bodyBuilder.indent();

          // @Override
          bodyBuilder.appendFormalLine("@Override");

          // public boolean onItemSingleTapUp(final int index,
          // final OverlayItem item) {
          bodyBuilder
              .appendFormalLine("public boolean onItemSingleTapUp(final int index, final OverlayItem item) {");
          bodyBuilder.indent();

          // return true; // We 'handled' this event.
          bodyBuilder.appendFormalLine("return true; // We 'handled' this event.");

          bodyBuilder.indentRemove();
          bodyBuilder.appendFormalLine("}");

          // @Override
          bodyBuilder.appendFormalLine("@Override");

          // public boolean onItemLongPress(final int index, final
          // OverlayItem item) {
          bodyBuilder
              .appendFormalLine("public boolean onItemLongPress(final int index, final OverlayItem item) {");
          bodyBuilder.indent();

          // return false;
          bodyBuilder.appendFormalLine("return false;");

          bodyBuilder.indentRemove();
          bodyBuilder.appendFormalLine("}");

          // }, new
          // DefaultResourceProxyImpl(getApplicationContext()));
          bodyBuilder.indentRemove();
          bodyBuilder.appendFormalLine(String.format("}, new %s(getApplicationContext()));",
              new JavaType("org.osmdroid.DefaultResourceProxyImpl").getNameIncludingTypeParameters(
                  false, importResolver)));

          bodyBuilder.indentRemove();

          // field.getOverlays().add(mMyLocationOverlay);
          bodyBuilder.appendFormalLine(String.format("%s.getOverlays().add(mMyLocationOverlay);",
              fieldName));

          // field.invalidate();
          bodyBuilder.appendFormalLine(String.format("%s.invalidate();", fieldName));

          // // Initial map position
          bodyBuilder.appendFormalLine("// Initial map position");

          // IMapController mapController = field.getController();
          bodyBuilder.appendFormalLine(String.format("%s mapController = %s.getController();",
              new JavaType("org.osmdroid.api.IMapController").getNameIncludingTypeParameters(false,
                  importResolver), fieldName));

          // mapController.setZoom(15);
          bodyBuilder.appendFormalLine("mapController.setZoom(15);");

          // mapController.setCenter(output);
          bodyBuilder.appendFormalLine("mapController.setCenter(output);");

        }
      }
    }
  }

  /**
   * Method that returns field type declared on an activity Java file using
   * current entity FieldMetadata
   * 
   * @param field
   *            FieldMetadata that contains all necessary information to
   *            obtain field type declared on an activity java file
   * @return JavaType that contains declared field type
   */
  public JavaType getFieldTypeOnActivity(FieldMetadata field) {
    // Getting field type
    JavaType fieldType = field.getFieldType();
    JavaType formFieldType = null;

    if (fieldType.equals(JavaType.BOOLEAN_PRIMITIVE) || fieldType.equals(JavaType.BOOLEAN_OBJECT)) {
      formFieldType = new JavaType("android.widget.Switch");
    } else if (fieldType.equals(new JavaType("org.osmdroid.util.GeoPoint"))) {
      formFieldType = new JavaType("org.osmdroid.views.MapView");
    } else if (isReferencedField(field)) {
      formFieldType = new JavaType("android.widget.Spinner");
    } else {
      formFieldType = new JavaType("android.widget.EditText");
    }

    return formFieldType;
  }

  /**
   * Method that returns field name declared on an activity Java file using
   * current entity FieldMetadata.
   * 
   * @param field
   *            FieldMetadata that contains all necessary information to
   *            obtain field name declared on an activity Java file
   * @return String that contains field name declared.
   */
  public String getFieldNameOnActivity(FieldMetadata field) {

    // Getting field type on activity
    JavaType formFieldType = getFieldTypeOnActivity(field);

    // Getting fieldName
    String fieldName =
        Character.toLowerCase(field.getFieldName().getSymbolName().charAt(0))
            + field.getFieldName().getSymbolName().substring(1)
                .concat(formFieldType.getSimpleTypeName());

    return fieldName;
  }

  /**
   * Method that returns field view type declared on an activity Java file
   * using current entity FieldMetadata.
   * 
   * @param field
   *            FieldMetadata that contains all necessary information to
   *            obtain field view type declared on an activity Java file
   * @return String that contains field view type.
   */
  public String getFieldViewTypeOnActivity(FieldMetadata field) {
    // Getting field type
    JavaType fieldType = field.getFieldType();
    String fieldViewType = "";

    if (fieldType.equals(JavaType.BOOLEAN_PRIMITIVE) || fieldType.equals(JavaType.BOOLEAN_OBJECT)) {
      fieldViewType = "switch";
    } else if (fieldType.equals(new JavaType("org.osmdroid.util.GeoPoint"))) {
      fieldViewType = "mapview";
    } else if (isReferencedField(field)) {
      fieldViewType = "spinner";
    } else {
      fieldViewType = "text";
    }

    return fieldViewType;
  }

  /**
   * Method to check if provided field has @AndrooidReferencedField annotation
   * 
   * @param field
   *            FieldMetadata with the field to check
   * 
   * @return true if is annotated with @AndrooidReferencedField
   */
  public boolean isReferencedField(FieldMetadata field) {
    AnnotationMetadata annotation =
        field.getAnnotation(new JavaType(
            "io.androoid.roo.addon.suite.addon.fields.annotations.AndrooidReferencedField"));
    return annotation != null;
  }

  /**
   * Method to check if provided field is GeoField
   * 
   * @param field
   *            FieldMetadata with the field to check
   * 
   * @return true if is a valid GeoField
   */
  public boolean isGeoField(FieldMetadata field) {
    // Getting field type
    JavaType fieldType = field.getFieldType();
    if (fieldType.equals(new JavaType("org.osmdroid.util.GeoPoint"))) {
      return true;
    }

    return false;
  }

  /**
   * Method to check if provided field is numeric field
   * 
   * @param field
   *            FieldMetadata with the field to check
   * 
   * @return true if is a valid number field
   */
  public boolean isNumericField(FieldMetadata field) {

    JavaType fieldType = field.getFieldType();

    if (fieldType.equals(JavaType.INT_PRIMITIVE) || fieldType.equals(JavaType.INT_OBJECT)) {
      return true;
    }

    return false;
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
