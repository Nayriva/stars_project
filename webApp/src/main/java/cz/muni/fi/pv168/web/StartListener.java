package cz.muni.fi.pv168.web;

import cz.muni.fi.pv168.db_backend.Main;
import cz.muni.fi.pv168.db_backend.backend.*;
import cz.muni.fi.pv168.db_backend.common.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Clock;

/**
 * Created by xbucik on 29.3.17.
 */

@WebListener
public class StartListener implements ServletContextListener {

    private final static Logger log = LoggerFactory.getLogger(StartListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        log.debug("Starting servlet...");
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            System.exit(1);
        }

        AgentManager agentManager = new AgentManagerImpl();
        AssignmentManager assignmentManager = new AssignmentManagerImpl(Clock.systemUTC());
        MissionManager missionManager = new MissionManagerImpl();
        DataSource ds = null;
        try {
            log.debug("Setting up data source...");
            ds = Main.createDB();
            DBUtils.tryCreateTables(ds, Main.class.getResource("backend/createTables.sql"));
        } catch (IOException | SQLException e) {
            System.exit(1);
        }
        agentManager.setDataSource(ds);
        assignmentManager.setDataSource(ds);
        missionManager.setDataSource(ds);
        ServletContext servletContext = servletContextEvent.getServletContext();
        servletContext.setAttribute("agentMgr", agentManager);
        servletContext.setAttribute("missionMgr", missionManager);
        servletContext.setAttribute("assignmentMgr", assignmentManager);
        log.debug("Servlet setup finished...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        log.debug("Servlet stopped...");
    }
}
