package main;

import org.apache.commons.lang3.StringUtils;

/**
 * This class provides rule-based OCR error correction.
 */
class OCR {

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
}
