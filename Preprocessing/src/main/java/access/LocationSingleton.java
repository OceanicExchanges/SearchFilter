package access;

import de.uni_stuttgart.searchfilter.common.configuration.C;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class represents a single instance of a map from original locations
 * as a string to {@link Location}
 */
public class LocationSingleton {
  private static Map<String, Location> locations = null;

  private LocationSingleton() {}

  public static Map<String, Location> getInstance() {
    if (locations == null) {
      locations = new HashMap<>();
      try (Stream<String> stream = Files
        .lines(Paths.get(C.FilePath.locationsFile()))) {
        stream.forEach(
          line -> locations.put(line.split(":")[0], new Location(line)));
      } catch (IOException exception) {
        exception.printStackTrace();
      }
      locations = Collections.unmodifiableMap(locations);
    }
    return locations;
  }
}
