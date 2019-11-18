package searcher.util;

import java.util.List;

public class CSVStringBuilder {

  private static final String SEPARATOR = ",";
  private static final String NO_SEPARATOR = "";
  private static final String LINE_BREAK = "\n";

  private final StringBuilder stringBuilder;

  public CSVStringBuilder() {
    this.stringBuilder = new StringBuilder();
  }

  private static String escape(String string) {
    if(string.contains("\"")) {
      return string.replace("\"", "\"\"");
    } else {
      return string;
    }
  }

  public void addRecord(String ... record) {
    String delimiter = NO_SEPARATOR;
    for (String element : record) {
      stringBuilder.append(delimiter);
      if(element.contains(",")) {
        stringBuilder.append("\"");
        stringBuilder.append(escape(element));
        stringBuilder.append("\"");
      } else {
        stringBuilder.append(escape(element));
      }
      delimiter = SEPARATOR;
    }
    stringBuilder.append(LINE_BREAK);
  }

  @Override
  public String toString() {
    return stringBuilder.toString();
  }
}
