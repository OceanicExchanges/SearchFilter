package servlets;

import de.uni_stuttgart.searchfilter.common.configuration.C;
import searcher.Searcher;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public abstract class MainServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private PrintWriter writer;
  private Map<String, String[]> queryParameterMap;

  MainServlet() {
    super();
  }

  private void prepareResponse(HttpServletRequest request,
                               HttpServletResponse response)
    throws IOException {
    response.setContentType(C.ContentTypes.CSV);
    response.setCharacterEncoding(C.ContentEncoding.UTF8);
    writer = response.getWriter();
    queryParameterMap = request.getParameterMap();
  }

  private String computeResponse(Searcher queryAnalyzer) {
    return queryAnalyzer.search(queryParameterMap);
  }

  private void finishResponse(HttpServletResponse response, String result) {
    writer.write(result);
    writer.close();
  }

  void respond(HttpServletRequest request, HttpServletResponse response,
               Searcher analyzer) throws IOException {
    prepareResponse(request, response);
    String result = computeResponse(analyzer);
    finishResponse(response, result);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    response.getWriter().append("Served at: ").append(request.getContextPath());
  }

  protected void doPost(HttpServletRequest request,
                        HttpServletResponse response) throws IOException {
    doGet(request, response);
  }
}
