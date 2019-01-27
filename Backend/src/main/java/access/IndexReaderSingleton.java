package access;

import de.uni_stuttgart.corpusexplorer.common.configuration.C;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * This class represents a single instance of a Lucene {@link IndexReader}.
 */
public class IndexReaderSingleton {
  private static IndexReader indexReader = null;

  private IndexReaderSingleton() {}

  /**
   * Ensure that there is <i>one</i> instance of a Lucene {@link IndexReader}.
   *
   * @return {@link IndexReader}
   *
   * @throws IOException in case opening the index failed
   */
  public static IndexReader getInstance() throws IOException {
    if (indexReader == null) {
      FSDirectory directory = FSDirectory.open(Paths.get(C.FilePath.index()));
      indexReader = DirectoryReader.open(directory);
    }
    return indexReader;
  }
}
