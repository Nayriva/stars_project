package db_backend.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Database tools. (Inspired by Petr Adámek)
 *
 * @author nayriva
 */
public class DBUtils {

    /**
     * Extract key from given ResultSet.
     *
     * @param key resultSet with key
     * @return key from given result set
     * @throws SQLException when operation fails
     */
    public static Long getId(ResultSet key) throws SQLException {
        if (key.getMetaData().getColumnCount() != 1) {
            throw new IllegalArgumentException("Given ResultSet contains more columns");
        }
        if (key.next()) {
            Long result = key.getLong(1);
            if (key.next()) {
                throw new IllegalArgumentException("Given ResultSet contains more rows");
            }
            return result;
        } else {
            throw new IllegalArgumentException("Given ResultSet contain no rows");
        }
    }

    /**
     * Reads SQL statements from file. SQL commands in file must be separated by
     * a semicolon.
     *
     * @param url url of the file
     * @return array of command  strings
     */
    private static String[] readSqlStatements(URL url) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
            String line;
            String result="";
            while ((line = br.readLine()) != null) {
                result += line;
            }
            return result.split(";");
        } catch (IOException ex) {
            throw new RuntimeException("Cannot read " + url, ex);
        }
    }

    /**
     * Try to execute script for creating tables. If tables already exist,
     * appropriate exception is catched and ignored.
     *
     * @param ds dataSource
     * @param scriptUrl url of script for creating tables
     * @throws SQLException when operation fails
     */
    public static void tryCreateTables(DataSource ds, URL scriptUrl) throws SQLException {
        try {
            executeSqlScript(ds, scriptUrl);
        } catch (SQLException ex) {
            if (! "X0Y32".equals(ex.getSQLState())) {
                // This code represents "Table/View/... already exists"
                // This code is Derby specific!
                throw ex;
            }
        }
    }

    /**
     * Executes SQL script.
     *
     * @param ds datasource
     * @param scriptUrl url of sql script to be executed
     * @throws SQLException when operation fails
     */
    public static void executeSqlScript(DataSource ds, URL scriptUrl) throws SQLException {
        try (Connection conn = ds.getConnection()) {
            for (String sqlStatement : readSqlStatements(scriptUrl)) {
                if (!sqlStatement.trim().isEmpty()) {
                    conn.prepareStatement(sqlStatement).executeUpdate();
                }
            }
        }
    }

    /**
     * Check if updates count is one. Otherwise appropriate exception is thrown.
     *
     * @param count updates count.
     * @param entity updated entity (to be included into error message)
     * @param insert flag if performed operation was insert
     * @throws IllegalEntityException when updates count is zero, so updated entity does not exist
     * @throws ServiceFailureException when updates count is unexpected number
     */
    public static void checkUpdatesCount(int count, Object entity, boolean insert)
            throws IllegalEntityException, ServiceFailureException
    {
        if (!insert && count == 0) {
            throw new IllegalEntityException("Entity " + entity + " does not exist in the db");
        }

        if (count != 1) {
            throw new ServiceFailureException("Internal integrity error: Unexpected rows count in database affected: " + count);
        }
    }
}