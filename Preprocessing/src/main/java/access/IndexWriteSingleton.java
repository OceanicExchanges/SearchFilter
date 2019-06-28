package access;

import de.uni_stuttgart.searchfilter.common.configuration.C;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
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
      IndexWriterConfig config = new IndexWriterConfig(buildAnalyzer());
      indexWriterInstance = new IndexWriter(directory, config);
    }
    return indexWriterInstance;
  }

  private static Analyzer buildAnalyzer() throws IOException {
    return CustomAnalyzer.builder(Paths.get(C.FilePath.project()))
        .withTokenizer(StandardTokenizerFactory.class)
        .addTokenFilter(LowerCaseFilterFactory.class)
        .addTokenFilter(StopFilterFactory.class,
          "ignoreCase", "false",
          "words", C.FilePath.stopwords(),
          "format", "wordset")
        .build();
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
