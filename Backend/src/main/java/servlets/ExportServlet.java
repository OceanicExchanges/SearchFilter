package servlets;

import searcher.ExportSearcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ExportServlet extends MainServlet {
  public ExportServlet() { super(); }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    respond(request, response, new ExportSearcher());
  }
}
