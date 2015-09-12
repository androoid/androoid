package io.androoid.roo.addon.suite.support;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.osgi.OSGiUtils;
import org.springframework.roo.support.util.FileUtils;

/**
 * 
 * Implementation of {@AndrooidOperationsUtils} utility class
 * 
 * @author Juan Carlos García
 * @since 1.0
 *
 */
@Component
@Service
public class AndrooidOperationsUtilsImpl implements AndrooidOperationsUtils {

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
			ComponentContext context, Class<?> clazz) {
		StringUtils.isNotBlank(sourceAntPath);
		StringUtils.isNotBlank(targetDirectory);

		if (!targetDirectory.endsWith("/")) {
			targetDirectory += "/";
		}

		if (!fileManager.exists(targetDirectory)) {
			fileManager.createDirectory(targetDirectory);
		}

		String path = FileUtils.getPath(clazz, sourceAntPath);
		Collection<URL> urls = OSGiUtils.findEntriesByPattern(context.getBundleContext(), path);
		Validate.notNull(urls, "Could not search bundles for resources for Ant Path '" + path + "'");
		for (URL url : urls) {
			String fileName = url.getPath().substring(url.getPath().lastIndexOf("/") + 1);
			try {
				if (!fileManager.exists(targetDirectory + fileName)) {

					OutputStream outputStream = null;
					try {
						outputStream = fileManager.createFile(targetDirectory + fileName).getOutputStream();
						IOUtils.copy(url.openStream(), outputStream);
					} finally {
						IOUtils.closeQuietly(outputStream);
					}
				} else {

					OutputStream outputStream = null;
					try {
						outputStream = fileManager.updateFile(targetDirectory + fileName).getOutputStream();
						IOUtils.copy(url.openStream(), outputStream);
					} finally {
						IOUtils.closeQuietly(outputStream);
					}
				}
			} catch (IOException e) {
				throw new IllegalStateException("Encountered an error during updating of resources for the add-on.", e);
			}
		}
	}

	/**
	 * Gets the {@code src/main/res} logicalPath
	 * 
	 * @param projectOperations
	 * @return
	 */
	public LogicalPath getResourcesPath(ProjectOperations projectOperations) {
		return LogicalPath.getInstance(Path.SRC_MAIN_RES, projectOperations.getFocusedModuleName());
	}

}
