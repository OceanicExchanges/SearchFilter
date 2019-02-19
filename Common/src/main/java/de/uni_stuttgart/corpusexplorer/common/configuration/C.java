package de.uni_stuttgart.corpusexplorer.common.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class C {

  private static Properties properties = null;

  public static void create(String path) throws IOException {
    properties = new Properties();
    InputStream inputStream = new FileInputStream(path);
    properties.load(inputStream);
  }

  private static String getS(String property) {
    return properties.getProperty(property);
  }

  private static int getI(String property) {
    return Integer.parseInt(properties.getProperty(property));
  }

  private static boolean getB(String property) {
    return properties.getProperty(property).equals("true");
  }

  private static float getF(String property) {
    return Float.parseFloat(properties.getProperty(property));
  }

  public static class Serve {
    private static String PAGE_SIZE = "serve.page.size";

    /**
     * @return the page size (the number of texts served).
     */
    public static int pageSize() {
      return C.getI(PAGE_SIZE);
    }

    private static String NUMBER_DOCUMENTS = "serve.number.documents";

    /**
     * @return the maximum number of documents that should be served.
     */
    public static int numberDocuments() {
      return C.getI(NUMBER_DOCUMENTS);
    }

    private static String MAX_EDIT_DISTANCE = "serve.maxEditDistance";

    /**
     * @return the maximum number of character edits of a query term
     */
    public static int maxEditDistance() { return C.getI(MAX_EDIT_DISTANCE); }
  }

  public static class FilePath {

    private static final String PROJECT = "file.path.project";

    /**
     * @return path to the project files
     */
    public static String project() {
      return C.getS(PROJECT);
    }

    private static final String INDEX = "file.path.index";

    /**
     * @return path to the index
     */
    public static String index() {
      return C.getS(PROJECT) + C.getS(INDEX);
    }

    private static final String DOCUMENT = "file.path.document";

    /**
     * @return path to the documents
     */
    public static String document() {
      return C.getS(PROJECT) + C.getS(DOCUMENT);
    }

    private static final String LOCATIONS = "file.locations";

    /**
     * @return path to locations file
     */
    public static String locationsFile() {
      return C.getS(PROJECT) + C.getS(LOCATIONS);
    }
  }

  public static class Process {
    private static final String INCLUDE_NON_OPEN = "process.includeNonOpen";

    /**
     * @return true if non-open articles should be included
     */
    public static boolean includeNonOpen() {
      return C.getB(INCLUDE_NON_OPEN);
    }

    private static final String TYPE = "process.type";

    /**
     * @return type of files to be processed
     */
    public static String type() {
      return C.getS(TYPE);
    }
  }

  public static class CSV {
    public static final int ID = 0;
    public static final int DATE = 7;
    public static final int TEXT = 10;
    public static final int OPEN_DOCUMENT = 24;
    public static final int LANGUAGE = 37;
    public static final int PLACE_OF_PUBLICATION = 38;
    public static final int LINK = 13;
    public static final int PUBLISHER = 38;
  }

  public static class JSON {
    public static final String ID = "cid";
    public static final String DATE = "date";
    public static final String TEXT = "text";
    public static final String LINK = "page_access";
    public static final String PUBLISHER = "title";
  }

  public static class ContentTypes {
    public static final String JSON = "application/json";
  }

  public static class FieldNames {
    // Fields in the CSV files
    public static final String UID = "cid";
    public static final String DATE = "date";
    public static final String TEXT = "text";
    public static final String PLACE_OF_PUBLICATION = "placeOfPublication";
    public static final String LANGUAGE = "language";
    public static final String TITLE = "title";
    public static final String LINK = "url";

    // Fields of computed information
    public static final String LENGTH = "length";
    public static final String ID = "id";

    public static final String VISUALIZATION = "visualization";
    public static final String TEXT_DATA = "textData";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
  }

  public static class JSONFieldNames {
    public static final String PARSE_ERROR = "parseError";
    public static final String QUERY = "query";
    public static final String DOCUMENTS = "documents";
    public static final String ID = "id";
    public static final String DATE = "date";
    public static final String TEXT = "text";
    public static final String LINK = "link";
    public static final String TEXT_LENGTH = "textLength";
    public static final String TOTAL_HITS = "totalHits";
    public static final String BASIC_INFORMATION = "basicInformation";
    public static final String HITS_SERVED = "hitsServed";
    public static final String COMPUTATION_TIME = "computationTime";
    public static final String EXCEPTION = "exception";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String PUBLISHER = "publisher";
    public static final String LANGUAGE = "language";
  }
}

