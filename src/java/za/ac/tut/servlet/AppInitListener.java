

package za.ac.tut.servlet;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import za.ac.tut.databaseConnection.DatabaseInitializer;
 

@WebListener
public class AppInitListener implements ServletContextListener {
    
       @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Tickify: Initializing database...");
        DatabaseInitializer.initialize();
        System.out.println("Tickify: Database ready.");
    }
 
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Tickify: Application shutting down.");
    }
}
