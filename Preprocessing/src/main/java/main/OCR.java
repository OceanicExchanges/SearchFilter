package main;

import org.apache.commons.lang3.StringUtils;

public class OCR {

  private static final String[] DELETE_STRINGS =
    {"&nbsp;", "nbsp;", "&nbsp", "nbsp"};
  private static final String SPACE = " ";

  /**
   * Replace a number of substrings in the text to make it more readable
   *
   * @param text
   * @return cleaned text
   */
  final public static String cleanText(String text) {
    for (int i = 0; i < DELETE_STRINGS.length; ++i) {
      text = StringUtils.replace(text, DELETE_STRINGS[i], SPACE);
    }
    return text;
  }
}
