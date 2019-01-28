package access;

/**
 * The class contains a textual description and the coordinates of a location.
 */
public class Location {
  public final String name;
  public final float latitude;
  public final float longitude;

  Location(String location) {
    String[] locationSplit = location.split(":");
    name = locationSplit[1];
    latitude = Float.parseFloat(locationSplit[2]);
    longitude = Float.parseFloat(locationSplit[3]);
  }
}
