package main;

import de.uni_stuttgart.searchfilter.common.configuration.C;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class ZippedCSVDocumentCreator extends DocumentCreator {

  protected static final Logger log = Logger.getLogger(
      ZippedCSVDocumentCreator.class.getName());

  ZippedCSVDocumentCreator(File file, AtomicInteger counter)
      throws IOException {
    super(file, counter);
  }

  @Override
  public void run() {
    BufferedReader bufferedReader;
    Iterable<CSVRecord> records;
    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
      InputStreamReader inputStreamReader = new InputStreamReader(
          gzipInputStream);
      bufferedReader = new BufferedReader(inputStreamReader);
      records = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord().parse(
          bufferedReader);
    } catch (IOException exception) {
      exception.printStackTrace();
      return;
    }
    boolean includeNonOpen = C.Process.includeNonOpen();
    for (CSVRecord record : records) {
      String open = record.get(C.CSV.OPEN_DOCUMENT).toLowerCase();
      if (includeNonOpen || open.equals("true")) {
        addDocument(record);
      }
    }
    try {
      bufferedReader.close();
    } catch (IOException exception) {
      // Nothing bad happens, if we are unable to close the file
    }
    log.log(Level.INFO,
        "Finished indexing " + counter.incrementAndGet() + "  files.");
    logger.log(Level.INFO, "Finished file: " + file.toString());
  }

  private void addDocument(CSVRecord record) {
    JSONObject text = new JSONObject();
    JSONObject visualization = new JSONObject();
    addIdentification(record.get(C.CSV.ID), visualization, text);
    addTextLength(record.get(C.CSV.TEXT), visualization);
    addDate(record.get(C.CSV.DATE), visualization, text);
    addText(record.get(C.CSV.TEXT), text);
    addPublisher(record.get(C.CSV.PUBLISHER), text);
    addCluster(record.get(C.CSV.CLUSTER), visualization);
    addLink(record.get(C.CSV.LINK), text);
    if (locations.containsKey(record.get(C.CSV.PLACE_OF_PUBLICATION))) {
      addCoordinates(getLatitude(record.get(C.CSV.PLACE_OF_PUBLICATION)),
          getLongitude(record.get(C.CSV.PLACE_OF_PUBLICATION)), visualization);
    } else if (locations.containsKey(record.get(C.CSV.SOURCE))) {
      addCoordinates(getLatitude(record.get(C.CSV.SOURCE)),
          getLongitude(record.get(C.CSV.SOURCE)), visualization);
    }
    addLanguage(record.get(C.CSV.LANGUAGE), visualization);
    super.visualizationData.setStringValue(visualization.toString());
    super.textData.setStringValue(text.toString());
    try {
      writer.addDocument(super.document);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
    resetFields();
  }
}
