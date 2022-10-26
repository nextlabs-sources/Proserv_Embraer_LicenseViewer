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

import main.java.object.NDA;

public class NDADBHelper implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(NDADBHelper.class);

	/**
	 * Get NDA by lazy loading
	 * 
	 * @param start
	 * @param size
	 * @param sortField
	 * @param sortOrder
	 * @param filters
	 * @return NDA based on filter values
	 */
	public static List<NDA> getNDALazy(int start, int size, String sortField, String sortOrder,
			Map<String, Object> filters) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<NDA> resultList = new ArrayList<NDA>();
		Map<String, Integer> parameterIndex = new HashMap<String, Integer>();

		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			StringBuilder queryString = new StringBuilder(
					"SELECT outer.* FROM (SELECT ROWNUM rn, inner.* FROM (SELECT n.ID as ID, n.userID as UserID, n.licenseID as LicenseID, l.Name as Name FROM NDA n, Licenses l");
			queryString.append(" WHERE n.licenseID = l.ID");
			StringBuilder whereClause = new StringBuilder();

			// build query string
			int countFilters = 1;
			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					String key = Character.toUpperCase(filter.getKey().charAt(0)) + filter.getKey().substring(1);

					if (key.equalsIgnoreCase("licenseName")) {
						whereClause.append("UPPER(l.Name) LIKE UPPER(?) AND ");
					} else {
						whereClause.append("UPPER(n." + key + ") LIKE UPPER(?) AND ");
					}

					// for setting value of the filter later
					parameterIndex.put(key, countFilters);
					countFilters++;
				}
			}

			if (whereClause.toString().trim().length() != 0) {
				whereClause = new StringBuilder(whereClause.substring(0, whereClause.length() - 5));
				queryString.append(" AND " + whereClause);
				
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
				String userID = resultSet.getString("UserID");
				long licenseID = resultSet.getLong("LicenseID");
				String licenseName = resultSet.getString("Name");
				NDA nDA = new NDA(id, userID, licenseID, licenseName);
				resultList.add(nDA);
			}
		} catch (Exception ex) {
			LOG.error("getNDAListLazy(): " + ex.getMessage(), ex);
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
				LOG.error("getNDAListLazy(): " + ex.getMessage(), ex);
			}
		}
		return resultList;
	}

	/**
	 * Count NDA
	 * 
	 * @param filters
	 * @return total number of licenses
	 */
	public static int countNDA(Map<String, Object> filters) {
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
			StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM NDA n, Licenses l");
			queryString.append(" WHERE n.licenseID = l.ID");
			StringBuilder whereClause = new StringBuilder();

			int countFilters = 1;
			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					String key = Character.toUpperCase(filter.getKey().charAt(0)) + filter.getKey().substring(1);

					if (key.equalsIgnoreCase("licenseName")) {
						whereClause.append("UPPER(l.Name) LIKE UPPER(?) AND ");
					} else {
						whereClause.append("UPPER(n." + key + ") LIKE UPPER(?) AND ");
					}

					parameterIndex.put(key, countFilters);
					countFilters++;
				}

			}

			if (whereClause.toString().trim().length() != 0) {
				whereClause = new StringBuilder(whereClause.substring(0, whereClause.length() - 5));
				queryString.append(" AND " + whereClause);
			}

			preparedStatement = connection.prepareStatement(queryString.toString());

			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					String key = Character.toUpperCase(filter.getKey().charAt(0)) + filter.getKey().substring(1);
					String value = filter.getValue().toString() + "%";
					preparedStatement.setString(parameterIndex.get(key), value);
				}

			}

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				resultCount = resultSet.getInt(1);
			}
		} catch (Exception ex) {
			LOG.error("countNDA(): " + ex.getMessage(), ex);
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
				LOG.error("countNDA(): " + ex.getMessage(), ex);
			}
		}
		return resultCount;
	}
}
