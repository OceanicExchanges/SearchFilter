package searcher;

import access.IndexReaderSingleton;
import access.IndexSearcherSingleton;
import com.google.common.base.Stopwatch;
import de.mo42.JSONStringBuilder;
import de.uni_stuttgart.searchfilter.common.configuration.C;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DocumentSearcher extends Searcher {
  private IndexSearcher indexSearcher;
  private IndexReader indexReader;
  private JSONStringBuilder json;
  private Stopwatch stopwatch;

  public DocumentSearcher() throws IOException {
    indexSearcher = IndexSearcherSingleton.getInstance();
    indexReader = IndexReaderSingleton.getInstance();
  }

  @Override public String search(Map<String, String[]> queryMap) {
    Query query = query(queryMap);
    log.log(Level.INFO, "Query: " + query.toString());
    return search(query);
  }

  protected String search(Query query) {
    stopwatch = Stopwatch.createStarted();
    TopDocs docs;
    try {
      docs = indexSearcher.search(query, numberDocuments);
    } catch (IOException exception) {
      return handleException(exception);
    }
    log.log(Level.INFO, "Found: " + docs.scoreDocs.length + " documents.");
    json = new JSONStringBuilder();
    json.startJSON();
    addDocuments(docs);
    json.separate();
    addBasicInformation(docs.totalHits.value, docs.scoreDocs.length,
      query.toString());
    json.endJSON();
    return json.toString();
  }

  /**
   * Add information about the documents to the response.
   *
   * @param docs found searching the index
   */
  private void addDocuments(TopDocs docs) {
    json.startJSONArray(C.JSONFieldNames.DOCUMENTS);
    String delimiter = JSONStringBuilder.NOT_DELIMIT;
    for (ScoreDoc doc : docs.scoreDocs) {
      Document document;
      try {
        document = indexReader.document(doc.doc);
      } catch (IOException exception) {
        log.log(Level.WARNING, "Exception: " + exception.getMessage());
        continue;
      }
      json.append(delimiter);
      String d = document.getField(C.FieldNames.VISUALIZATION).stringValue();
      json.append(d);
      delimiter = JSONStringBuilder.DELIMIT;
    }
    json.endJSONArray();
  }

  /**
   * Add basic information like number of hits, response time and topic terms to
   * the response.
   *
   * @param totalHits  total number of hits
   * @param hitsServed number of hits in the response
   * @param query      query string leading to this response
   */
  private void addBasicInformation(long totalHits, int hitsServed,
                                   String query) {
    json.startJSON(C.JSONFieldNames.BASIC_INFORMATION);
    json.put(C.JSONFieldNames.QUERY, query);
    json.separate();
    json.put(C.JSONFieldNames.TOTAL_HITS, totalHits);
    json.separate();
    json.put(C.JSONFieldNames.HITS_SERVED, hitsServed);
    json.separate();
    json.put(C.JSONFieldNames.COMPUTATION_TIME,
      stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
    json.endJSON();
  }
}
