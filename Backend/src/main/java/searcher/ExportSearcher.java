package searcher;

import access.IndexSearcherSingleton;
import de.uni_stuttgart.searchfilter.common.configuration.C;
import org.apache.lucene.document.Document;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

public class ExportSearcher extends Searcher {

  private IndexSearcher indexSearcher;

  public ExportSearcher() throws IOException {
    indexSearcher = IndexSearcherSingleton.getInstance();
  }

  @Override public String search(Map<String, String[]> queryMap) {
    Query query = query(queryMap);
    log.log(Level.INFO, "Query: " + query.toString());
    return search(query);
  }

  private static final char CSV_SEPARATOR = '\t';
  private static final char CSV_LINE_BREAK = '\n';

  public String search(Query query) {
    TopDocs docs;
    try {
      docs = indexSearcher.search(query, C.Serve.numberDocuments());
    } catch (IOException exception) {
      return handleException(exception);
    }
    StringBuilder csvString = new StringBuilder();
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
      csvString.append(date);
      csvString.append(CSV_SEPARATOR);
      csvString.append(text);
      csvString.append(CSV_LINE_BREAK);
    }
    return csvString.toString();

  }
}
