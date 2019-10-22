package servlets;

import de.uni_stuttgart.searchfilter.common.configuration.C;
import searcher.ExportSearcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ExportServlet extends MainServlet {
  public ExportServlet() { super(); }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    response.setContentType(C.ContentTypes.CSV);
    response.setCharacterEncoding(C.ContentEncoding.UTF8);
    respond(request, response, new ExportSearcher());
  }
}
