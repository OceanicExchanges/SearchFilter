package main;

import de.uni_stuttgart.corpusexplorer.common.configuration.C;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;

import java.io.*;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

public class ZippedCSVDocumentCreator extends DocumentCreator {


  ZippedCSVDocumentCreator(File file) throws IOException {
    super(file);
  }

  @Override public void run() {
    BufferedReader bufferedReader;
    Iterable<CSVRecord> records;
    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
      InputStreamReader inputStreamReader =
        new InputStreamReader(gzipInputStream);
      bufferedReader = new BufferedReader(inputStreamReader);
      records =
        CSVFormat.TDF.withHeader().withSkipHeaderRecord().parse(bufferedReader);
    } catch (IOException exception) {
      exception.printStackTrace();
      return;
    }
    boolean includeNonOpen = C.Process.includeNonOpen();
    for (CSVRecord record : records) {
      String open = record.get(C.CSV.OPEN_DOCUMENT);
      if (includeNonOpen && open.equals("true")) {
        addDocument(record);
      }
    }
    try {
      bufferedReader.close();
    } catch (IOException exception) {
      // Nothing bad happens, if we are unable to close the file
    }
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
    if (locations.containsKey(record.get(C.CSV.PLACE_OF_PUBLICATION))) {
      addCoordinates(getLatitude(record.get(C.CSV.PLACE_OF_PUBLICATION)),
        getLongitude(record.get(C.CSV.PLACE_OF_PUBLICATION)), visualization);
    }
    addLanguages(normalizeLanguages(record.get(C.CSV.LANGUAGE)), visualization);
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
