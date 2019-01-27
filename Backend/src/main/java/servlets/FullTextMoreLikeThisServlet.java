package servlets;

import searcher.FullTextMoreLikeThisSearcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FullTextMoreLikeThisServlet extends MainServlet {
  private static final long serialVersionUID = 1L;

  public FullTextMoreLikeThisServlet() {
    super();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    respond(request, response, new FullTextMoreLikeThisSearcher());
  }
}
