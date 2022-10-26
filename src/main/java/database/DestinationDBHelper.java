package main.java.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import main.java.common.PropertyLoader;
import main.java.common.Utils;

public class DestinationDBHelper implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(DestinationDBHelper.class);
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
			Class.forName(PropertyLoader.properties.getProperty("D_driver"));
			String url = PropertyLoader.properties.getProperty("D_connection_string");
			String user = PropertyLoader.properties.getProperty("D_sql_user");
			String password = Utils.decrypt(PropertyLoader.properties.getProperty("D_sql_password"));

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
		if (!PropertyLoader.properties.getProperty("D_connection_string").equals(url)
				|| !PropertyLoader.properties.getProperty("D_driver").equals(driver)
				|| !PropertyLoader.properties.getProperty("D_sql_user").equals(user)
				|| !Utils.decrypt(PropertyLoader.properties.getProperty("D_sql_password")).equals(password)) {
			LOG.info("DBHelper validatePoolProperties() Setting up pool properties");
			url = PropertyLoader.properties.getProperty("D_connection_string");
			driver = PropertyLoader.properties.getProperty("D_driver");
			user = PropertyLoader.properties.getProperty("D_sql_user");
			password = Utils.decrypt(PropertyLoader.properties.getProperty("D_sql_password"));
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
					url = PropertyLoader.properties.getProperty("D_connection_string");
					driver = PropertyLoader.properties.getProperty("D_driver");
					user = PropertyLoader.properties.getProperty("D_sql_user");
					password = Utils.decrypt(PropertyLoader.properties.getProperty("D_sql_password"));
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
			LOG.error("Exception: " + e.getMessage(), e);
		}

		if (connection == null) {
			connection = getDatabaseConnectionSimple();
		}

		LOG.debug("Get database connection completed. Time spent: "
				+ ((System.nanoTime() - lCurrentTime) / 1000000.00) + "ms");
		if (connection == null)
			return null;

		if (LOG.isDebugEnabled()) {
			return new net.sf.log4jdbc.ConnectionSpy(connection);
		} else {
			return connection;
		}
	}

	public static void initializeDatabase() {
		Connection connection = null;
		try {
			InputStream is = DestinationDBHelper.class.getResourceAsStream("oracle.sql");
			connection = getDatabaseConnection();
			connection.setAutoCommit(false);

			if (is != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));

				String line;
				StringBuffer command = new StringBuffer();

				while ((line = reader.readLine()) != null) {
					String trimmedLine = line.trim();

					if (trimmedLine.startsWith("//") || trimmedLine.startsWith("/*") || trimmedLine.startsWith("\\*")) {
						// ignore comment
					} else {
						command.append(trimmedLine);
						command.append(" ");

						if (trimmedLine.endsWith(";")) {

							String commandString = command.toString().replaceAll(";", "");

							Statement statement = connection.createStatement();

							statement.execute(commandString);
							statement.close();

							command = new StringBuffer();
						}
					}
				}

				connection.commit();
			} else {
				LOG.error("Cannot parse the SQL script");
			}

		} catch (IOException e) {
			LOG.error("Cannot read the SQL script", e);
		} catch (SQLException e) {
			LOG.error("Error encountered when executing SQL script", e);
			try {
				connection.rollback();
			} catch (SQLException ex) {

			}
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
				}
			}
		}

		LOG.info("initializeDatabase() completed");
	}

	public static void initializeNationalities() {
		Connection connection = null;

		try {
			InputStream is = DestinationDBHelper.class.getResourceAsStream("countrycode.sql");
			connection = getDatabaseConnection();

			Statement statement = connection.createStatement();

			int count = 0;
			ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM Nationalities");
			if (result.next()) {
				count = result.getInt(1);
			}

			statement.close();
			result.close();

			if (count > 0) {
				LOG.info("Nationalities data exists");
				return;
			}

			connection.setAutoCommit(false);

			if (is != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));

				String line;
				StringBuffer command = new StringBuffer();

				while ((line = reader.readLine()) != null) {
					String trimmedLine = line.trim();

					if (trimmedLine.startsWith("//") || trimmedLine.startsWith("/*") || trimmedLine.startsWith("\\*")) {
						// ignore comment
					} else {
						command.append(trimmedLine);
						command.append(" ");

						if (trimmedLine.endsWith(";")) {
							String commandString = command.toString().replaceAll(";", "");

							PreparedStatement pStatement = connection.prepareStatement(commandString);
							pStatement.execute();
							command = new StringBuffer();
						}
					}
				}
				connection.commit();
			} else {
				LOG.error("Cannot parse the SQL script");
			}

		} catch (IOException e) {
			LOG.error("Cannot read the SQL script", e);
		} catch (SQLException e) {
			LOG.error("Error encountered when executing SQL script", e);
			try {
				connection.rollback();
			} catch (SQLException ex) {

			}
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
				}
			}
		}

		LOG.info("completed");
	}

	public static boolean checkTables() {
		Connection connection = null;
		try {
			String[] temp = { "licenses", "entities", "nationalities", "approvedentities", "approvednationalities",
					"deniednationalities", "nda", "status" };
			List<String> tableList = new ArrayList<String>(Arrays.asList(temp));
			LOG.info("Checking tables existence...");
			connection = getDatabaseConnection();
			if (connection == null) {
				return false;
			}

			DatabaseMetaData meta = connection.getMetaData();
			ResultSet tables = meta.getTables(null, null, "%", new String[] { "TABLE" });
			while (tables.next()) {
				String name = tables.getString("TABLE_NAME");
				if (tableList.contains(name.toLowerCase())) {
					tableList.remove(name.toLowerCase());
				}
				if (tableList.isEmpty()) {
					break;
				}
			}

			if (tableList.isEmpty()) {
				LOG.info("Tables exist");
				return true;
			} else {
				LOG.error("Not all tables exist");
				return false;
			}

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return false;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
				}
			}
		}
	}
}
