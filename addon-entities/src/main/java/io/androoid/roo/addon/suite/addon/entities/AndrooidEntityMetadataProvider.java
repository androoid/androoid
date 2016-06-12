package io.androoid.roo.addon.suite.addon.entities;

import java.io.Serializable;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

import io.androoid.roo.addon.suite.addon.entities.annotations.AndrooidEntity;

/**
 * Provides {@link AndrooidEntityMetadata}.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidEntityMetadataProvider extends AbstractItdMetadataProvider {

  protected final static Logger LOGGER = HandlerUtils
      .getLogger(AndrooidEntityMetadataProvider.class);

  public static final JavaType ANDROOID_ENTITY = new JavaType(AndrooidEntity.class);

  protected void activate(final ComponentContext cContext) {
    context = cContext.getBundleContext();
    getMetadataDependencyRegistry().addNotificationListener(this);
    getMetadataDependencyRegistry().registerDependency(
        PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
    addMetadataTrigger(ANDROOID_ENTITY);
  }

  @Override
  protected String createLocalIdentifier(final JavaType javaType, final LogicalPath path) {
    return AndrooidEntityMetadata.createIdentifier(javaType, path);
  }

  protected void deactivate(final ComponentContext context) {
    getMetadataDependencyRegistry().removeNotificationListener(this);
    getMetadataDependencyRegistry().deregisterDependency(
        PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
    removeMetadataTrigger(ANDROOID_ENTITY);
  }

  @Override
  protected String getGovernorPhysicalTypeIdentifier(final String metadataIdentificationString) {
    final JavaType javaType = AndrooidEntityMetadata.getJavaType(metadataIdentificationString);
    final LogicalPath path = AndrooidEntityMetadata.getPath(metadataIdentificationString);
    return PhysicalTypeIdentifier.createIdentifier(javaType, path);
  }

  public String getItdUniquenessFilenameSuffix() {
    return "AndrooidEntity";
  }

  @Override
  protected ItdTypeDetailsProvidingMetadataItem getMetadata(
      final String metadataIdentificationString, final JavaType aspectName,
      final PhysicalTypeMetadata governorPhysicalTypeMetadata, final String itdFilename) {

    JavaType javaType = AndrooidEntityMetadata.getJavaType(metadataIdentificationString);

    ClassOrInterfaceTypeDetails entityClass = getTypeLocationService().getTypeDetails(javaType);

    final String physicalTypeIdentifier = entityClass.getDeclaredByMetadataId();

    MemberDetails entityMemberDetails = getMemberDetails(governorPhysicalTypeMetadata);

    // Getting @AndrooidEntity annotation and values
    AnnotationMetadata androoidEntityAnnotation =
        entityClass.getAnnotation(new JavaType(AndrooidEntity.class));

    JavaSymbolName identifierFieldValue = null;
    JavaType identifierTypeValue = null;

    AnnotationAttributeValue<String> identifierField =
        androoidEntityAnnotation.getAttribute("identifierField");
    AnnotationAttributeValue<Class<? extends Serializable>> identifierType =
        androoidEntityAnnotation.getAttribute("identifierType");

    if (identifierField == null) {
      identifierFieldValue = new JavaSymbolName("id");
    } else {
      identifierFieldValue = new JavaSymbolName(identifierField.getValue());;
    }

    if (identifierType == null) {
      identifierTypeValue = JavaType.LONG_OBJECT;
    } else {
      identifierTypeValue = new JavaType(identifierType.getValue());
    }

    return new AndrooidEntityMetadata(metadataIdentificationString, aspectName,
        governorPhysicalTypeMetadata, physicalTypeIdentifier, entityMemberDetails,
        identifierFieldValue, identifierTypeValue);
  }

  public String getProvidesType() {
    return AndrooidEntityMetadata.getMetadataIdentiferType();
  }

}
