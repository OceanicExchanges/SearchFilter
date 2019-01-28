package main;

import access.IndexWriteSingleton;
import access.Location;
import access.LocationSingleton;
import de.uni_stuttgart.corpusexplorer.common.configuration.C;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public abstract class DocumentCreator implements Runnable {
  static final Logger logger =
    Logger.getLogger(DocumentCreator.class.getName());
  final IndexWriter writer;
  final Document document;

  File file;
  IndexWriter indexWriter;
  Map<String, Location> locations;
  FieldType textFieldType;
  FieldType languageFieldType;

  // Index fields
  final LongPoint idField;
  final StoredField visualizationData;
  final StoredField textData;
  final IntPoint lengthField;
  final StringField dateField;
  final Field textField;
  final DoublePoint longitudeField;
  final DoublePoint latitudeField;
  final Field languageField;

  DocumentCreator(File file) throws IOException {
    this.file = file;
    this.indexWriter = IndexWriteSingleton.getInstance();
    this.locations = LocationSingleton.getInstance();
    // Setup the field type for text
    textFieldType = new FieldType();
    textFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
    // Texts are stored with other data in textDataField
    textFieldType.setStored(false);
    // This is needed for the more like this functionality
    textFieldType.setStoreTermVectors(true);
    // Setup the field type for language data
    languageFieldType = new FieldType();
    // Texts are stored with other data in textDataField
    languageFieldType.setIndexOptions(IndexOptions.DOCS);
    languageFieldType.setStored(false);
    // This is needed for the more like this functionality
    languageFieldType.setStoreTermVectors(false);
    locations = LocationSingleton.getInstance();
    // Setup the fields
    idField = new LongPoint(C.FieldNames.ID, 0);
    visualizationData = new StoredField(C.FieldNames.VISUALIZATION, "");
    textData = new StoredField(C.FieldNames.TEXT_DATA, "");
    lengthField = new IntPoint(C.FieldNames.LENGTH, 0);
    dateField = new StringField(C.FieldNames.DATE, "", Field.Store.NO);
    textField = new Field(C.FieldNames.TEXT, "", textFieldType);
    longitudeField = new DoublePoint(C.FieldNames.LONGITUDE, 0.0);
    latitudeField = new DoublePoint(C.FieldNames.LATITUDE, 0.0);
    languageField = new Field(C.FieldNames.LANGUAGE, "", languageFieldType);
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
    text = cleanText(text);
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
   * @param link
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
                            JSONObject visualization) {
    visualization.put(C.JSONFieldNames.LATITUDE, latitude);
    visualization.put(C.JSONFieldNames.LONGITUDE, longitude);
    latitudeField.setDoubleValue(latitude);
    longitudeField.setDoubleValue(longitude);
  }

  /**
   * Add languages
   *
   * @param languages
   * @param visualization JSON that contains the visualization data
   */
  final void addLanguages(String[] languages, JSONObject visualization) {
    visualization.put(C.JSONFieldNames.LANGUAGE, new JSONArray(languages));
    languageField.setStringValue(String.join(" ", languages));
  }

  /**
   * Return the latitude of the given place of publication
   *
   * @param placeOfPublication
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

  /**
   * Return an array of normalized language identifiers
   *
   * @param languages
   * @return
   */
  final String[] normalizeLanguages(String languages) {
    String[] languagesArray = languages.split(",");
    for (int i = 0; i < languagesArray.length; ++i) {
      switch (languagesArray[i]) {
        case "German":
          languagesArray[i] = "de";
          break;
        case "French":
          languagesArray[i] = "fr";
          break;
      }
    }
    return languagesArray;
  }

  /**
   * Replace a number of substrings in the text to make it more readable
   *
   * @param text
   * @return cleaned text
   */
  final String cleanText(String text) {
    final String[] searchStrings = {"&nbsp;", "nbsp;", "&nbsp", "nbsp"};
    final String[] replacements = {" ", " ", " ", " "};
    for (int i = 0; i < searchStrings.length; ++i) {
      text = StringUtils.replace(text, searchStrings[i], replacements[i]);
    }
    return text;
  }
}

