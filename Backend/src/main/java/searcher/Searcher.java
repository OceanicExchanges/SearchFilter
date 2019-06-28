package searcher;

import de.uni_stuttgart.searchfilter.common.configuration.C;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.search.Query;
import org.json.JSONObject;
import searcher.util.LuceneQueryBuilder;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides the basic structure of a searcher. A searcher accepts a
 * {@link Map} of URL parameters, builds a {@link Query} and computes the
 * response as a {@link String}.
 */
public abstract class Searcher {

  protected static final Logger log =
    Logger.getLogger(Searcher.class.getName());

  final int numberDocuments;

  Searcher() {
    numberDocuments = C.Serve.numberDocuments();
  }

  /**
   * Main method to compute the response given a parameter map.
   *
   * @param queryMap of parameters (search terms)
   * @return JSON string containing the response
   */
  public abstract String search(Map<String, String[]> queryMap);

  /**
   * Helper method to build a Lucene {@link Query}.
   *
   * @param queryMap of parameters (search terms)
   * @return {@link Query}
   */
  protected Query query(Map<String, String[]> queryMap) {
    LuceneQueryBuilder luceneQueryBuilder = new LuceneQueryBuilder(queryMap);
    return luceneQueryBuilder.build();
  }

  /**
   * Return a JSON containing information about the exception.
   *
   * @param exception to be handled
   * @return JSON
   */
  String handleException(Exception exception) {
    log.log(Level.WARNING, "Exception: " + exception.getMessage());
    JSONObject json = new JSONObject();
    json.put(C.JSONFieldNames.EXCEPTION, exception.getMessage());
    return json.toString();
  }

  String handleMissingParameter(String parameter) {
    log.log(Level.WARNING, "Missing URL parameter: " + parameter);
    JSONObject json = new JSONObject();
    json
      .put(C.JSONFieldNames.PARSE_ERROR, "Missing URL parameter: " + parameter);
    return json.toString();
  }

  /**
   * Given the ID  of a document, build a query that finds the document.
   *
   * @param id of a document
   * @return query to find the document
   */
  Query idQuery(long id) {
    return LongPoint.newExactQuery("id", id);
  }
}
