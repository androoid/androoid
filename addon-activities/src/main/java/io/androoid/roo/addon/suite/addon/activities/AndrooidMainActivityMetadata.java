package io.androoid.roo.addon.suite.addon.activities;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidMainActivity;

/**
 * Metadata for {@link AndrooidMainActivity}.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public class AndrooidMainActivityMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

  private static final String PROVIDES_TYPE_STRING = AndrooidMainActivityMetadata.class.getName();
  private static final String PROVIDES_TYPE = MetadataIdentificationUtils
      .create(PROVIDES_TYPE_STRING);

  private JavaPackage applicationPackage;

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
   */
  public AndrooidMainActivityMetadata(final String identifier, final JavaType aspectName,
      final PhysicalTypeMetadata governorPhysicalTypeMetadata, JavaPackage applicationPackage) {
    super(identifier, aspectName, governorPhysicalTypeMetadata);
    Validate.isTrue(isValid(identifier),
        "Metadata identification string '%s' does not appear to be a valid", identifier);

    this.applicationPackage = applicationPackage;

    // Generating main method
    builder.addMethod(getOnCreateMethod());

    // Create a representation of the desired output ITD
    itdTypeDetails = builder.build();

  }

  /**
   * Generates onCreate method
   * 
   * @return onCreate Method Metadata Builder
   */
  private MethodMetadataBuilder getOnCreateMethod() {

    // Compute the mutator method name
    final JavaSymbolName methodName = new JavaSymbolName("onCreate");

    // See if the type itself declared the main method
    if (governorHasMethod(methodName)) {
      return null;
    }

    final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
    buildOnCreateMethodBody(bodyBuilder);

    MethodMetadataBuilder methodBuilder =
        new MethodMetadataBuilder(getId(), Modifier.PUBLIC, methodName, JavaType.VOID_PRIMITIVE,
            bodyBuilder);

    // Adding params
    methodBuilder.addParameter("savedInstanceState", new JavaType("android.os.Bundle"));

    // Adding annotations
    List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
    annotations.add(new AnnotationMetadataBuilder(new JavaType("Override")));
    methodBuilder.setAnnotations(annotations);

    return methodBuilder;

  }

  /**
   * Generates onCreate method body
   * 
   * @param bodyBuilder
   */
  private void buildOnCreateMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
    // super.onCreate(savedInstanceState);
    bodyBuilder.appendFormalLine("super.onCreate(savedInstanceState);");

    // setContentView(R.layout.main_activity);
    bodyBuilder.appendFormalLine(String.format("setContentView(%s.layout.main_activity);",
        new JavaType(applicationPackage.getFullyQualifiedPackageName().concat(".R"))
            .getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver())));

    // TODO: Include buttons for annotation registered entities
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
