package servlets;

import searcher.DocumentSearcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DocumentServlet extends MainServlet {
  private static final long serialVersionUID = 1L;

  public DocumentServlet() {
    super();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    respond(request, response, new DocumentSearcher());
  }
}
