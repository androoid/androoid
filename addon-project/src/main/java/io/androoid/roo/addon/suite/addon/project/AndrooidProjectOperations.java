package io.androoid.roo.addon.suite.addon.project;

import io.androoid.roo.addon.suite.addon.project.utils.AvailableSDKs;

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.project.Feature;

/**
 * Interface of Androoid project commands that are available via the Roo shell.
 *
 * @author Juan Carlos García
 * @since 1.0
 */
public interface AndrooidProjectOperations extends Feature {

  public static final String FEATURE_ANDROOID_PROJECT = "androoid-project";

  /**
   * Indicates if create an androoid project command should be available.
   * 
   * @return true if it should be available, otherwise false
   */
  boolean isCreateProjectAvailable();


  /**
   * Generates Android project structure.
   * 
   * @param applicationId
   * @param minSdkVersion
   * @param targetSdkVersion
   */
  void setup(JavaPackage applicationId, AvailableSDKs minSdkVersion, AvailableSDKs targetSdkVersion);

}
