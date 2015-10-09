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
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
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

import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidFormActivity;
import io.androoid.roo.addon.suite.addon.fields.annotations.AndrooidReferencedField;

/**
 * Metadata for {@link AndrooidFormActivity} annotation.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public class AndrooidActivityFormMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

	private static final JavaType ARRAY_ADAPTER_JAVATYPE = new JavaType("android.widget.ArrayAdapter");
	private static final String PROVIDES_TYPE_STRING = AndrooidActivityFormMetadata.class.getName();
	private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

	private final ImportRegistrationResolver importResolver;
	private final JavaType entity;
	private final JavaPackage applicationPackage;
	private final JavaType listEntityJavaType;
	private final JavaType arrayListEntityJavaType;
	private final String getIdFieldMethod;
	private final JavaType entityIdFieldType;
	private final List<FieldMetadata> entityFields;
	private final Map<String, String> fieldNameLayout;

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
	 * @param entityIdFieldName
	 *            String that contains the identifier field name of current
	 *            entity
	 * @param entityIdFieldType
	 *            JavaType that contains the type of the identifier field of the
	 *            current entity
	 * 
	 */
	public AndrooidActivityFormMetadata(final String identifier, final JavaType aspectName,
			final PhysicalTypeMetadata governorPhysicalTypeMetadata, JavaPackage projectPackage, JavaType entity,
			String entityIdFieldName, JavaType entityIdFieldType, List<FieldMetadata> entityFields) {
		super(identifier, aspectName, governorPhysicalTypeMetadata);
		Validate.isTrue(isValid(identifier), "Metadata identification string '%s' does not appear to be a valid",
				identifier);

		this.importResolver = builder.getImportRegistrationResolver();
		this.entity = entity;
		this.applicationPackage = projectPackage;
		this.listEntityJavaType = new JavaType("java.util.List", 0, DataType.TYPE, null, Arrays.asList(entity));
		this.arrayListEntityJavaType = new JavaType("java.util.ArrayList", 0, DataType.TYPE, null,
				Arrays.asList(entity));
		this.getIdFieldMethod = "get"
				.concat(Character.toUpperCase(entityIdFieldName.charAt(0)) + entityIdFieldName.substring(1));
		this.entityIdFieldType = entityIdFieldType;
		this.entityFields = entityFields;
		this.fieldNameLayout = new HashMap<String, String>();

		// Adding fields
		addFormActivityFields();

		// Adding necessary methods
		builder.addMethod(getOnCreateMethod());

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
		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
				new JavaSymbolName("onCreate"), JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);
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
						.getNameIncludingTypeParameters(false, importResolver),
				entity.getSimpleTypeName().toLowerCase()));

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
			} else if (fieldName.endsWith("Spinner")) {
				fieldType = "Spinner";
			}

			// fieldName = (fieldType) findViewById(R.id.fieldId);
			bodyBuilder
					.appendFormalLine(String.format("%s = (%s) findViewById(R.id.%s);", fieldName, fieldType, fieldId));

			// If is a MapView item needs some extra configuration
			if (isMapView) {

				// Including mapview text
				String textFieldName = fieldName.replaceFirst("MapView", "EdiText");

				// fieldName = (fieldType) findViewById(R.id.fieldId);
				fieldId = fieldId.substring(0, fieldId.lastIndexOf("_")).concat("_text");
				bodyBuilder.appendFormalLine(
						String.format("%s = (%s) findViewById(R.id.%s);", textFieldName, "EditText", fieldId));

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
								importResolver),
						fieldName));

				// mapController.setZoom(5);
				bodyBuilder.appendFormalLine("mapController.setZoom(5);");

				// GeoPoint startPoint = new GeoPoint(45.416775400000000000,
				// -7.703790199999957600);
				bodyBuilder.appendFormalLine(
						String.format("%s startPoint = new GeoPoint(45.416775400000000000, -7.703790199999957600);",
								new JavaType("org.osmdroid.util.GeoPoint").getNameIncludingTypeParameters(false,
										importResolver)));

				// mapController.setCenter(startPoint);
				bodyBuilder.appendFormalLine("mapController.setCenter(startPoint);");

				bodyBuilder.appendFormalLine("");

				// Adding event on street input
				bodyBuilder.appendFormalLine("// Adding event on street input");

				// final Handler mHandler = new Handler();
				bodyBuilder.appendFormalLine(String.format("final %s mHandler = new Handler();",
						new JavaType("android.os.Handler").getNameIncludingTypeParameters(false, importResolver)));

				// textFieldName.addTextChangedListener(new TextWatcher() {
				bodyBuilder.appendFormalLine(String.format("%s.addTextChangedListener(new %s() {", textFieldName,
						new JavaType("android.text.TextWatcher").getNameIncludingTypeParameters(false,
								importResolver)));

				// public void afterTextChanged(Editable s) {
				bodyBuilder.indent();
				bodyBuilder.appendFormalLine(String.format("public void afterTextChanged(%s s) {",
						new JavaType("android.text.Editable").getNameIncludingTypeParameters(false, importResolver)));

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
				bodyBuilder.appendFormalLine(String
						.format("public void beforeTextChanged(CharSequence s, int start, int count, int after) {"));
				bodyBuilder.appendFormalLine("}");
				bodyBuilder.appendFormalLine("");

				// public void onTextChanged(CharSequence s, int start, int
				// before, int count) {
				bodyBuilder.appendFormalLine("");
				bodyBuilder.appendFormalLine(
						String.format("public void onTextChanged(CharSequence s, int start, int before, int count) {"));
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
				bodyBuilder.appendFormalLine(String.format("addressSearchHelper.delegate = %sFormActivity.this;",
						entity.getSimpleTypeName()));

				// // Update map with address location
				bodyBuilder.appendFormalLine("");
				bodyBuilder.appendFormalLine(" // Update map with address location");

				// String address = entityLocationText.getText().toString();
				bodyBuilder.appendFormalLine(String.format("String address = %s.getText().toString();", textFieldName));

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

		// TODO: Populate spinners

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
		bodyBuilder.appendFormalLine(
				String.format("int %s = bundle.getInt(\"%s\");", entity.getSimpleTypeName().toLowerCase().concat("Id"),
						entity.getSimpleTypeName().toLowerCase().concat("Id")));

		// // Getting entity by id
		bodyBuilder.appendFormalLine("// Getting entity by id");

		// populateForm(entityId);
		bodyBuilder.appendFormalLine(
				String.format("populateForm(%s);", entity.getSimpleTypeName().toLowerCase().concat("Id")));

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
	 * Method to add all necessary fields to FormActivity .aj file
	 */
	private void addFormActivityFields() {
		FieldMetadataBuilder adapterField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
				new JavaSymbolName(entity.getSimpleTypeName().toLowerCase()), entity, null);
		builder.addField(adapterField);

		boolean hasGeoField = false;

		// Adding fields on current form activity
		for (FieldMetadata field : entityFields) {

			// Checking if current field is a valid Database Field
			AnnotationMetadata databaseFieldAnnotation = field
					.getAnnotation(new JavaType("com.j256.ormlite.field.DatabaseField"));
			if (databaseFieldAnnotation != null) {

				boolean hasReferencedField = false;

				// Checking if field is a generatedId field
				AnnotationAttributeValue<Boolean> generatedIdAttr = databaseFieldAnnotation.getAttribute("generatedId");

				boolean generatedId = false;

				if (generatedIdAttr != null) {
					generatedId = generatedIdAttr.getValue();
				}

				if (!generatedId) {

					// Getting field type
					JavaType fieldType = field.getFieldType();
					JavaType formFieldType = null;
					String fieldViewType = "";

					if (fieldType.equals(JavaType.BOOLEAN_PRIMITIVE) || fieldType.equals(JavaType.BOOLEAN_OBJECT)) {
						formFieldType = new JavaType("android.widget.Switch");
						fieldViewType = "switch";
					} else if (fieldType.equals(new JavaType("org.osmdroid.util.GeoPoint"))) {
						formFieldType = new JavaType("org.osmdroid.views.MapView");
						hasGeoField = true;
						fieldViewType = "mapview";
					} else if (isReferencedField(field)) {
						formFieldType = new JavaType("android.widget.Spinner");
						hasReferencedField = true;
						fieldViewType = "spinner";
					} else {
						formFieldType = new JavaType("android.widget.EditText");
						fieldViewType = "text";
					}

					String fieldName = entity.getSimpleTypeName().toLowerCase()
							+ Character.toLowerCase(field.getFieldName().getSymbolName().charAt(0))
							+ field.getFieldName().getSymbolName().substring(1)
									.concat(formFieldType.getSimpleTypeName());

					FieldMetadataBuilder entityField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
							new JavaSymbolName(fieldName), formFieldType, null);
					builder.addField(entityField);

					// Saving fieldNameLayout that will be used on findViewById
					// method
					fieldNameLayout.put(fieldName,
							entity.getSimpleTypeName().toLowerCase().concat("_")
									.concat(field.getFieldName().getSymbolName().toLowerCase()).concat("_")
									.concat(fieldViewType));

					// If is a GEO field, is necessary to add an Edit Text
					// to make some geo search
					if (hasGeoField) {
						fieldName = entity.getSimpleTypeName().toLowerCase()
								+ Character.toLowerCase(field.getFieldName().getSymbolName().charAt(0))
								+ field.getFieldName().getSymbolName().substring(1).concat("EditText");

						FieldMetadataBuilder geoField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
								new JavaSymbolName(fieldName), new JavaType("android.widget.EditText"), null);
						builder.addField(geoField);

					}

					// If is a referenced field, add ArrayList to include
					// results
					if (hasReferencedField) {
						fieldName = fieldType.getSimpleTypeName().toLowerCase().concat("List");

						FieldMetadataBuilder relatedField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
								new JavaSymbolName(fieldName),
								new JavaType("java.util.ArrayList", 0, DataType.TYPE, null, Arrays.asList(fieldType)),
								null);
						builder.addField(relatedField);
					}

				}
			}

		}

		FieldMetadataBuilder modeField = new FieldMetadataBuilder(getId(), Modifier.PRIVATE, new JavaSymbolName("mode"),
				JavaType.STRING, null);
		builder.addField(modeField);

		// Check if exists some geo field
		if (hasGeoField) {
			FieldMetadataBuilder geoSearchHelper = new FieldMetadataBuilder(getId(), Modifier.PRIVATE,
					new JavaSymbolName("addressSearchHelper"),
					new JavaType("io.androoid.geo.search.AddressSearchHelper"), null);

			builder.addField(geoSearchHelper);
		}

	}

	/**
	 * Method to check if provided field has @AndrooidReferencedField annotation
	 * 
	 * @param field
	 *            FieldMetadata with the field to check
	 * 
	 * @return true if is annotated with @AndrooidReferencedField
	 */
	private boolean isReferencedField(FieldMetadata field) {
		AnnotationMetadata annotation = field.getAnnotation(new JavaType(AndrooidReferencedField.class));
		return annotation != null;
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
