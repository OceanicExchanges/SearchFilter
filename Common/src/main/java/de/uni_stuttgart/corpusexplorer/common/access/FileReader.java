package de.uni_stuttgart.corpusexplorer.common.access;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Helper class to read a single file.
 */
public class FileReader {
  /**
   * Given a path to a file, read the entire file and return its content as a
   * string.
   *
   * @param path to the file
   * @return contents as a string
   * @throws IOException in case reading failed
   */
  static String readFile(String path) throws IOException {
    return new String(Files.readAllBytes(Paths.get(path)));
  }

  /**
   * @param fileName
   * @return a stream of strings and each string represents one line of the file
   * @throws IOException
   */
  public static Stream<String> lines(String fileName) throws IOException {
    return Files.lines(Paths.get(fileName));
  }
}
