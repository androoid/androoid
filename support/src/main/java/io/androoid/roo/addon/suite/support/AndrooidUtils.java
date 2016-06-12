package io.androoid.roo.addon.suite.support;

import java.util.Map;

import org.osgi.service.component.ComponentContext;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Interface that provides different utils method that could be used by all
 * Androoid AddOns that needed.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public interface AndrooidUtils {

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
  public void updateDirectoryContents(String sourceAntPath, String targetDirectory,
      FileManager fileManager, ComponentContext context, Class<?> clazz);

  /**
   * Insert a new element of type {@code nodeName} into {@code parent} with
   * attributes declared in {@code attributes}.
   * 
   * @param doc
   * @param parent
   * @param nodeName
   * @param attributes
   */
  public Element insertXmlElement(Document doc, Element parent, String nodeName,
      Map<String, String> attributes);

  /**
   * Gets the {@code src/main/res} logicalPath
   * 
   * @param projectOperations
   * @return
   */
  public LogicalPath getResourcesPath(ProjectOperations projectOperations);

  /**
   * Gets the {@code src/main} logicalPath
   * 
   * @param projectOperations
   * @return
   */
  public LogicalPath getMainPath(ProjectOperations projectOperations);

}
