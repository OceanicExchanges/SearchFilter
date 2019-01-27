package de.uni_stuttgart.corpusexplorer.common.access;

import de.uni_stuttgart.corpusexplorer.common.configuration.C;
import org.json.JSONArray;

import java.io.IOException;

/**
 * This class provides access to a single file that contains a set of terms for
 * each topic.
 */
public class TopicTermsAccess {

  private static JSONArray topicTermsArray = null;

  private TopicTermsAccess() {
  }

  /**
   * Return an array, where each element contains the topic ID and a list of
   * terms.
   *
   * @return a JSON array containing the topic terms
   *
   * @throws IOException in case of failure opening and reading the
   */
  private static JSONArray getTopicTerms() throws IOException {
    String file = FileReader.readFile(C.FilePath.topicTermsFile());
    return new JSONArray(file);
  }

  /**
   * Return an array, where each element contains the topic ID and a list of
   * terms.
   *
   * @return a JSON array containing the topic terms.
   *
   * @throws IOException in case of failure opening and reading the file
   */
  public static JSONArray getJSONArray() throws IOException {
    if (topicTermsArray == null) {
      topicTermsArray = getTopicTerms();
    }
    return topicTermsArray;
  }
}
