package io.androoid.roo.addon.suite.support;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * Implementation of {@AndrooidUtils} utility class
 * 
 * @author Juan Carlos García
 * @since 1.0
 *
 */
@Component
@Service
public class AndrooidUtilsImpl implements AndrooidUtils {

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
	 * Insert a new element of type {@code nodeName} into {@code parent} with
	 * attributes declared in {@code attributes}.
	 *
	 * @param doc
	 * @param parent
	 * @param nodeName
	 * @param attributes
	 */
	public Element insertXmlElement(Document doc, Element parent, String nodeName, Map<String, String> attributes) {

		Element newElement = doc.createElement(nodeName);

		for (Entry<String, String> attribute : attributes.entrySet()) {
			String name = attribute.getKey();
			String value = attribute.getValue();
			newElement.setAttribute(name, value);
		}

		// insert element as last element of the node type
		Node inserPosition = null;
		// Locate last node of this type
		List<Element> elements = XmlUtils.findElements(nodeName, parent);
		if (!elements.isEmpty()) {
			inserPosition = elements.get(elements.size() - 1).getNextSibling();
		}

		// Add node
		if (inserPosition == null) {
			parent.appendChild(newElement);
		} else {
			parent.insertBefore(newElement, inserPosition);
		}

		return newElement;
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

	/**
	 * Gets the {@code src/main} logicalPath
	 * 
	 * @param projectOperations
	 * @return
	 */
	public LogicalPath getMainPath(ProjectOperations projectOperations) {
		return LogicalPath.getInstance(Path.SRC_MAIN, projectOperations.getFocusedModuleName());
	}

}
