package access;

import de.uni_stuttgart.corpusexplorer.common.configuration.C;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * This class represents a single instance of an index writer for a Lucene
 * repository.
 *
 * @author mo
 */
public class IndexWriteSingleton {

  private static IndexWriter indexWriterInstance = null;

  private IndexWriteSingleton() {}

  /**
   * Return single instance of the temporary Lucene index writer.
   *
   * @return index writer
   * @throws IOException in case {@link IndexWriter} could not be opened
   */
  public static IndexWriter getInstance() throws IOException {
    if (indexWriterInstance == null) {
      FSDirectory directory = FSDirectory.open(Paths.get(C.FilePath.index()));
      IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
      indexWriterInstance = new IndexWriter(directory, config);
    }
    return indexWriterInstance;
  }

  /**
   * Close the writer and delete the instance.
   *
   * @throws IOException in case {@link IndexWriter} could not be closed
   */
  public static void deleteInstance() throws IOException {
    if (indexWriterInstance != null) {
      indexWriterInstance.close();
      indexWriterInstance = null;
    }
  }
}
