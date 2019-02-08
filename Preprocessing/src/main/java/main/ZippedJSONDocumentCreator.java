package main;

import de.uni_stuttgart.corpusexplorer.common.configuration.C;
import org.json.JSONObject;

import java.io.*;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

public class ZippedJSONDocumentCreator extends DocumentCreator {
  public ZippedJSONDocumentCreator(File file) throws IOException {
    super(file);
  }

  @Override public void run() {
    FileInputStream fileInputStream;
    GZIPInputStream gzipInputStream;
    try {
      fileInputStream = new FileInputStream(file);
      gzipInputStream = new GZIPInputStream(fileInputStream);
    } catch (FileNotFoundException exception) {
      exception.printStackTrace();
      return;
    } catch (IOException exception) {
      exception.printStackTrace();
      return;
    }
    InputStreamReader inputStreamReader =
      new InputStreamReader(gzipInputStream);
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    try {
      for (String line; (line = bufferedReader.readLine()) != null; ) {
        addDocument(new JSONObject(line));
      }
    } catch (IOException exception) {
      exception.printStackTrace();
    }
    logger.log(Level.INFO, "Finished file: " + file.toString());
  }

  private void addDocument(JSONObject document) {
    JSONObject text = new JSONObject();
    JSONObject visualization = new JSONObject();
    addIdentification(document.getString(C.JSON.ID), visualization, text);
    addTextLength(document.getString(C.JSON.TEXT), visualization);
    addDate(document.getString(C.JSON.DATE), visualization, text);
    addText(document.getString(C.JSON.TEXT), text);
    addPublisher(document.getString(C.JSON.PUBLISHER), text);
    addLink(document.getString(C.JSON.LINK), text);
    super.visualizationData.setStringValue(visualization.toString());
    super.textData.setStringValue(text.toString());
    try {
      writer.addDocument(this.document);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
    resetFields();
  }
}
