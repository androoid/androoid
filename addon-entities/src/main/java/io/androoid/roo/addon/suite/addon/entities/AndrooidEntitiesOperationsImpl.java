package io.androoid.roo.addon.suite.addon.entities;

import java.io.File;
import java.lang.reflect.Modifier;
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
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;

/**
 * Implementation of {@link AndrooidEntitiesOperations} interface.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidEntitiesOperationsImpl implements
		AndrooidEntitiesOperations {

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
	public void createEntity(JavaType entity) {
		int modifier = Modifier.PUBLIC;
		final String declaredByMetadataId = PhysicalTypeIdentifier
				.createIdentifier(entity,
						pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
		File targetFile = new File(
				typeLocationService
						.getPhysicalTypeCanonicalPath(declaredByMetadataId));
		Validate.isTrue(!targetFile.exists(), "Type '%s' already exists",
				entity);

		// Prepare class builder
		final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
				declaredByMetadataId, modifier, entity,
				PhysicalTypeCategory.CLASS);


		// Including DatabaseTable annotation
		cidBuilder.addAnnotation(new AnnotationMetadataBuilder(new JavaType(
				"com.j256.ormlite.table.DatabaseTable")));
		
		// TODO: Include @AndrooidEntity annotation

		typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
	}

}