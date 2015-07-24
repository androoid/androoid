package io.androoid.roo.addon.suite.addon.persistence;


/**
 * Interface of Androoid persistence commands that are available 
 * via the Roo shell.
 *
 * @author Juan Carlos García
 * @since 1.0
 */
public interface AndrooidPersistenceOperations {

    /**
     * Indicates if androoid persistence command 
     * should be available.
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isPersistenceSetupAvailable();
    
    /**
     * Configures persistence on generated Android project.
     * 
     * TODO: By default, uses ORMLite and SQLite, but in future versions 
     * developers should select ORM they want to use.
     * 
     */
    void setup();



}