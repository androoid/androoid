package io.androoid.roo.addon.suite.addon.entities;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.RooJavaType;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

import io.androoid.roo.addon.suite.addon.entities.annotations.AndrooidEntity;

/**
 * Implementation of {@link AndrooidEntitiesOperations} interface.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidEntitiesOperationsImpl implements AndrooidEntitiesOperations {

	/**
	 * Get hold of a JDK Logger
	 */
	private Logger LOGGER = Logger.getLogger(getClass().getName());

	@Reference
	private ProjectOperations projectOperations;

	@Reference
	private PathResolver pathResolver;

	@Reference
	private TypeLocationService typeLocationService;

	@Reference
	private TypeManagementService typeManagementService;

	/** {@inheritDoc} */
	public boolean isEntityCreationAvailable() {
		return projectOperations.isFeatureInstalled("androoid-persistence");
	}

	/** {@inheritDoc} */
	public void createEntity(JavaType entity, JavaSymbolName identifierName, JavaType identifierType) {
		// Install necessary dependencies
		installDependencies();

		// Generate entity
		int modifier = Modifier.PUBLIC;
		final String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(entity,
				pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
		File targetFile = new File(typeLocationService.getPhysicalTypeCanonicalPath(declaredByMetadataId));
		Validate.isTrue(!targetFile.exists(), "Type '%s' already exists", entity);

		// Prepare class builder
		final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
				declaredByMetadataId, modifier, entity, PhysicalTypeCategory.CLASS);

		// Including DatabaseTable annotation
		cidBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType("com.j256.ormlite.table.DatabaseTable")));

		// Including @AndrooidEntity annotation
		AnnotationMetadataBuilder entityAnnotation = new AnnotationMetadataBuilder(new JavaType(AndrooidEntity.class));
		if (identifierName != null) {
			entityAnnotation.addStringAttribute("identifierField", identifierName.getSymbolName());
		}
		if (identifierType != null) {
			entityAnnotation.addClassAttribute("identifierType", identifierType);

		}
		cidBuilder.addAnnotation(entityAnnotation);
		
		// Including @RooJavaBean to generate getters and setters
		AnnotationMetadataBuilder javaBeanAnnotation = new AnnotationMetadataBuilder(new JavaType("org.springframework.roo.addon.javabean.annotations.RooJavaBean"));
		cidBuilder.addAnnotation(javaBeanAnnotation);

		typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
	}

	/**
	 * Method that uses configuration.xml file to install dependencies and
	 * properties on current pom.xml
	 */
	private void installDependencies() {
		final Element configuration = XmlUtils.getConfiguration(getClass());

		// Add properties
		List<Element> properties = XmlUtils.findElements("/configuration/androoid/properties/*", configuration);
		for (Element property : properties) {
			projectOperations.addProperty(projectOperations.getFocusedModuleName(), new Property(property));
		}

		// Add dependencies
		List<Element> elements = XmlUtils.findElements("/configuration/androoid/dependencies/dependency",
				configuration);
		List<Dependency> dependencies = new ArrayList<Dependency>();
		for (Element element : elements) {
			Dependency dependency = new Dependency(element);
			dependencies.add(dependency);
		}
		projectOperations.addDependencies(projectOperations.getFocusedModuleName(), dependencies);
	}

}