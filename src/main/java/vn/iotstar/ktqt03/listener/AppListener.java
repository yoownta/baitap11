package vn.iotstar.ktqt03.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import vn.iotstar.ktqt03.util.Jpa;
import vn.iotstar.ktqt03.dao.DatabaseInitializer;

@WebListener
public class AppListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabaseInitializer.initialize();
        Jpa.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Jpa.close();
    }
}
