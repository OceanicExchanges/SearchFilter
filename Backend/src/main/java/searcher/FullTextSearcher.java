package searcher;

import access.IndexSearcherSingleton;
import de.mo42.JSONStringBuilder;
import de.uni_stuttgart.corpusexplorer.common.configuration.C;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

public class FullTextSearcher extends Searcher {

  private int pageSize;
  private IndexSearcher indexSearcher;

  public FullTextSearcher() throws IOException {
    indexSearcher = IndexSearcherSingleton.getInstance();
    pageSize = C.Serve.pageSize();
  }

  @Override
  public String search(Map<String, String[]> queryMap) {
    Query query = query(queryMap);
    log.log(Level.INFO, "Query: " + query.toString());
    int pageNumber = queryIndex(queryMap);
    return search(query, pageNumber * pageSize);
  }

  public String search(Query query, int hits) {
    TopDocs docs;
    try {
      docs = indexSearcher.search(query, hits);
    } catch (IOException exception) {
      return handleException(exception);
    }
    int start = docs.scoreDocs.length - pageSize - 1;
    if (start < 0) {
      start = 0;
    }
    JSONStringBuilder json = new JSONStringBuilder();
    json.startJSON();
    json.startJSONArray(C.JSONFieldNames.DOCUMENTS);
    String delimiter = JSONStringBuilder.NOT_DELIMIT;
    for (int i = start; i < docs.scoreDocs.length; ++i) {
      ScoreDoc doc = docs.scoreDocs[i];
      Document document;
      try {
        document = indexSearcher.doc(doc.doc);
      } catch (IOException exception) {
        log.log(Level.WARNING, "Exception: " + exception.getMessage());
        continue;
      }
      json.append(delimiter);
      String d = document.getField(C.FieldNames.TEXT_DATA).stringValue();
      json.append(d);
      delimiter = JSONStringBuilder.DELIMIT;
    }
    json.endJSONArray();
    json.endJSON();
    return json.toString();

  }

  /**
   * Return the page number. If there is no page parameter, return '0' for the
   * first page.
   *
   * @param queryParameterMap possibly containing a page parameter
   * @return page number
   */
  private int queryIndex(final Map<String, String[]> queryParameterMap) {
    final String page = queryParameterMap.get("page")[0];
    return page != null ? Integer.parseInt(page) : 0;
  }
}
