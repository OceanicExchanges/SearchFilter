package main;

import access.IndexWriteSingleton;
import de.uni_stuttgart.corpusexplorer.common.access.FilesAccess;
import de.uni_stuttgart.corpusexplorer.common.configuration.C;
import org.apache.lucene.index.IndexWriter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    for (File file : files) {
      switch (type) {
        case "json": {
          ZippedJSONDocumentCreator documentCreator =
            new ZippedJSONDocumentCreator(file);
          executorService.execute(documentCreator);
          break;
        }
        case "csv": {
          ZippedCSVDocumentCreator documentCreator =
            new ZippedCSVDocumentCreator(file);
          executorService.execute(documentCreator);
          break;
        }
      }
    }
    executorService.shutdown();
    executorService.awaitTermination(48, TimeUnit.HOURS);
    IndexWriteSingleton.deleteInstance();
  }
}
