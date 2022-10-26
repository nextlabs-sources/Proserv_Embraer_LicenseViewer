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

import main.java.object.Nationality;

public class NationalityDBHelper implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(NationalityDBHelper.class);

	/**
	 * Get all nationalities
	 * 
	 * @return all nationalities
	 */
	public static List<Nationality> getAllNationalities() {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<Nationality> resultList = new ArrayList<Nationality>();
		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			String queryString = "SELECT * FROM Nationalities";

			preparedStatement = connection.prepareStatement(queryString);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				long id = resultSet.getLong("ID");
				String name = resultSet.getString("Name");
				String code = resultSet.getString("Code");
				Nationality nationality = new Nationality(id, name, code);
				resultList.add(nationality);
			}
		} catch (Exception ex) {
			LOG.error("getAllNationality(): " + ex.getMessage(), ex);
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
				LOG.error("getAllNationality(): " + ex.getMessage(), ex);
			}
		}
		return resultList;
	}

	/**
	 * Get nationalities by lazy loading
	 * 
	 * @param start
	 * @param size
	 * @param sortField
	 * @param sortOrder
	 * @param filters
	 * @return nationalities based on filter values
	 */
	public static List<Nationality> getNationalitiesLazy(int start, int size, String sortField, String sortOrder,
			Map<String, Object> filters) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<Nationality> resultList = new ArrayList<Nationality>();
		Map<String, Integer> parameterIndex = new HashMap<String, Integer>();

		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			StringBuilder queryString = new StringBuilder(
					"SELECT outer.* FROM (SELECT ROWNUM rn, inner.* FROM (SELECT * FROM Nationalities");
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

			LOG.debug(queryString.toString());

			preparedStatement = connection.prepareStatement(queryString.toString());

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
				String code = resultSet.getString("Code");
				Nationality nationality = new Nationality(id, name, code);
				resultList.add(nationality);
			}
		} catch (Exception ex) {
			LOG.error("getNationalityLazy(): " + ex.getMessage(), ex);
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
				LOG.error("getNationalityLazy(): " + ex.getMessage(), ex);
			}
		}
		return resultList;
	}

	/**
	 * Count nationalities
	 * 
	 * @param filters
	 * @return total number of nationalities
	 */
	public static int countNationalities(Map<String, Object> filters) {
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
			StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM Nationalities");
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
			LOG.error("countNationalities(): " + ex.getMessage(), ex);
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
				LOG.error("countNationalities(): " + ex.getMessage(), ex);
			}
		}
		return resultCount;
	}

	/**
	 * Get nationality by name
	 * 
	 * @param name
	 * @return the nationality or null if not found
	 */
	public static Nationality getNationality(String name) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Nationality nationality = null;

		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			statement = connection.prepareStatement("SELECT * FROM Nationalities WHERE Name = ?");
			statement.setString(1, name);
			resultSet = statement.executeQuery();

			if (resultSet.next()) {
				long id = resultSet.getLong("ID");
				String code = resultSet.getString("Code");
				nationality = new Nationality(id, name, code);
			}
		} catch (Exception ex) {
			LOG.error("getNationality(): " + ex.getMessage(), ex);
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
				LOG.error("getNationality(): " + ex.getMessage(), ex);
			}
		}
		return nationality;
	}
	
	
	/**
	 * Get nationality by code
	 * 
	 * @param code
	 * @return the nationality or null if not found
	 */
	public static Nationality getNationalityByCode(String code) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Nationality nationality = null;

		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			statement = connection.prepareStatement("SELECT * FROM Nationalities WHERE Code = ?");
			statement.setString(1, code);
			resultSet = statement.executeQuery();

			if (resultSet.next()) {
				long id = resultSet.getLong("ID");
				String name = resultSet.getString("Name");
				nationality = new Nationality(id, name, code);
			}
		} catch (Exception ex) {
			LOG.error("getNationalityByCode(): " + ex.getMessage(), ex);
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
				LOG.error("getNationalityByCode(): " + ex.getMessage(), ex);
			}
		}
		return nationality;
	}

	/**
	 * Create new nationality
	 * 
	 * @param name
	 * @param code
	 * @return
	 */
	public static synchronized boolean createNationality(Nationality nationality) {

		int id = countNationalities(null) + 1;
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			statement = connection.prepareStatement("INSERT INTO Nationalities (ID, Name, Code) VALUES (?, ?, ?)");
			statement.setInt(1, id);
			statement.setString(2, nationality.getName());
			statement.setString(3, nationality.getCode());

			// LOG.info(queryString);
			statement.execute();
			connection.commit();
		} catch (Exception ex) {
			LOG.error("createNationality(): " + ex.getMessage(), ex);
			return false;
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				LOG.error("createNationality(): " + ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}
}
