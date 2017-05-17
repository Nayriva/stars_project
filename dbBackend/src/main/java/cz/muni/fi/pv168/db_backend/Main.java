package cz.muni.fi.pv168.db_backend;

import cz.muni.fi.pv168.db_backend.backend.*;
import org.apache.commons.dbcp2.BasicDataSource;
<<<<<<< HEAD
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
=======
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
>>>>>>> 155efb123758cce2b2c8e16162f4cb8b74e0a772

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Main class containing test method for running app and createDB method.
 *
 * Created by xbucik on 29.3.17.
 */
public class Main {
<<<<<<< HEAD
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
=======
    private static final Logger logger = LoggerFactory.getLogger("Main.class");
>>>>>>> 155efb123758cce2b2c8e16162f4cb8b74e0a772

    public static DataSource createDB() throws IOException {
        Properties config = new Properties();
        config.load(Main.class.getResourceAsStream("db.properties"));
        BasicDataSource ds = new BasicDataSource();
<<<<<<< HEAD
        String driver = config.getProperty("jdbc.driver");
        try {
            if (driver == null || driver.isEmpty()) {
                logger.error("Driver string not set!!!");
                System.exit(1);
            }
            Class.forName(driver).newInstance();
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            logger.error("Cannot register driver!!! {}", driver);
            System.exit(2);
            e.printStackTrace();
        }
        ds.setUrl(config.getProperty("jdbc.url"));
        ds.setUsername(config.getProperty("jdbc.user"));
        ds.setPassword(config.getProperty("jdbc.password"));
=======
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
>>>>>>> 155efb123758cce2b2c8e16162f4cb8b74e0a772

        return ds;
    }

    public static void main(String[] args) throws IOException {

        Properties config = new Properties();
        config.load(Main.class.getResourceAsStream("db.properties"));

        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(config.getProperty("jdbc.url"));
        ds.setUsername(config.getProperty("jdbc.user"));
        ds.setPassword(config.getProperty("jdbc.password"));

        AgentManager agentManager = new AgentManagerImpl();
        agentManager.setDataSource(ds);

        List<Agent> agents = agentManager.findAllAgents();
        agents.forEach(System.out::println);
    }
}
