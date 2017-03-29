package cz.muni.fi.pv168.db_backend.backend;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.util.Properties;

/**
 * Main method of STARS.
 *
 * Created by nayriva on 21.3.2017.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Properties myconf = new Properties();
        myconf.load(Main.class.getResourceAsStream("/db.properties"));

        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(myconf.getProperty("jdbc.url"));
        ds.setUsername(myconf.getProperty("jdbc.user"));
        ds.setPassword(myconf.getProperty("jdbc.password"));
    }
}
