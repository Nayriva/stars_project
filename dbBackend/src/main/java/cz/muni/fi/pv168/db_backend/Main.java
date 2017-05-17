package cz.muni.fi.pv168.db_backend;

import cz.muni.fi.pv168.db_backend.backend.*;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Created by xbucik on 29.3.17.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger("Main.class");

    public static DataSource createDB() throws IOException {
        Properties myconf = new Properties();
        myconf.load(Main.class.getResourceAsStream("db.properties"));

        BasicDataSource ds = new BasicDataSource();
        String driver = myconf.getProperty("jdbc.driver");
        if (driver != null) {
            try {
                Class.forName(driver) ;
            } catch (ClassNotFoundException e) {
                logger.error("Cannot register driver", e);
                System.exit(1);
            }
        }
        ds.setUrl(myconf.getProperty("jdbc.url"));
        ds.setUsername(myconf.getProperty("jdbc.user"));
        ds.setPassword(myconf.getProperty("jdbc.password"));

        return ds;
    }

    public static void main(String[] args) throws IOException {

        Properties myconf = new Properties();
        myconf.load(Main.class.getResourceAsStream("db.properties"));

        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(myconf.getProperty("jdbc.url"));
        ds.setUsername(myconf.getProperty("jdbc.user"));
        ds.setPassword(myconf.getProperty("jdbc.password"));

        AgentManager agentManager = new AgentManagerImpl();
        agentManager.setDataSource(ds);

        List<Agent> agents = agentManager.findAllAgents();
        agents.forEach(System.out::println);
    }
}
