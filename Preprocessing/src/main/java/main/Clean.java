package main;

import org.apache.commons.lang3.StringUtils;

/**
 * This class provides cleaning and normalizations methods for OCR text.
 */
class Clean {

  private static final String[] DELETE_STRINGS =
    {"&nbsp;", "nbsp;", "&nbsp", "nbsp"};
  private static final String SPACE = " ";

  /**
   * @param text Replace a number of substrings in the text to make it more
   *             readable
   * @return cleaned text
   */
  static String cleanTextOCR(String text) {
    for (int i = 0; i < DELETE_STRINGS.length; ++i) {
      text = StringUtils.replace(text, DELETE_STRINGS[i], SPACE);
    }
    return text;
  }

  /**
   * Replace line breaks from text.
   * @param text
   * @return text without linebreaks
   */
  static String cleanLineBreaks(String text) {
    return text.replaceAll("\\r\\n|\\r|\\n", " ");
  }
}
