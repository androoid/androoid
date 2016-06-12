package io.androoid.roo.addon.suite.addon.persistence;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CommandMarker;

/**
 * Androoid PersistenceCommands class. This class provides all necessary
 * commands to configure persistence of a generated android project using Spring
 * Roo Shell.
 * 
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidPersistenceCommands implements CommandMarker {

  /**
   * Get hold of a JDK Logger
   */
  private Logger LOGGER = Logger.getLogger(getClass().getName());

  @Reference
  private AndrooidPersistenceOperations persistenceOperations;

  /**
   * Persistence setup is only available if an Android project was generated before
   * using Spring Roo Shell
   * 
   * @return true if not exists other project in the current folder.
   */
  @CliAvailabilityIndicator("androoid persistence setup")
  public boolean isPersistenceSetupAvailable() {
    return persistenceOperations.isPersistenceSetupAvailable();
  }

  /**
   * This method registers the androoid persistence setup command.
   * 
   */
  @CliCommand(value = "androoid persistence setup",
      help = "Configure persistence on generated Android project.")
  public void persistenceSetup() {
    // Configuring Android project persistence
    persistenceOperations.setup();

  }
}
