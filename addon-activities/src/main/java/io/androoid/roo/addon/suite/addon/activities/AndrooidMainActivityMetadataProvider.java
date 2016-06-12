package io.androoid.roo.addon.suite.addon.activities;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;

import io.androoid.roo.addon.suite.addon.activities.annotations.AndrooidMainActivity;

/**
 * Provides {@link AndrooidMainActivityMetadata}.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidMainActivityMetadataProvider extends AbstractItdMetadataProvider {

  protected final static Logger LOGGER = HandlerUtils
      .getLogger(AndrooidMainActivityMetadataProvider.class);

  public static final JavaType ANDROOID_MAIN_ACTVITY = new JavaType(AndrooidMainActivity.class);

  @Reference
  private ProjectOperations projectOperations;

  protected void activate(final ComponentContext cContext) {
    context = cContext.getBundleContext();
    getMetadataDependencyRegistry().addNotificationListener(this);
    getMetadataDependencyRegistry().registerDependency(
        PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
    addMetadataTrigger(ANDROOID_MAIN_ACTVITY);
  }

  @Override
  protected String createLocalIdentifier(final JavaType javaType, final LogicalPath path) {
    return AndrooidMainActivityMetadata.createIdentifier(javaType, path);
  }

  protected void deactivate(final ComponentContext context) {
    getMetadataDependencyRegistry().removeNotificationListener(this);
    getMetadataDependencyRegistry().deregisterDependency(
        PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
    removeMetadataTrigger(ANDROOID_MAIN_ACTVITY);
  }

  @Override
  protected String getGovernorPhysicalTypeIdentifier(final String metadataIdentificationString) {
    final JavaType javaType =
        AndrooidMainActivityMetadata.getJavaType(metadataIdentificationString);
    final LogicalPath path = AndrooidMainActivityMetadata.getPath(metadataIdentificationString);
    return PhysicalTypeIdentifier.createIdentifier(javaType, path);
  }

  public String getItdUniquenessFilenameSuffix() {
    return "AndrooidMainActivity";
  }

  @Override
  protected ItdTypeDetailsProvidingMetadataItem getMetadata(
      final String metadataIdentificationString, final JavaType aspectName,
      final PhysicalTypeMetadata governorPhysicalTypeMetadata, final String itdFilename) {

    JavaPackage applicationPackage = projectOperations.getFocusedTopLevelPackage();

    return new AndrooidMainActivityMetadata(metadataIdentificationString, aspectName,
        governorPhysicalTypeMetadata, applicationPackage);
  }

  public String getProvidesType() {
    return AndrooidMainActivityMetadata.getMetadataIdentiferType();
  }

}
