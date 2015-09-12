package io.androoid.roo.addon.suite.addon.activities;

import org.springframework.roo.project.Feature;

/**
 * Interface of Androoid activity layer commands that are available via the Roo
 * shell.
 *
 * @author Juan Carlos García
 * @since 1.0
 */
public interface AndrooidActivitiesOperations extends Feature {

	public static final String FEATURE_ANDROOID_ACTIVITY_LAYER = "androoid-activity-layer";

	/**
	 * Checks if activity layer setup is available.
	 * 
	 * @return true if exists an androoid project in the current folder.
	 */
	boolean isSetupAvailable();

	/**
	 * Generates Android activity layer structure with all necessary components
	 * for activity layer.
	 */
	void setup();

}