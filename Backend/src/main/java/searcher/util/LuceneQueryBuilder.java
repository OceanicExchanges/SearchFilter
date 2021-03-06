package searcher.util;

import de.uni_stuttgart.searchfilter.common.configuration.C;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Build a Lucene @{Link Query} using a URL map of parameters.
 */
public class LuceneQueryBuilder {

  private static final String PRIMARY = "primary";
  private static final String SELECTIONS = "selections";
  private static final String EXCLUSIONS = "exclusions";
  private static final String LENGTH = "length";
  private static final String TIME = "time";
  private static final String LONGITUDE = "longitude";
  private static final String LATITUDE = "latitude";
  private static final String LANGUAGE = "language";
  private static final String CLUSTER = "cluster";

  /**
   * Map that contains all the query fields
   */
  private final Map<String, String[]> queryMap;

  private final int maxEditDistance;

  private static final int PREFIX_LENGTH = 5;

  public LuceneQueryBuilder(Map<String, String[]> queryMap) {
    this.queryMap = queryMap;
    this.maxEditDistance = C.Serve.maxEditDistance();
  }

  /**
   * Return a list of {@link org.apache.lucene.search.BooleanClause} for a given
   * string array and an operator.
   *
   * @param terms to be added to the list
   * @param occur {@link org.apache.lucene.search.BooleanClause.Occur}
   * @return list of {@link org.apache.lucene.search.BooleanClause}
   */
  private static List<BooleanClause> getTextClauses(String[] terms,
                                                    BooleanClause.Occur occur,
                                                    int maxEditDistance) {
    List<BooleanClause> booleanClauseList = new ArrayList<>();
    for (String primaryTerm : terms) {
      primaryTerm = primaryTerm.toLowerCase();
      FuzzyQuery fuzzyQuery =
        new FuzzyQuery(new Term(C.FieldNames.TEXT, primaryTerm),
          maxEditDistance, PREFIX_LENGTH);
      BooleanClause booleanClause = new BooleanClause(fuzzyQuery, occur);
      booleanClauseList.add(booleanClause);
    }
    return booleanClauseList;
  }

  /**
   * Build a {@link Query} from an URL parameter map.
   *
   * @return {@link Query}
   */
  public Query build() {
    BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
    if (queryMap.containsKey(PRIMARY)) {
      String[] primary = queryMap.get(PRIMARY)[0].split(",");
      List<BooleanClause> clauses =
        getTextClauses(primary, BooleanClause.Occur.MUST, this.maxEditDistance);
      clauses.forEach(booleanQueryBuilder::add);
    }
    if (queryMap.containsKey(SELECTIONS)) {
      String[] selections = queryMap.get(SELECTIONS)[0].split(",");
      List<BooleanClause> clauses =
        getTextClauses(selections, BooleanClause.Occur.SHOULD,
          this.maxEditDistance);
      clauses.forEach(booleanQueryBuilder::add);
    }
    if (queryMap.containsKey(EXCLUSIONS)) {
      String[] exclusions = queryMap.get(EXCLUSIONS)[0].split(",");
      List<BooleanClause> clauses =
        getTextClauses(exclusions, BooleanClause.Occur.MUST_NOT,
          this.maxEditDistance);
      clauses.forEach(booleanQueryBuilder::add);
    }
    if (queryMap.containsKey(LENGTH)) {
      String[] range = queryMap.get(LENGTH)[0].split(",");
      booleanQueryBuilder
        .add(getIntRange(C.FieldNames.LENGTH, range), BooleanClause.Occur.MUST);
    }
    if (queryMap.containsKey(TIME)) {
      String[] range = queryMap.get(TIME)[0].split(",");
      booleanQueryBuilder.add(getStringRange(C.FieldNames.DATE, range),
        BooleanClause.Occur.MUST);
    }
    if (queryMap.containsKey(LONGITUDE)) {
      String[] range = queryMap.get(LONGITUDE)[0].split(",");
      booleanQueryBuilder.add(getDoubleRange(C.FieldNames.LONGITUDE, range),
        BooleanClause.Occur.MUST);
    }
    if (queryMap.containsKey(LATITUDE)) {
      String[] range = queryMap.get(LATITUDE)[0].split(",");
      booleanQueryBuilder.add(getDoubleRange(C.FieldNames.LATITUDE, range),
        BooleanClause.Occur.MUST);
    }
    if (queryMap.containsKey(LANGUAGE)) {
      String language = queryMap.get(LANGUAGE)[0].split(",")[0];
      booleanQueryBuilder
        .add(getTermQuery(C.FieldNames.LANGUAGE, language), BooleanClause.Occur.MUST);
    }
    if (queryMap.containsKey(CLUSTER)) {
      long cluster = Long.parseLong(queryMap.get(CLUSTER)[0].split(",")[0]);
      booleanQueryBuilder.add(getTermQuery(CLUSTER, cluster), BooleanClause.Occur.MUST);
    }
    return booleanQueryBuilder.build();
  }

  private Query getTermQuery(final String fieldName, String term) {
    return new TermQuery(new Term(fieldName, term));
  }

  private Query getTermQuery(final String fieldName, long number) {
    return LongPoint.newExactQuery(fieldName, number);
  }

  /**
   * Create a boolean clause for a given integer range.
   *
   * @param fieldName on which the range is applied
   * @param range     array of length two: [lower, upper]
   * @return Int range query
   */
  private Query getIntRange(final String fieldName, String[] range) {
    int lower = Integer.parseInt(range[0]);
    int upper = Integer.parseInt(range[1]);
    return IntPoint.newRangeQuery(fieldName, lower, upper);
  }

  /**
   * Create a boolean clause for a given string range.
   *
   * @param fieldName on which the range is applied
   * @param range     array of length two: [lower, upper]
   * @return String range query
   */
  private Query getStringRange(final String fieldName, String[] range) {
    return TermRangeQuery
      .newStringRange(fieldName, range[0], range[1], true, true);
  }


  /**
   * Create a Boolean clause for a given double range.
   *
   * @param fieldName on which the range is applied
   * @param range     array of length two: [lower, upper]
   * @return Double range query
   */
  private Query getDoubleRange(final String fieldName, String[] range) {
    double lower = Double.parseDouble(range[0]);
    double upper = Double.parseDouble(range[1]);
    return DoublePoint.newRangeQuery(fieldName, lower, upper);
  }
}
