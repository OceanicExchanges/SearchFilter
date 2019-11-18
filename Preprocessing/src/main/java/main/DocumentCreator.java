package main;

import access.IndexWriteSingleton;
import access.Location;
import access.LocationSingleton;
import de.uni_stuttgart.searchfilter.common.configuration.C;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

abstract class DocumentCreator implements Runnable {

  static final Logger logger = Logger.getLogger(
      DocumentCreator.class.getName());
  final IndexWriter writer;
  final Document document;
  final StoredField visualizationData;
  final StoredField textData;
  // Index fields
  private final LongPoint idField;
  private final IntPoint lengthField;
  private final StringField dateField;
  private final Field textField;
  private final DoublePoint longitudeField;
  private final DoublePoint latitudeField;
  private final StringField languageField;
  private final LongPoint clusterField;
  File file;
  AtomicInteger counter;
  Map<String, Location> locations;

  DocumentCreator(File file, AtomicInteger counter) throws IOException {
    this.file = file;
    this.counter = counter;
    this.locations = LocationSingleton.getInstance();
    // Setup the fields
    idField = new LongPoint(C.FieldNames.ID, 0);
    visualizationData = new StoredField(C.FieldNames.VISUALIZATION, "");
    textData = new StoredField(C.FieldNames.TEXT_DATA, "");
    lengthField = new IntPoint(C.FieldNames.LENGTH, 0);
    dateField = new StringField(C.FieldNames.DATE, "", Field.Store.NO);
    textField = new TextField(C.FieldNames.TEXT, "", Field.Store.NO);
    longitudeField = new DoublePoint(C.FieldNames.LONGITUDE, 0.0);
    latitudeField = new DoublePoint(C.FieldNames.LATITUDE, 0.0);
    languageField = new StringField(C.FieldNames.LANGUAGE, "", Field.Store.NO);
    clusterField = new LongPoint(C.FieldNames.CLUSTER, 0);
    document = new Document();
    document.add(idField);
    document.add(visualizationData);
    document.add(textData);
    document.add(lengthField);
    document.add(dateField);
    document.add(textField);
    document.add(longitudeField);
    document.add(latitudeField);
    document.add(languageField);
    document.add(clusterField);
    writer = IndexWriteSingleton.getInstance();
  }

  /**
   * Add an identification
   *
   * @param identification String representation of the ID
   * @param visualization  JSON that contains the visualization data
   * @param text           JSON that contains the text data
   */
  final void addIdentification(String identification, JSONObject visualization,
      JSONObject text) {
    long id = Long.parseLong(identification);
    idField.setLongValue(id);
    visualization.put(C.JSONFieldNames.ID, id);
    text.put(C.JSONFieldNames.ID, id);
  }

  /**
   * Add length of the text
   *
   * @param text          Full text of the entire document
   * @param visualization JSON that contains the visualization data
   */
  final void addTextLength(String text, JSONObject visualization) {
    int length = text.split("\\s").length;
    lengthField.setIntValue(length);
    visualization.put(C.JSONFieldNames.TEXT_LENGTH, length);
  }

  /**
   * Add date of the publication
   *
   * @param date          ISO 8601 String representation of the date
   * @param visualization JSON that contains the visualization data
   * @param text          JSON that contains the text data
   */
  final void addDate(String date, JSONObject visualization, JSONObject text) {
    String[] elements = date.split("-");
    switch (elements.length) {
      case 1:
        date = elements[0] + "-01-01";
        break;
      case 2:
        date = elements[0] + "-" + elements[1] + "-01";
        break;
      default:
        break;
    }
    dateField.setStringValue(date);
    visualization.put(C.JSONFieldNames.DATE, date);
    text.put(C.JSONFieldNames.DATE, date);
  }

  /**
   * Add full text
   *
   * @param text         full text
   * @param textDocument JSON that contains the text data
   */
  final void addText(String text, JSONObject textDocument) {
    textField.setStringValue(text);
    textDocument.put(C.JSONFieldNames.TEXT, text);
  }

  /**
   * Add publisher
   *
   * @param publisher    publisher string
   * @param textDocument JSON that contains the text data
   */
  final void addPublisher(String publisher, JSONObject textDocument) {
    textDocument.put(C.JSONFieldNames.PUBLISHER, publisher);
  }

  /**
   * Add link
   *
   * @param link         URL of article at library
   * @param textDocument JSON that contains the text data
   */
  final void addLink(String link, JSONObject textDocument) {
    textDocument.put(C.JSONFieldNames.LINK, link);
  }

  /**
   * Add coordinates
   *
   * @param latitude
   * @param longitude
   * @param visualization JSON that contains the visualization data
   */
  final void addCoordinates(double latitude, double longitude,
      JSONObject visualization, JSONObject text) {
    text.put(C.JSONFieldNames.LATITUDE, latitude);
    text.put(C.JSONFieldNames.LONGITUDE, longitude);
    visualization.put(C.JSONFieldNames.LATITUDE, latitude);
    visualization.put(C.JSONFieldNames.LONGITUDE, longitude);
    latitudeField.setDoubleValue(latitude);
    longitudeField.setDoubleValue(longitude);
  }

  /**
   * Add languages
   *
   * @param language      ISO language code
   * @param visualization JSON that contains the visualization data
   */
  final void addLanguage(String language, JSONObject visualization, JSONObject text) {
    text.put(C.JSONFieldNames.LANGUAGE, language);
    visualization.put(C.JSONFieldNames.LANGUAGE, language);
    languageField.setStringValue(language);
  }

  /**
   * Add cluster ID
   *
   * @param clusterString
   * @param visualization JSON that contains the visualization data
   */
  final void addCluster(String clusterString, JSONObject visualization, JSONObject text) {
    long cluster = Long.parseLong(clusterString);
    text.put(C.JSONFieldNames.CLUSTER, cluster);
    visualization.put(C.JSONFieldNames.CLUSTER, cluster);
    clusterField.setLongValue(cluster);
  }

  /**
   * Add the place of publication
   *
   * @param placeOfPublication
   * @param text
   */
  final void addPlaceOfPublication(String placeOfPublication, JSONObject text) {
    text.put(C.JSONFieldNames.PLACE_OF_PUBLICATION, placeOfPublication);
  }

  final void addCorpus(String corpus, JSONObject visualization, JSONObject text) {
    text.put(C.JSONFieldNames.CORPUS, corpus);
    visualization.put(C.JSONFieldNames.CORPUS, corpus);
  }

  /**
   * Return the latitude of the given place of publication
   *
   * @param placeOfPublication
   *
   * @return latitude
   */
  final double getLatitude(String placeOfPublication) {
    if (locations.containsKey(placeOfPublication)) {
      return locations.get(placeOfPublication).latitude;
    }
    return -1.0;
  }

  /**
   * Return the longitude of the given place of publication
   *
   * @param placeOfPublication
   *
   * @return longitude
   */
  final double getLongitude(String placeOfPublication) {
    if (locations.containsKey(placeOfPublication)) {
      return locations.get(placeOfPublication).longitude;
    }
    return -1.0;
  }

  /**
   * Set all fields to default values.
   */
  final void resetFields() {
    idField.setLongValue(0);
    visualizationData.setStringValue("");
    textData.setStringValue("");
    lengthField.setIntValue(0);
    dateField.setStringValue("");
    textField.setStringValue("");
    longitudeField.setDoubleValue(0.0);
    latitudeField.setDoubleValue(0.0);
    languageField.setStringValue("");
  }
}
