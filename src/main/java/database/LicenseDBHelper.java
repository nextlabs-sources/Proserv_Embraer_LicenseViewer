package main.java.database;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import main.java.object.License;

public class LicenseDBHelper implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(LicenseDBHelper.class);

	/**
	 * Get all licenses
	 * 
	 * @return all licenses
	 */
	public static List<License> getAllLicense() {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<License> resultList = new ArrayList<License>();
		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			String queryString = "SELECT * FROM Licenses";

			preparedStatement = connection.prepareStatement(queryString);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				long id = resultSet.getLong("ID");
				String name = resultSet.getString("Name");
				String type = resultSet.getString("Type");
				License license = new License(id, name, type);
				resultList.add(license);
			}
		} catch (Exception ex) {
			LOG.error("LicenseDBHelper getAllLicense(): " + ex.getMessage(), ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				LOG.error("LicenseDBHelper getAllLicense(): " + ex.getMessage(), ex);
			}
		}
		return resultList;
	}

	/**
	 * Get licenses by lazy loading
	 * 
	 * @param start
	 * @param size
	 * @param sortField
	 * @param sortOrder
	 * @param filters
	 * @return licenses based on filter values
	 */
	public static List<License> getLicensesLazy(int start, int size, String sortField, String sortOrder,
			Map<String, Object> filters) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<License> resultList = new ArrayList<License>();
		Map<String, Integer> parameterIndex = new HashMap<String, Integer>();

		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			StringBuilder queryString = new StringBuilder(
					"SELECT outer.* FROM (SELECT ROWNUM rn, inner.* FROM (SELECT * FROM Licenses");
			StringBuilder whereClause = new StringBuilder();

			// build query string
			int countFilters = 1;
			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					String key = Character.toUpperCase(filter.getKey().charAt(0)) + filter.getKey().substring(1);

					whereClause.append("UPPER(" + key + ") LIKE UPPER(?) AND ");

					// for setting value of the filter later
					parameterIndex.put(key, countFilters);
					countFilters++;
				}

			}

			if (whereClause.toString().trim().length() != 0) {
				whereClause = new StringBuilder(whereClause.substring(0, whereClause.length() - 5));
				queryString.append(" WHERE " + whereClause);
			}

			// concate sort
			queryString.append(" ORDER BY " + Character.toUpperCase(sortField.charAt(0)) + sortField.substring(1) + "");
			if (!sortOrder.equals("")) {
				queryString.append(" " + sortOrder);
			}

			// lazy loading
			queryString
					.append(" ) inner) outer WHERE outer.rn >= " + (start + 1) + " AND outer.rn <= " + (start + size));

			preparedStatement = connection.prepareStatement(queryString.toString());

			LOG.debug(queryString.toString());

			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					// set value of the filter
					String key = Character.toUpperCase(filter.getKey().charAt(0)) + filter.getKey().substring(1);
					String value = filter.getValue().toString() + "%";
					preparedStatement.setString(parameterIndex.get(key), value);
				}

			}

			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				long id = resultSet.getLong("ID");
				String name = resultSet.getString("Name");
				String type = resultSet.getString("Type");
				License license = new License(id, name, type);
				resultList.add(license);
			}
		} catch (Exception ex) {
			LOG.error("LicenseDBHelper getLicenseLazy(): " + ex.getMessage(), ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if (connection != null) {
					connection.close();
				}

			} catch (SQLException ex) {
				LOG.error("LicenseDBHelper getLicenseLazy(): " + ex.getMessage(), ex);
			}
		}
		return resultList;
	}

	/**
	 * Count licenses
	 * 
	 * @param filters
	 * @return total number of licenses
	 */
	public static int countLicenses(Map<String, Object> filters) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		int resultCount = 0;
		Map<String, Integer> parameterIndex = new HashMap<String, Integer>();
		if (filters == null) {
			filters = new HashMap<String, Object>();
		}
		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM Licenses");
			StringBuilder whereClause = new StringBuilder();

			int countFilters = 1;
			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					String key = Character.toUpperCase(filter.getKey().charAt(0)) + filter.getKey().substring(1);
					whereClause.append("UPPER(" + key + ") LIKE UPPER(?) AND ");
					parameterIndex.put(key, countFilters);
					countFilters++;
				}

			}

			if (whereClause.toString().trim().length() != 0) {
				whereClause = new StringBuilder(whereClause.substring(0, whereClause.length() - 5));
				queryString.append(" WHERE " + whereClause);
			}

			preparedStatement = connection.prepareStatement(queryString.toString());

			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					String key = Character.toUpperCase(filter.getKey().charAt(0)) + filter.getKey().substring(1);
					String value = filter.getValue().toString() + "%";
					preparedStatement.setString(parameterIndex.get(key), value);
				}

			}

			// LOG.info("Lazy query count: " + preparedStatement.toString());

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				resultCount = resultSet.getInt(1);
			}
		} catch (Exception ex) {
			LOG.error("LicenseDBHelper countLicenses(): " + ex.getMessage(), ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if (connection != null) {
					connection.close();
				}

			} catch (SQLException ex) {
				LOG.error("LicenseDBHelper countLicenses(): " + ex.getMessage(), ex);
			}
		}
		return resultCount;
	}

	/**
	 * Get license by name
	 * 
	 * @param name
	 * @return the license or null if not found
	 */
	public static License getLicense(String name) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		License license = null;

		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			statement = connection.prepareStatement("SELECT * FROM Licenses WHERE Name = ?");
			statement.setString(1, name);
			resultSet = statement.executeQuery();

			if (resultSet.next()) {
				long id = resultSet.getLong("ID");
				String type = resultSet.getString("Type");
				license = new License(id, name, type);
			}
		} catch (Exception ex) {
			LOG.error("LicenseDBHelper getLicense(): " + ex.getMessage(), ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}

			} catch (SQLException ex) {
				LOG.error("LicenseDBHelper getLicense(): " + ex.getMessage(), ex);
			}
		}
		return license;
	}
}
