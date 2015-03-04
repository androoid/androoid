package io.androoid.addon.android.project;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.osgi.service.component.ComponentContext;

/**
 * 
 * @author Juan Carlos García
 * @since 1.0.0
 * 
 */
@Component(immediate = true)
public class AndroidActivator {

	private static final Logger LOGGER = Logger
			.getLogger(AndroidActivator.class.getName());

	protected void activate(final ComponentContext context) {
		LOGGER.log(Level.INFO, "Welcome to Android - Spring Roo Addon Suite!");
	}

}
