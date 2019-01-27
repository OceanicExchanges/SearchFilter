package searcher;

import access.IndexReaderSingleton;
import access.IndexSearcherSingleton;
import de.uni_stuttgart.corpusexplorer.common.configuration.C;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.Map;

public class DocumentMoreLikeThisSearcher extends Searcher {

  private IndexReader indexReader;

  private IndexSearcher indexSearcher;

  public DocumentMoreLikeThisSearcher() throws IOException {
    indexSearcher = IndexSearcherSingleton.getInstance();
    indexReader = IndexReaderSingleton.getInstance();
  }

  @Override public String search(Map<String, String[]> queryParameterMap) {
    if (queryParameterMap.containsKey(C.FieldNames.ID)) {
      long id = Long.parseLong(queryParameterMap.get(C.FieldNames.ID)[0]);
      return search(id);
    } else {
      return handleMissingParameter(C.FieldNames.ID);
    }
  }

  /**
   * Given the ID of a document, retrieve similar documents.
   *
   * @param id of a document
   * @return response with documents similar to provided document
   */
  private String search(long id) {
    Query query = idQuery(id);
    TopDocs docs;
    try {
      docs = indexSearcher.search(query, 1);
    } catch (IOException exception) {
      return handleException(exception);
    }
    MoreLikeThis moreLikeThis = new MoreLikeThis(indexReader);
    moreLikeThis.setFieldNames(
      new String[]{C.FieldNames.TITLE, C.FieldNames.TEXT});
    moreLikeThis.setAnalyzer(new StandardAnalyzer());
    Query moreLikeThisQuery;
    try {
      moreLikeThisQuery = moreLikeThis.like(docs.scoreDocs[0].doc);
      DocumentSearcher documentSearcher = new DocumentSearcher();
      return documentSearcher.search(moreLikeThisQuery);
    } catch (IOException exception) {
      return handleException(exception);
    }
  }
}
