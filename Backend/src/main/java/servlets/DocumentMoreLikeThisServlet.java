package servlets;

import searcher.DocumentMoreLikeThisSearcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DocumentMoreLikeThisServlet extends MainServlet {
  private static final long serialVersionUID = 1L;

  public DocumentMoreLikeThisServlet() {
    super();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    respond(request, response, new DocumentMoreLikeThisSearcher());
  }
}
