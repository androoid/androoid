package io.androoid.roo.addon.suite.support;

import org.osgi.service.component.ComponentContext;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;

/**
 * Interface that provides different utils method that could be used by all
 * Androoid AddOns that needed.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public interface AndrooidOperationsUtils {

	/**
	 * Updates files in source path into target directory path. <strong>Useful
	 * for copy/update binary resources (images) from Addon bundle resources to
	 * destination directory</strong>. For text resources (tagx, jspx, ...) use
	 * <code>AbstractOperations.copyDirectoryContents(..)</code> instead
	 * 
	 * @param sourceAntPath
	 *            the source path
	 * @param targetDirectory
	 *            the target directory
	 * @param fileManager
	 * @param context
	 * @param clazz
	 *            which owns the resources in source path
	 * @see org.springframework.roo.classpath.operations.AbstractOperations.
	 *      copyDirectoryContents(String, String, boolean)
	 */
	public void updateDirectoryContents(String sourceAntPath, String targetDirectory, FileManager fileManager,
			ComponentContext context, Class<?> clazz);
	

    /**
     * Gets the {@code src/main/res} logicalPath
     * 
     * @param projectOperations
     * @return
     */
    public LogicalPath getResourcesPath(ProjectOperations projectOperations);

}
