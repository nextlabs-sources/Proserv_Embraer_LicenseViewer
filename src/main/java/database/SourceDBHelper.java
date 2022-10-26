package main.java.database;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import main.java.common.PropertyLoader;
import main.java.common.Utils;

public class SourceDBHelper implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(SourceDBHelper.class);
	private static DataSource datasource;
	private static String url;
	private static String driver;
	private static String user;
	private static String password;

	/**
	 * Get database connection via DriverManager
	 * 
	 * @return database connection
	 */
	public static Connection getDatabaseConnectionSimple() {
		Connection connection = null;
		try {
			Class.forName(PropertyLoader.properties.getProperty("S_driver"));
			String url = PropertyLoader.properties.getProperty("S_connection-string");
			String user = PropertyLoader.properties.getProperty("S_sql_user");
			String password = Utils.decrypt(PropertyLoader.properties.getProperty("S_sql_password"));

			connection = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			LOG.error("SQL Exception: " + e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			LOG.error("Class Not Found Exception: " + e.getMessage(),
					e);
		}

		if (connection == null) {
			return null;
		}

		if (LOG.isDebugEnabled()) {
			return new net.sf.log4jdbc.ConnectionSpy(connection);
		} else {
			return connection;
		}

	}

	public static boolean testDB() {
		Connection connection = DestinationDBHelper.getDatabaseConnection();
		if (connection == null) {
			return false;
		} else {
			try {
				connection.close();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			return true;
		}
	}

	private static boolean validatePoolProperties() {
		if (!PropertyLoader.properties.getProperty("S_connection_string").equals(url)
				|| !PropertyLoader.properties.getProperty("S_driver").equals(driver)
				|| !PropertyLoader.properties.getProperty("S_sql_user").equals(user)
				|| !Utils.decrypt(PropertyLoader.properties.getProperty("S_sql_password")).equals(password)) {
			LOG.info("DBHelper validatePoolProperties() Setting up pool properties");
			url = PropertyLoader.properties.getProperty("S_connection_string");
			driver = PropertyLoader.properties.getProperty("S_driver");
			user = PropertyLoader.properties.getProperty("S_sql_user");
			password = Utils.decrypt(PropertyLoader.properties.getProperty("S_sql_password"));
			return false;
		}
		return true;
	}

	public static Connection getDatabaseConnection() {
		Connection connection = null;
		long lCurrentTime = System.nanoTime();

		try {
			if (datasource == null || !validatePoolProperties()) {
				LOG.info("Initialize datasource");

				if (url == null || driver == null || user == null || password == null) {
					url = PropertyLoader.properties.getProperty("S_connection_string");
					driver = PropertyLoader.properties.getProperty("S_driver");
					user = PropertyLoader.properties.getProperty("S_sql_user");
					password = Utils.decrypt(PropertyLoader.properties.getProperty("S_sql_password"));
				}

				PoolProperties p = new PoolProperties();
				p.setUrl(url);
				p.setDriverClassName(driver);
				p.setUsername(user);
				p.setPassword(password);
				p.setMaxIdle(10);
				p.setRemoveAbandonedTimeout(300);
				p.setRemoveAbandoned(true);
				p.setDefaultAutoCommit(true);
				p.setMaxWait(100000);
				p.setMaxActive(30);
				p.setValidationQuery("select 1 from dual");
				p.setTestOnConnect(false);
				p.setTestOnBorrow(true);
				p.setTestWhileIdle(true);
				p.setTimeBetweenEvictionRunsMillis(30000);
				datasource = new org.apache.tomcat.jdbc.pool.DataSource(p);
			}

			connection = datasource.getConnection();
		} catch (Exception e) {
			LOG.error(" Exception: " + e.getMessage(), e);
		}

		if (connection == null) {
			connection = getDatabaseConnectionSimple();
		}

		LOG.debug("completed. Time spent: "
				+ ((System.nanoTime() - lCurrentTime) / 1000000.00) + "ms");
		if (connection == null)
			return null;

		if (LOG.isDebugEnabled()) {
			return new net.sf.log4jdbc.ConnectionSpy(connection);
		} else {
			return connection;
		}
	}
}
