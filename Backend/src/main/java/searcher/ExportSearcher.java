package searcher;

import access.IndexSearcherSingleton;
import de.uni_stuttgart.searchfilter.common.configuration.C;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.json.JSONObject;
import searcher.util.CSVStringBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

public class ExportSearcher extends Searcher {

  private IndexSearcher indexSearcher;

  public ExportSearcher() throws IOException {
    indexSearcher = IndexSearcherSingleton.getInstance();
  }

  @Override
  public String search(Map<String, String[]> queryMap) {
    Query query = query(queryMap);
    log.log(Level.INFO, "Query: " + query.toString());
    return search(query);
  }

  public String search(Query query) {
    TopDocs docs;
    try {
      docs = indexSearcher.search(query, C.Serve.numberExport());
    } catch (IOException exception) {
      return handleException(exception);
    }
    CSVStringBuilder csvStringBuilder = new CSVStringBuilder();
    csvStringBuilder.addRecord("text", "date", "publisher",
        "placeOfPublication", "latitude", "longitude", "link", "language",
        "corpus", "cluster");
    for (int i = 0; i < docs.scoreDocs.length; ++i) {
      ScoreDoc doc = docs.scoreDocs[i];
      Document document;
      try {
        document = indexSearcher.doc(doc.doc);
      } catch (IOException exception) {
        log.log(Level.WARNING, "Exception: " + exception.getMessage());
        continue;
      }
      String textData = document.getField(C.FieldNames.TEXT_DATA).stringValue();
      JSONObject textDataJSON = new JSONObject(textData);
      String text = textDataJSON.getString(C.JSONFieldNames.TEXT);
      String date = textDataJSON.getString(C.JSONFieldNames.DATE);
      String publisher = textDataJSON.getString(C.JSONFieldNames.PUBLISHER);
      String placeOfPublication = textDataJSON.getString(
          C.JSONFieldNames.PLACE_OF_PUBLICATION);
      Double latitude = textDataJSON.getDouble(C.JSONFieldNames.LATITUDE);
      Double longitude = textDataJSON.getDouble(C.JSONFieldNames.LONGITUDE);
      String link = textDataJSON.getString(C.JSONFieldNames.LINK);
      String language = textDataJSON.getString(C.JSONFieldNames.LANGUAGE);
      String corpus = textDataJSON.getString(C.JSONFieldNames.CORPUS);
      Long cluster = textDataJSON.getLong(C.JSONFieldNames.CLUSTER);
      csvStringBuilder.addRecord(text, date, publisher, placeOfPublication,
          latitude.toString(), longitude.toString(), link, language, corpus,
          cluster.toString());
    }
    return csvStringBuilder.toString();
  }
}
