package access;

import de.uni_stuttgart.searchfilter.common.configuration.C;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a single instance of a map from original locations as a
 * string to {@link Location}
 */
public class LocationSingleton {
  private static Map<String, Location> locations = null;

  private LocationSingleton() {}

  public static Map<String, Location> getInstance() {
    if (locations == null) {
      locations = new HashMap<>();

      Iterable<CSVRecord> records;
      try {
        FileInputStream fileInputStream = new FileInputStream(
            C.FilePath.locationsFile());
        InputStreamReader inputStreamReader = new InputStreamReader(
            fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        records = CSVFormat.TDF.withHeader().withSkipHeaderRecord().parse(
            bufferedReader);
      } catch (IOException exception) {
        exception.printStackTrace();
        return null;
      }
      for (CSVRecord record : records) {
        locations.put(record.get(2),
            new Location(Float.parseFloat(record.get(4)),
                Float.parseFloat(record.get(5))));
      }
    }
    return locations;
  }
}
