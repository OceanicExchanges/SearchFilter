package main;

import access.IndexWriteSingleton;
import de.uni_stuttgart.searchfilter.common.access.FilesAccess;
import de.uni_stuttgart.searchfilter.common.configuration.C;
import org.apache.lucene.index.IndexWriter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
  public static void main(String[] args)
    throws IOException, InterruptedException {
    if (args.length < 1) {
      System.err.println("Please provide the path to the configuration file!");
      return;
    }
    C.create(args[0]);
    indexDocuments();
  }

  private static final String GZIP_CSV = "gzipcsv";
  private static final String GZIP_JSON = "gzipjson";

  private static void indexDocuments()
    throws IOException, InterruptedException {
    // Clear previous index
    IndexWriter indexWriter = IndexWriteSingleton.getInstance();
    indexWriter.deleteAll();
    indexWriter.commit();
    // Index new files
    File[] files = new FilesAccess(C.FilePath.document()).getFiles();
    int cores = Runtime.getRuntime().availableProcessors();
    ExecutorService executorService = Executors.newFixedThreadPool(cores);
    String type = C.Process.type();
    AtomicInteger counter = new AtomicInteger(0);
    for (File file : files) {
      switch (type) {
        case GZIP_JSON: {
          break;
        }
        case GZIP_CSV: {
          ZippedCSVDocumentCreator documentCreator =
            new ZippedCSVDocumentCreator(file, counter);
          executorService.execute(documentCreator);
          break;
        }
      }
    }
    executorService.shutdown();
    executorService.awaitTermination(24 * 7, TimeUnit.HOURS);
    IndexWriteSingleton.deleteInstance();
  }
}
