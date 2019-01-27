package configuration;

import de.uni_stuttgart.corpusexplorer.common.configuration.C;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;

public class PropertiesServletContextListener
    implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      String config = sce.getServletContext().getRealPath(
          "/WEB-INF/classes/config.properties");
      C.create(config);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) { }
}
