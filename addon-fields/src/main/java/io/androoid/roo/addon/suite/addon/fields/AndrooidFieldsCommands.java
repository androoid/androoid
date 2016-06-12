package io.androoid.roo.addon.suite.addon.fields;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Androoid Fields commands class. This class provides all necessary commands to
 * generate entity fields on Android Project.
 * 
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
@Component
@Service
public class AndrooidFieldsCommands implements CommandMarker {

  /**
   * Get hold of a JDK Logger
   */
  private Logger LOGGER = Logger.getLogger(getClass().getName());

  @Reference
  private AndrooidFieldsOperations fieldOperations;

  /**
   * Field generation is only available if persistence was configured using
   * androoid persistence command.
   * 
   * @return true if persistence is installed.
   */
  @CliAvailabilityIndicator({"androoid field string", "androoid field number",
      "androoid field boolean", "androoid field reference", "androoid field geo"})
  public boolean isEntityCreationAvailable() {
    return fieldOperations.isFieldCreationAvailable();
  }

  /**
   * This method registers the androoid field string command.
   * 
   * @param class
   * @param name
   */
  @CliCommand(value = "androoid field string",
      help = "Creates new String field on selected entity.")
  public void createStringField(@CliOption(key = "class", mandatory = false,
      unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT,
      help = "The name of the entity to receive this field") final JavaType entity, @CliOption(
      key = "name", mandatory = true, optionContext = UPDATE_PROJECT,
      help = "The field name to use.") final JavaSymbolName fieldName) {

    // Creating new field
    fieldOperations.createField(entity, fieldName, JavaType.STRING);
  }

  /**
   * This method registers the androoid field number command.
   * 
   * @param class
   * @param type
   * @param name
   */
  @CliCommand(value = "androoid field number",
      help = "Creates new numeric field on selected entity.")
  public void createNumberField(@CliOption(key = "class", mandatory = false,
      unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT,
      help = "The name of the entity to receive this field") final JavaType entity, @CliOption(
      key = "type", mandatory = true, optionContext = "java-number",
      help = "The Java type of the numeric field") JavaType fieldType,
      @CliOption(key = "name", mandatory = true, optionContext = UPDATE_PROJECT,
          help = "The field name to use.") final JavaSymbolName fieldName) {

    // Creating new field
    fieldOperations.createField(entity, fieldName, fieldType);
  }

  /**
   * This method registers the androoid field boolean command.
   * 
   * @param class
   * @param name
   */
  @CliCommand(value = "androoid field boolean", help = "Creates new boolean on selected entity.")
  public void createBooleanField(@CliOption(key = "class", mandatory = false,
      unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT,
      help = "The name of the entity to receive this field") final JavaType entity, @CliOption(
      key = "name", mandatory = true, optionContext = UPDATE_PROJECT,
      help = "The field name to use.") final JavaSymbolName fieldName) {

    // Creating new field
    fieldOperations.createField(entity, fieldName, JavaType.BOOLEAN_PRIMITIVE);
  }

  /**
   * This method registers the androoid field reference command.
   * 
   * @param class
   * @param type
   * @param name
   */
  @CliCommand(value = "androoid field reference",
      help = "Creates new reference to other existing entity on selected entity.")
  public void createReferenceField(@CliOption(key = "class", mandatory = false,
      unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT,
      help = "The name of the entity to receive this field") final JavaType entity, @CliOption(
      key = "type", mandatory = true, optionContext = UPDATE_PROJECT,
      help = "The entity to reference.") final JavaType entityToReference,
      @CliOption(key = "name", mandatory = true, optionContext = UPDATE_PROJECT,
          help = "The field name to use.") final JavaSymbolName fieldName) {

    // Creating new field
    fieldOperations.createReferencedField(entity, fieldName, entityToReference);
  }

  /**
   * This method registers the androoid field geo command.
   * 
   * @param class
   * @param type
   * @param name
   */
  @CliCommand(value = "androoid field geo", help = "Creates new geo field on selected entity.")
  public void createGeoField(
      @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*",
          optionContext = UPDATE_PROJECT, help = "The name of the entity to receive this field") final JavaType entity,
      @CliOption(key = "type", mandatory = true, help = "The GEO type of the field") AndrooidFieldGeoTypes fieldType,
      @CliOption(key = "name", mandatory = true, optionContext = UPDATE_PROJECT,
          help = "The field name to use.") final JavaSymbolName fieldName) {

    // Creating new field
    fieldOperations.createGeoField(entity, fieldName, fieldType);
  }
}
