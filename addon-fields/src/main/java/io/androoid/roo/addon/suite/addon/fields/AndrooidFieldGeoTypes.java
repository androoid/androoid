package io.androoid.roo.addon.suite.addon.fields;

/**
 * Enum that defines all GEO types allowed by Androoid
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public enum AndrooidFieldGeoTypes {

  POINT("org.osmdroid.util.GeoPoint");

  public final String description;

  AndrooidFieldGeoTypes(String description) {
    this.description = description;
  }

  public static AndrooidFieldGeoTypes getFieldGeoTypes(String geoTypes) {
    if (geoTypes != null && !"null".equals(geoTypes)) {
      try {
        return AndrooidFieldGeoTypes.valueOf(geoTypes);
      } catch (java.lang.IllegalArgumentException ex) {
        return null;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return description;
  }
}
