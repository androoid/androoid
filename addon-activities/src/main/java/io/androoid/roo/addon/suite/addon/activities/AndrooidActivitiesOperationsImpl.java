package io.androoid.roo.addon.suite.addon.activities;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.comments.CommentStructure;
import org.springframework.roo.classpath.details.comments.CommentStructure.CommentLocation;
import org.springframework.roo.classpath.details.comments.JavadocComment;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidActionBarCallback;
import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidFormActivity;
import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidListActivity;
import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidMainActivity;
import io.androoid.roo.addon.suite.addon.entities.annotations.AndrooidEntity;
import io.androoid.roo.addon.suite.addon.manifest.manager.AndrooidManifestOperations;
import io.androoid.roo.addon.suite.support.AndrooidUtils;

/**
 * Implementation of {@link AndrooidActivitiesOperations} interface.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidActivitiesOperationsImpl implements AndrooidActivitiesOperations {

  /**
   * Get hold of a JDK Logger
   */
  private Logger LOGGER = Logger.getLogger(getClass().getName());

  private ComponentContext cContext;

  @Reference
  private FileManager fileManager;
  @Reference
  private ProjectOperations projectOperations;
  @Reference
  private PathResolver pathResolver;
  @Reference
  private AndrooidUtils operationsUtils;
  @Reference
  private AndrooidManifestOperations manifestOperations;
  @Reference
  private TypeLocationService typeLocationService;
  @Reference
  private TypeManagementService typeManagementService;

  protected void activate(final ComponentContext componentContext) {
    cContext = componentContext;
  }

  /** {@inheritDoc} */
  public boolean isSetupAvailable() {
    return projectOperations.isFeatureInstalled("androoid-project");
  }

  /** {@inheritDoc} */
  public void setup() {
    // Include androoid dependencies
    installDependencies();

    // Including basic files
    addBasicFiles(projectOperations.getFocusedTopLevelPackage());

    // Including utilities
    addActionBarCallbackUtility(projectOperations.getFocusedTopLevelPackage());

    // Update AndroidManifest.xml with basic configuration
    Map<String, String> attributes = new HashMap<String, String>();
    attributes.put("android:icon", "@mipmap/app_icon");
    attributes.put("android:label", "@string/app_name");
    attributes.put("android:theme", "@style/AppTheme");
    manifestOperations.addApplicationConfig(attributes);

    // Update AndroidManifest.xml file with basic permissions
    List<String> permissionsNames = new ArrayList<String>();
    permissionsNames.add("android.permission.ACCESS_COARSE_LOCATION");
    permissionsNames.add("android.permission.ACCESS_FINE_LOCATION");
    permissionsNames.add("android.permission.ACCESS_WIFI_STATE");
    permissionsNames.add("android.permission.ACCESS_NETWORK_STATE");
    permissionsNames.add("android.permission.INTERNET");
    permissionsNames.add("android.permission.WRITE_EXTERNAL_STORAGE");
    manifestOperations.addPermissions(permissionsNames);

    // Generates Main Activity by default.
    generatesMainActivity();

  }

  /**
   * Method that includes ActionBarCallback utility
   * 
   * @param projectPackage
   *            JavaPackage that indicates generated project package
   * 
   */
  private void addActionBarCallbackUtility(JavaPackage projectPackage) {

    // Generate ActionBarCallback
    JavaType actionBarCallback =
        new JavaType(projectPackage.getFullyQualifiedPackageName().concat(
            ".utils.ActionBarCallback"));

    int modifier = Modifier.PUBLIC;
    final String declaredByMetadataId =
        PhysicalTypeIdentifier.createIdentifier(actionBarCallback,
            pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));

    File targetFile =
        new File(typeLocationService.getPhysicalTypeCanonicalPath(declaredByMetadataId));
    Validate.isTrue(!targetFile.exists(), "Type '%s' already exists", actionBarCallback);

    // Prepare class builder
    final ClassOrInterfaceTypeDetailsBuilder cidBuilder =
        new ClassOrInterfaceTypeDetailsBuilder(declaredByMetadataId, modifier, actionBarCallback,
            PhysicalTypeCategory.CLASS);

    // Including @AndrooidActionBarCallback annotation
    cidBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType(
        AndrooidActionBarCallback.class)));

    // Including implements ActionMode.Callback
    cidBuilder.addImplementsType(new JavaType("android.view.ActionMode.Callback").getBaseType());

    // Including necessary methods
    cidBuilder.addMethod(getOnCreateActionModeMethod(declaredByMetadataId));
    cidBuilder.addMethod(getOnPrepareActionModeMethod(declaredByMetadataId));
    cidBuilder.addMethod(getOnActionItemClickedMethod(declaredByMetadataId));
    cidBuilder.addMethod(getOnDestroyActionModeMethod(declaredByMetadataId));

    typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

  }

  /**
   * Method that generates onCreateActionMode method
   * 
   * @param declaredByMetadataId
   * 
   * @return MethodMetadata that will be added to builder
   */
  private MethodMetadata getOnCreateActionModeMethod(String declaredByMetadataId) {

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
    bodyBuilder.appendFormalLine("return onCreateAndrooidActionMode(mode, menu);");

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(declaredByMetadataId, Modifier.PUBLIC, new JavaSymbolName(
            "onCreateActionMode"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
            bodyBuilder);
    methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

    return methodBuilder.build(); // Build and return a MethodMetadata
    // instance

  }

  /**
   * Method that generates onPrepareActionMode method
   * 
   * @param declaredByMetadataId
   * 
   * @return MethodMetadata that will be added to builder
   */
  private MethodMetadata getOnPrepareActionModeMethod(String declaredByMetadataId) {

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
    bodyBuilder.appendFormalLine("return onPrepareAndrooidActionMode(mode, menu);");

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(declaredByMetadataId, Modifier.PUBLIC, new JavaSymbolName(
            "onPrepareActionMode"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
            bodyBuilder);
    methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

    return methodBuilder.build(); // Build and return a MethodMetadata
    // instance

  }

  /**
   * Method that generates onActionItemClicked method
   * 
   * @param declaredByMetadataId
   * 
   * @return MethodMetadata that will be added to builder
   */
  private MethodMetadata getOnActionItemClickedMethod(String declaredByMetadataId) {

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
    bodyBuilder.appendFormalLine("return onAndrooidActionItemClicked(mode, item);");

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(declaredByMetadataId, Modifier.PUBLIC, new JavaSymbolName(
            "onActionItemClicked"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
            bodyBuilder);
    methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

    return methodBuilder.build(); // Build and return a MethodMetadata
    // instance

  }

  /**
   * Method that generates onDestroyActionMode method
   * 
   * @param declaredByMetadataId
   * 
   * @return MethodMetadata that will be added to builder
   */
  private MethodMetadata getOnDestroyActionModeMethod(String declaredByMetadataId) {

    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
    parameterTypes.add(AnnotatedJavaType
        .convertFromJavaType(new JavaType("android.view.ActionMode")));

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
    parameterNames.add(new JavaSymbolName("mode"));

    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
    bodyBuilder.appendFormalLine("onAndrooidDestroyActionMode(mode);");

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(declaredByMetadataId, Modifier.PUBLIC, new JavaSymbolName(
            "onDestroyActionMode"), JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
            bodyBuilder);
    methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

    return methodBuilder.build(); // Build and return a MethodMetadata
    // instance

  }

  /** {@inheritDoc} */
  public void add(JavaType entity) {

    // Checks if current entity is annotated with @AndrooidEntity
    ClassOrInterfaceTypeDetails entityDetails = typeLocationService.getTypeDetails(entity);
    AnnotationMetadata androoidEntityAnnotation =
        entityDetails.getAnnotation(new JavaType(AndrooidEntity.class));

    Validate.notNull(androoidEntityAnnotation, String.format(
        "ERROR: Provided entity %s is not annotated with @AndrooidEntity. "
            + "Only Androoid Entity classes could be used to generate new Androoid Activities.",
        entity.getSimpleTypeName()));

    // Generate new List activity
    addListActivity(entity);

    // Generate new Form activity
    addFormActivity(entity);

    // Add new activity button to main view
    // addActivityToMainView(entity);
  }

  /**
   * This method creates new AndrooidListActivity related with an specified
   * entity that will allow users to list data about the specified entity.
   * 
   * @param entity
   *            JavaType that will be used to generate the related activity
   */
  private void addListActivity(JavaType entity) {

    // Creates new activity JavaType
    String entityName = entity.getSimpleTypeName();
    String listActivityName =
        projectOperations.getFocusedTopLevelPackage().getFullyQualifiedPackageName()
            .concat(".activities.").concat(entityName.toLowerCase()).concat(".").concat(entityName)
            .concat("ListActivity");
    JavaType listActivity = new JavaType(listActivityName);

    int modifier = Modifier.PUBLIC;
    final String declaredByMetadataId =
        PhysicalTypeIdentifier.createIdentifier(listActivity,
            pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));

    File targetFile =
        new File(typeLocationService.getPhysicalTypeCanonicalPath(declaredByMetadataId));
    Validate.isTrue(!targetFile.exists(), "Type '%s' already exists", listActivity);

    // Prepare class builder
    final ClassOrInterfaceTypeDetailsBuilder cidBuilder =
        new ClassOrInterfaceTypeDetailsBuilder(declaredByMetadataId, modifier, listActivity,
            PhysicalTypeCategory.CLASS);

    // Including @AndrooidListActivity annotation
    AnnotationMetadataBuilder listActivityAnnotation =
        new AnnotationMetadataBuilder(new JavaType(AndrooidListActivity.class));
    listActivityAnnotation.addClassAttribute("entity", entity);
    cidBuilder.addAnnotation(listActivityAnnotation);

    // AndrooidActivityList extends OrmLiteBaseListActivity<DatabaseHelper>
    JavaType extendsType =
        new JavaType("com.j256.ormlite.android.apptools.OrmLiteBaseListActivity", 0, DataType.TYPE,
            null, Arrays.asList(new JavaType(projectOperations.getFocusedTopLevelPackage()
                .getFullyQualifiedPackageName().concat(".utils.DatabaseHelper"))));
    cidBuilder.addExtendsTypes(extendsType);

    // AndrooidActivityList implements AbsListView.MultiChoiceModeListener
    // and AdapterView.OnItemClickListener
    cidBuilder.addImplementsType(new JavaType("android.widget.AbsListView.MultiChoiceModeListener")
        .getBaseType());
    cidBuilder.addImplementsType(new JavaType("android.widget.AdapterView.OnItemClickListener")
        .getBaseType());

    // Add necessary @Override methods that will invoke .aj methods that
    // will implements necessary code
    cidBuilder.addMethod(getOnItemCheckedStateChangedMethod(declaredByMetadataId, entity));
    cidBuilder.addMethod(getOnCreateActionModeMethod(declaredByMetadataId, entity));
    cidBuilder.addMethod(getOnPrepareActionModeMethod(declaredByMetadataId, entity));
    cidBuilder.addMethod(getOnActionItemClickedMethod(declaredByMetadataId, entity));
    cidBuilder.addMethod(getOnDestroyActionModeMethod(declaredByMetadataId, entity));
    cidBuilder.addMethod(getOnItemClickMethod(declaredByMetadataId, entity));

    typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

    // Add ListActivity to AndroidManifest.xml
    Element listActivityElement =
        manifestOperations.addActivity(listActivityName,
            String.format("@string/title_activity_%s", entityName.toLowerCase()), "orientation",
            "portrait");
    manifestOperations.addMetadataToActivity(listActivityElement,
        "android.support.PARENT_ACTIVITY", ".activities.MainActivity");

    // Include entity_list_activity.xml view file
    final InputStream templateInputStream =
        FileUtils.getInputStream(getClass(), "layout/list_activity.xml");

    // Include all necessary labels on strings.xml file
    addLabel(String.format("title_activity_%s", entityName.toLowerCase()),
        entity.getSimpleTypeName());

    final Document listFile = XmlUtils.readXml(templateInputStream);
    final Element root = listFile.getDocumentElement();
    root.setAttribute("tools:context", listActivityName);

    String xmlViewPath =
        pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/layout/"
            .concat(entity.getSimpleTypeName().toLowerCase()).concat("_list_activity.xml"));

    final MutableFile listActivityMutableFile = fileManager.createFile(xmlViewPath);

    XmlUtils.writeXml(listActivityMutableFile.getOutputStream(), listFile);

  }

  /**
   * This method creates new AndrooidFormActivity related with an specified
   * entity that will allow users to manage data about the specified entity.
   * 
   * @param entity
   *            JavaType that will be used to generate the related activity
   */
  private void addFormActivity(JavaType entity) {

    // Creates new activity JavaType
    String entityName = entity.getSimpleTypeName();
    String formActivityName =
        projectOperations.getFocusedTopLevelPackage().getFullyQualifiedPackageName()
            .concat(".activities.").concat(entityName.toLowerCase()).concat(".").concat(entityName)
            .concat("FormActivity");
    JavaType formActivity = new JavaType(formActivityName);

    int modifier = Modifier.PUBLIC;
    final String declaredByMetadataId =
        PhysicalTypeIdentifier.createIdentifier(formActivity,
            pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));

    File targetFile =
        new File(typeLocationService.getPhysicalTypeCanonicalPath(declaredByMetadataId));
    Validate.isTrue(!targetFile.exists(), "Type '%s' already exists", formActivity);

    // Prepare class builder
    final ClassOrInterfaceTypeDetailsBuilder cidBuilder =
        new ClassOrInterfaceTypeDetailsBuilder(declaredByMetadataId, modifier, formActivity,
            PhysicalTypeCategory.CLASS);

    // Including @AndrooidFormActivity annotation
    AnnotationMetadataBuilder formActivityAnnotation =
        new AnnotationMetadataBuilder(new JavaType(AndrooidFormActivity.class));
    formActivityAnnotation.addClassAttribute("entity", entity);
    cidBuilder.addAnnotation(formActivityAnnotation);

    // AndrooidActivityList extends OrmLiteBaseListActivity<DatabaseHelper>
    JavaType extendsType =
        new JavaType("com.j256.ormlite.android.apptools.OrmLiteBaseActivity", 0, DataType.TYPE,
            null, Arrays.asList(new JavaType(projectOperations.getFocusedTopLevelPackage()
                .getFullyQualifiedPackageName().concat(".utils.DatabaseHelper"))));
    cidBuilder.addExtendsTypes(extendsType);

    // Add implements AsyncGeoResponse if has some Geo Field
    if (hasGeoField(entity)) {
      cidBuilder.addImplementsType(new JavaType("io.androoid.geo.search.AsyncGeoResponse"));

      // Adding processFinish method
      cidBuilder.addMethod(getProcessFinishMethod(declaredByMetadataId, entity));
    }

    typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

    // Add FormActivity to AndroidManifest.xml
    String listActivityName =
        projectOperations.getFocusedTopLevelPackage().getFullyQualifiedPackageName()
            .concat(".activities.").concat(entityName.toLowerCase()).concat(".").concat(entityName)
            .concat("ListActivity");
    Element formActivityElement =
        manifestOperations.addActivity(formActivityName,
            String.format("@string/title_activity_%s_form", entityName.toLowerCase()),
            "orientation", "portrait");
    manifestOperations.addMetadataToActivity(formActivityElement,
        "android.support.PARENT_ACTIVITY", listActivityName);

    // Include all necessary labels on strings.xml file
    addLabel(String.format("title_activity_%s_form", entityName.toLowerCase()),
        entity.getSimpleTypeName());

    // Include entity_form_activity.xml view file
    final InputStream templateInputStream =
        FileUtils.getInputStream(getClass(), "layout/form_activity.xml");

    final Document formFile = XmlUtils.readXml(templateInputStream);
    final Element root = formFile.getDocumentElement();
    root.setAttribute("tools:context", formActivityName);

    // Including all necessary fields on form view
    Set<ClassOrInterfaceTypeDetails> allEntities =
        typeLocationService.findClassesOrInterfaceDetailsWithAnnotation(new JavaType(
            AndrooidEntity.class));
    for (ClassOrInterfaceTypeDetails definedEntity : allEntities) {
      // Getting current entity
      if (definedEntity.getType().equals(entity)) {

        // Define list where all necessary labels will be saved
        Map<FieldMetadata, String> labelsToAdd = new HashMap<FieldMetadata, String>();

        // Getting fields
        List<? extends FieldMetadata> entityFields =
            definedEntity.getFieldsWithAnnotation(new JavaType(
                "com.j256.ormlite.field.DatabaseField"));

        for (FieldMetadata field : entityFields) {

          AnnotationMetadata databaseFieldAnnotation =
              field.getAnnotation(new JavaType("com.j256.ormlite.field.DatabaseField"));

          // Checking if field is a generatedId field
          AnnotationAttributeValue<Boolean> generatedIdAttr =
              databaseFieldAnnotation.getAttribute("generatedId");

          boolean generatedId = false;

          if (generatedIdAttr != null) {
            generatedId = generatedIdAttr.getValue();
          }

          if (!generatedId) {
            String fieldViewType = getFieldViewTypeOnActivity(field);
            String fieldViewLabel =
                entity.getSimpleTypeName().toLowerCase().concat("_")
                    .concat(field.getFieldName().getSymbolName().toLowerCase());
            String fieldViewName =
                entity.getSimpleTypeName().toLowerCase().concat("_")
                    .concat(field.getFieldName().getSymbolName().toLowerCase()).concat("_")
                    .concat(fieldViewType);

            // Creating label
            Element labelElement = formFile.createElement("TextView");
            labelElement.setAttribute("android:layout_width", "wrap_content");
            labelElement.setAttribute("android:layout_height", "wrap_content");
            labelElement.setAttribute("android:text", String.format("@string/%s", fieldViewLabel));
            labelElement.setAttribute("android:id", String.format("@+id/%s", fieldViewLabel));
            labelElement.setAttribute("android:layout_alignStart",
                String.format("@+id/%s", fieldViewName));
            labelElement.setAttribute("android:layout_alignParentStart", "true");
            root.appendChild(labelElement);

            // Creating value of label above
            addLabel(fieldViewLabel, field.getFieldName().getReadableSymbolName());

            // Creating element depending of its type
            if (fieldViewType.equals("text")) {
              Element editTextElement = formFile.createElement("EditText");
              editTextElement.setAttribute("android:layout_width", "fill_parent");
              editTextElement.setAttribute("android:layout_height", "wrap_content");
              editTextElement.setAttribute("android:inputType", "text");
              editTextElement.setAttribute("android:ems", "10");
              editTextElement.setAttribute("android:id", String.format("@+id/%s", fieldViewName));
              editTextElement.setAttribute("android:layout_alignParentTop", "true");
              editTextElement.setAttribute("android:layout_alignParentStart", "true");
              editTextElement.setAttribute("android:layout_marginTop", "10dp");
              editTextElement.setAttribute("android:layout_alignParentEnd", "false");
              root.appendChild(editTextElement);
            }

          }

        }

        break;
      }
    }

    String xmlViewPath =
        pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/layout/"
            .concat(entity.getSimpleTypeName().toLowerCase()).concat("_form_activity.xml"));

    final MutableFile formActivityMutableFile = fileManager.createFile(xmlViewPath);

    XmlUtils.writeXml(formActivityMutableFile.getOutputStream(), formFile);
  }

  /**
   * This method will include all necessary labels inside strings.xml file for
   * the provided entity
   * 
   * @param title title of the label to add
   * @param value the value of the label
   */
  private void addLabel(String title, String value) {

    final InputStream templateInputStream = getStringsMutableFile().getInputStream();

    final Document stringsFile = XmlUtils.readXml(templateInputStream);
    final Element root = stringsFile.getDocumentElement();

    Element stringElement = stringsFile.createElement("string");
    stringElement.setAttribute("name", title);
    stringElement.setTextContent(value);
    root.appendChild(stringElement);

    XmlUtils.writeXml(getStringsMutableFile().getOutputStream(), stringsFile);

  }

  /**
   * Method that generates onItemCheckedStateChanged ListActivity method
   * 
   * @param declaredByMetadataId
   * @param entity
   *            JavaType with the current entity to use
   * 
   * @return
   */
  private MethodMetadataBuilder getOnItemCheckedStateChangedMethod(String declaredByMetadataId,
      JavaType entity) {
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

    // Invoke .aj method with necessary implementation
    bodyBuilder.appendFormalLine(String.format(
        "onCheckedStateChanged%s(mode, position, id, checked);", entity.getSimpleTypeName()));

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(declaredByMetadataId, Modifier.PUBLIC, new JavaSymbolName(
            "onItemCheckedStateChanged"), JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
            bodyBuilder);
    methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

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
   * Method that generates onCreateActionMode ListActivity method
   * 
   * @param declaredByMetadataId
   * @param entity
   *            JavaType with the current entity to use
   * 
   * @return
   */
  private MethodMetadataBuilder getOnCreateActionModeMethod(String declaredByMetadataId,
      JavaType entity) {
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

    // Invoke .aj method with necessary implementation
    bodyBuilder.appendFormalLine(String.format("return onCreateActionMode%s(mode, menu);",
        entity.getSimpleTypeName()));

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(declaredByMetadataId, Modifier.PUBLIC, new JavaSymbolName(
            "onCreateActionMode"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
            bodyBuilder);
    methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

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
   * Method that generates onPrepareActionMode ListActivity method
   * 
   * @param declaredByMetadataId
   * @param entity
   *            JavaType with the current entity to use
   * 
   * @return
   */
  private MethodMetadataBuilder getOnPrepareActionModeMethod(String declaredByMetadataId,
      JavaType entity) {
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

    // Invoke .aj method with necessary implementation
    bodyBuilder.appendFormalLine(String.format("return onPrepareActionMode%s(mode, menu);",
        entity.getSimpleTypeName()));

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(declaredByMetadataId, Modifier.PUBLIC, new JavaSymbolName(
            "onPrepareActionMode"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
            bodyBuilder);
    methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

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
   * Method that generates onActionItemClicked ListActivity method
   * 
   * @param declaredByMetadataId
   * @param entity
   *            JavaType with the current entity to use
   * 
   * @return
   */
  private MethodMetadataBuilder getOnActionItemClickedMethod(String declaredByMetadataId,
      JavaType entity) {
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

    // Invoke .aj method with necessary implementation
    bodyBuilder.appendFormalLine(String.format("return onActionItemClicked%s(mode, item);",
        entity.getSimpleTypeName()));

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(declaredByMetadataId, Modifier.PUBLIC, new JavaSymbolName(
            "onActionItemClicked"), JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
            bodyBuilder);
    methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

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
   * Method that generates onDestroyActionMode ListActivity method
   * 
   * @param declaredByMetadataId
   * @param entity
   *            JavaType with the current entity to use
   * 
   * @return
   */
  private MethodMetadataBuilder getOnDestroyActionModeMethod(String declaredByMetadataId,
      JavaType entity) {
    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
    parameterTypes.add(AnnotatedJavaType
        .convertFromJavaType(new JavaType("android.view.ActionMode")));

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
    parameterNames.add(new JavaSymbolName("mode"));

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    // Invoke .aj method with necessary implementation
    bodyBuilder.appendFormalLine(String.format("onDestroyActionMode%s(mode);",
        entity.getSimpleTypeName()));

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(declaredByMetadataId, Modifier.PUBLIC, new JavaSymbolName(
            "onDestroyActionMode"), JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
            bodyBuilder);
    methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

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
   * Method that generates onItemClick ListActivity method
   * 
   * @param declaredByMetadataId
   * @param entity
   *            JavaType with the current entity to use
   * 
   * @return
   */
  private MethodMetadataBuilder getOnItemClickMethod(String declaredByMetadataId, JavaType entity) {
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

    // Invoke .aj method with necessary implementation
    bodyBuilder.appendFormalLine(String.format("onItemClick%s(parent, view, position, id);",
        entity.getSimpleTypeName()));

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(declaredByMetadataId, Modifier.PUBLIC, new JavaSymbolName(
            "onItemClick"), JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);
    methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

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
   * Method that generates processFinish FormActivity method
   * 
   * @param declaredByMetadataId
   * @param entity
   *            JavaType with the current entity to use
   * 
   * @return
   */
  private MethodMetadataBuilder getProcessFinishMethod(String declaredByMetadataId, JavaType entity) {
    // Define method parameter types
    List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
    parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
        "org.osmdroid.util.GeoPoint")));

    // Define method parameter names
    List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
    parameterNames.add(new JavaSymbolName("output"));

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

    // Invoke .aj method with necessary implementation
    bodyBuilder.appendFormalLine(String.format("processFinish%s(output);",
        entity.getSimpleTypeName()));

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(declaredByMetadataId, Modifier.PUBLIC, new JavaSymbolName(
            "processFinish"), JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);
    methodBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("Override")));

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
   * Method that generates necessary configuration files like strings.xml and
   * styles.xml
   * 
   * @param applicationId
   */
  private void addBasicFiles(JavaPackage applicationPackage) {

    // Installing all drawable resources
    operationsUtils.updateDirectoryContents("drawable/*.xml", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/drawable"), fileManager, cContext,
        getClass());
    operationsUtils.updateDirectoryContents("drawable/*.png", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/drawable"), fileManager, cContext,
        getClass());

    // Installing all drawable-hdpi resources
    operationsUtils.updateDirectoryContents("drawable-hdpi/*.xml", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/drawable-hdpi"), fileManager,
        cContext, getClass());
    operationsUtils.updateDirectoryContents("drawable-hdpi/*.png", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/drawable-hdpi"), fileManager,
        cContext, getClass());

    // Installing all drawable-mdpi resources
    operationsUtils.updateDirectoryContents("drawable-mdpi/*.xml", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/drawable-mdpi"), fileManager,
        cContext, getClass());
    operationsUtils.updateDirectoryContents("drawable-mdpi/*.png", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/drawable-mdpi"), fileManager,
        cContext, getClass());

    // Installing all drawable-xhdpi resources
    operationsUtils.updateDirectoryContents("drawable-xhdpi/*.xml", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/drawable-xhdpi"), fileManager,
        cContext, getClass());
    operationsUtils.updateDirectoryContents("drawable-xhdpi/*.png", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/drawable-xhdpi"), fileManager,
        cContext, getClass());

    // Installing all drawable-xxhdpi resources
    operationsUtils.updateDirectoryContents("drawable-xxhdpi/*.xml", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/drawable-xxhdpi"), fileManager,
        cContext, getClass());
    operationsUtils.updateDirectoryContents("drawable-xxhdpi/*.png", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/drawable-xxhdpi"), fileManager,
        cContext, getClass());

    // Installing all menu resources
    operationsUtils.updateDirectoryContents("menu/*.xml",
        pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/menu"),
        fileManager, cContext, getClass());

    // Installing all mipmap-xhdpi resources
    operationsUtils.updateDirectoryContents("mipmap-xhdpi/*.xml", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/mipmap-xhdpi"), fileManager,
        cContext, getClass());
    operationsUtils.updateDirectoryContents("mipmap-xhdpi/*.png", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/mipmap-xhdpi"), fileManager,
        cContext, getClass());

    // Installing all values resources
    operationsUtils.updateDirectoryContents("values/*.xml",
        pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/values"),
        fileManager, cContext, getClass());
    operationsUtils.updateDirectoryContents("values/*.png",
        pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/values"),
        fileManager, cContext, getClass());

    // Installing all values resources
    operationsUtils.updateDirectoryContents("values-w820dp/*.xml", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/values-w820dp"), fileManager,
        cContext, getClass());
    operationsUtils.updateDirectoryContents("values-w820dp/*.png", pathResolver.getIdentifier(
        operationsUtils.getResourcesPath(projectOperations), "/values-w820dp"), fileManager,
        cContext, getClass());

    // Copying basic strings.xml file with current application name
    InputStream stringsXmlFile =
        FileUtils.getInputStream(getClass(), "values-customized/strings.xml");

    final Document stringsFile = XmlUtils.readXml(stringsXmlFile);
    final Element stringsRoot = stringsFile.getDocumentElement();

    NodeList strings = stringsRoot.getElementsByTagName("string");
    for (int i = 0; i < strings.getLength(); i++) {
      Element item = (Element) strings.item(i);
      if (item.getAttribute("name").equals("app_name")) {
        item.setTextContent(applicationPackage.getLastElement());
      } else if (item.getAttribute("name").equals("welcome_text")) {
        item.setTextContent("Welcome to ".concat(applicationPackage.getLastElement()).concat(
            " Android application"));
      }
    }

    final String stringsPath =
        pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RES, "values/strings.xml");
    final MutableFile mutableFile = fileManager.createFile(stringsPath);

    XmlUtils.writeXml(mutableFile.getOutputStream(), stringsFile);

  }

  /**
   * Method that generates main activity including layouts, controllers and
   * all necessary files to run application on an android device.
   */
  private void generatesMainActivity() {

    // Include main activity layout xml file
    operationsUtils.updateDirectoryContents("layout/main_activity.xml",
        pathResolver.getIdentifier(operationsUtils.getResourcesPath(projectOperations), "/layout"),
        fileManager, cContext, getClass());

    // Define main activity on AndroidManifest.xml
    Element mainActivity =
        manifestOperations.addActivity(".activities.MainActivity", "@string/app_name",
            "orientation", "portrait");
    manifestOperations.addIntentFilterToActivity(mainActivity, "android.intent.action.MAIN",
        "android.intent.category.LAUNCHER");

    // Generating main activity class
    // Getting current package
    String activitiesPath =
        projectOperations.getFocusedTopLevelPackage().getFullyQualifiedPackageName()
            .concat(".activities");

    int modifier = Modifier.PUBLIC;
    JavaType target = new JavaType(activitiesPath.concat(".MainActivity"));
    final String declaredByMetadataId =
        PhysicalTypeIdentifier.createIdentifier(target,
            pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
    File targetFile =
        new File(typeLocationService.getPhysicalTypeCanonicalPath(declaredByMetadataId));
    Validate.isTrue(!targetFile.exists(), "Type '%s' already exists", target);

    // Prepare class builder
    final ClassOrInterfaceTypeDetailsBuilder cidBuilder =
        new ClassOrInterfaceTypeDetailsBuilder(declaredByMetadataId, modifier, target,
            PhysicalTypeCategory.CLASS);

    // DatabaseConfigUtils extends Activity
    cidBuilder.addExtendsTypes(new JavaType("android.app.Activity"));

    // Including AndrooidMainActivity annotation
    cidBuilder
        .addAnnotation(new AnnotationMetadataBuilder(new JavaType(AndrooidMainActivity.class)));

    typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

  }

  /**
   * Method that uses configuration.xml file to install dependencies and
   * properties on current pom.xml
   */
  private void installDependencies() {
    final Element configuration = XmlUtils.getConfiguration(getClass());

    // Add properties
    List<Element> properties =
        XmlUtils.findElements("/configuration/androoid/properties/*", configuration);
    for (Element property : properties) {
      projectOperations.addProperty(projectOperations.getFocusedModuleName(),
          new Property(property));
    }

    // Add dependencies
    List<Element> elements =
        XmlUtils.findElements("/configuration/androoid/dependencies/dependency", configuration);
    List<Dependency> dependencies = new ArrayList<Dependency>();
    for (Element element : elements) {
      Dependency dependency = new Dependency(element);
      dependencies.add(dependency);
    }
    projectOperations.addDependencies(projectOperations.getFocusedModuleName(), dependencies);
  }

  /**
   * Method that checks if current entity has some Geo Field defined
   * 
   * @param entity
   * @return
   */
  private boolean hasGeoField(JavaType entity) {
    Set<ClassOrInterfaceTypeDetails> allEntities =
        typeLocationService.findClassesOrInterfaceDetailsWithAnnotation(new JavaType(
            AndrooidEntity.class));
    for (ClassOrInterfaceTypeDetails definedEntity : allEntities) {

      // Getting current entity
      if (definedEntity.getType().equals(entity)) {
        List<? extends FieldMetadata> fields = definedEntity.getDeclaredFields();
        for (FieldMetadata field : fields) {
          if (field.getFieldType().equals(new JavaType("org.osmdroid.util.GeoPoint"))) {
            return true;
          }
        }
      }
    }

    return false;
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
      fieldViewType = "mapView";
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
   * This method obtains strings.xml mutable file
   * 
   * @return
   */
  public MutableFile getStringsMutableFile() {
    String stringsXmlPath =
        projectOperations.getPathResolver().getIdentifier(
            LogicalPath.getInstance(Path.SRC_MAIN_RES, ""), "values/strings.xml");
    Validate
        .isTrue(fileManager.exists(stringsXmlPath), "src/main/res/values/strings.xml not found");

    MutableFile stringsXmlMutableFile = null;

    try {
      stringsXmlMutableFile = fileManager.updateFile(stringsXmlPath);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    return stringsXmlMutableFile;
  }

  /**
   * FEATURE METHODS
   */

  public String getName() {
    return FEATURE_ANDROOID_ACTIVITY_LAYER;
  }

  public boolean isInstalledInModule(String moduleName) {
    final String manifestPath =
        pathResolver.getFocusedIdentifier(Path.SRC_MAIN_RES, "mipmap-xhdpi/app_icon.png");
    return fileManager.exists(manifestPath);
  }
}
