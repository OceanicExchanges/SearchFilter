package de.uni_stuttgart.corpusexplorer.common.access;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * Convenience class for accessing all files in a directory.
 */
public class FilesAccess {
  private File[] files;

  /**
   * Create an instance that provides access to all text files in a folder.
   *
   * @param path to a folder of documents
   */
  public FilesAccess(String path) {
    files = new File(path).listFiles();
  }

  /**
   * Create an instance that provides access to all files in a folder that start
   * with a certain prefix.
   *
   * @param path to the collection of documents
   */
  public FilesAccess(String path, String startWith) {
    FilenameFilter fileNameFilter = (dir, name) -> name.startsWith(startWith);
    files = new File(path).listFiles(fileNameFilter);
  }

  /**
   * Return raw array of files.
   *
   * @return array of files
   */
  public File[] getFiles() {
    return files;
  }
}
