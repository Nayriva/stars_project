package cz.muni.fi.pv168.db_backend;

import cz.muni.fi.pv168.db_backend.backend.*;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Created by xbucik on 29.3.17.
 */
public class Main {

    public static DataSource createMemoryDB() throws IOException {
        Properties myconf = new Properties();
        myconf.load(Main.class.getResourceAsStream("db.properties"));

        BasicDataSource ds = new BasicDataSource();
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
