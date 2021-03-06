package access;

import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

/**
 * This class represents a single instance of a Lucene {@link IndexSearcher}.
 */
public class IndexSearcherSingleton {

  private static IndexSearcher indexSearcher = null;

  private IndexSearcherSingleton() {}

  /**
   * Ensure that there is <i>one</i> instance of a Lucene {@link
   * IndexSearcher}.
   *
   * @return {@link IndexSearcher}
   * @throws IOException in case opening the index failed
   */
  public static IndexSearcher getInstance() throws IOException {
    if (indexSearcher == null) {
      indexSearcher = new IndexSearcher(IndexReaderSingleton.getInstance());
    }
    return indexSearcher;
  }
}
