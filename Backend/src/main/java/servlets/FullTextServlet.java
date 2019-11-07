package servlets;

import de.uni_stuttgart.searchfilter.common.configuration.C;
import searcher.FullTextSearcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FullTextServlet extends MainServlet {
  private static final long serialVersionUID = 1L;

  public FullTextServlet() {
    super();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    response.setContentType(C.ContentTypes.JSON);
    respond(request, response, new FullTextSearcher());
  }
}
