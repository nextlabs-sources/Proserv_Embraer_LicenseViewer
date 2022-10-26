package main.java.sync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import main.java.common.PropertyLoader;
import main.java.database.DestinationDBHelper;
import main.java.sync.object.License;
import main.java.sync.object.NDAUser;

public class DestinationDBHandler {

	protected final Logger logger = LogManager.getLogger(getClass());
	private List<License> licenses;
	private List<NDAUser> nDAUsers;
	private Map<String, Integer> hMapEntities = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, String> hMapNationality = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	private HashMap<String, Integer> hMapLicenses = new HashMap<String, Integer>();
	private static final int OP_LICENSE_INSERT = 1;
	private static final int OP_ENTITY_INSERT = 2;
	private static final int OP_APPROVED_PARTY_INSERT = 3;
	private static final int OP_APPROVED_SUBLICENSEE_INSERT = 4;
	private static final int OP_APPROVED_NATIONALITY_INSERT = 5;
	private static final int OP_DENIED_NATIONALITY_INSERT = 6;
	private static final int OP_NDA_INSERT = 7;
	private static final int ERR_UNIQUE = 1;
	private static final int ERR_NULL = 1400;
	private static final int ERROR_PARENT_NOT_FOUND = 2291;
	private List<String> messages;
	private int processLicenseCount;
	private int processNDACount;

	public DestinationDBHandler(List<License> lincenses, List<NDAUser> nDAUsers) {
		this.licenses = lincenses;
		this.nDAUsers = nDAUsers;
		processLicenseCount = 0;
		processNDACount = 0;
	}

	private void recordMessage(int level, String message) {

		if (message == null) {
			message = "Unknown error. Please check the debug log";
		}

		if (level == 0) {
			logger.error(message);
			messages.add("<p class = \"red-text\"><i class=\"fa fa-close\"></i>" + message + "</p>");
		} else if (level == 1) {
			logger.warn(message);
			messages.add("<p class = \"orange-text\"><i class=\"fa fa-warning\"></i>" + message + "</p>");
		} else {
			logger.info(message);
			messages.add("<p class = \"blue-text\"><i class=\"fa fa-check\"></i>" + message + "</p>");
		}
	}

	public void startProcess(List<String> messages) {
		this.messages = messages;

		loadNationalityTable();
		// reload properties file
		PropertyLoader.loadProperties();

		String sCurrentLicense = "";
		int iLicenseIndex = 0;
		int iEntityIndex = 0;
		int iApprovedEntityID = 0;
		int iApprovedNationalityID = 0;
		int iDeniedNationalityID = 0;
		int iNDAID = 0;

		Connection connection = null;

		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			recordMessage(0, "Error getting database connection.");
			logger.debug(e.toString(), e);
			return;
		}

		try {
			cleanupTable(connection);
		} catch (Exception e) {
			recordMessage(0, "Unable to clean up table, will roll back the transaction.");
			logger.debug(e.toString(), e);

			try {
				connection.rollback();
				connection.close();
			} catch (SQLException ex) {
				logger.debug(ex);
			}
			return;
		}

		boolean exceptionOccurred = false;

		for (Object object : licenses) {

			if (object == null) {
				continue;
			}

			License license = (License) object;
			String licenseName = license.getLicenseName();

			if (sCurrentLicense.length() < 1) {
				// First license to be insert
				logger.debug("Processing license " + licenseName);
				sCurrentLicense = licenseName;
				try {
					insertNewLicense(++iLicenseIndex, licenseName, license.getType(), connection);
				} catch (SQLException e) {
					recordMessage(0, "Error inserting license [" + licenseName + "]");
					getSQLExceptionMessage(messages, e, OP_LICENSE_INSERT);
					logger.debug(e.getMessage(), e);
					exceptionOccurred = true;
				}
				hMapLicenses.put(licenseName, iLicenseIndex);
				/* processLicenseCount++; */
			} else if (!sCurrentLicense.equalsIgnoreCase(licenseName)) {
				// New License detected, inserting
				logger.debug("Processing license " + licenseName);
				sCurrentLicense = licenseName;
				try {
					insertNewLicense(++iLicenseIndex, licenseName, license.getType(), connection);
				} catch (SQLException e) {
					recordMessage(0, "Error inserting license [" + licenseName + "]");
					getSQLExceptionMessage(messages, e, OP_LICENSE_INSERT);
					logger.debug(e.getMessage(), e);
					exceptionOccurred = true;
				}
				hMapLicenses.put(licenseName, iLicenseIndex);
				processLicenseCount++;
			}

			if (license.getApprovedPartiesCode() != null && license.getApprovedPartiesCode().length() > 1) {

				if (license.getApprovedPartiesCode().trim().equalsIgnoreCase("#N/A")) {
					logger.info("A company with code as #N/A found, skip adding approved party.");
				} else if (license.getApprovedPartiesCode().trim().equalsIgnoreCase("-")) {
					logger.info("A company with code as - found, skip adding approved party.");
				} else {
					Integer iEntIdx = hMapEntities.get(license.getApprovedPartiesCode());

					if (iEntIdx == null) {
						hMapEntities.put(license.getApprovedPartiesCode(), ++iEntityIndex);
						// insert new entity to DB
						try {
							insertEntity(iEntityIndex, license.getApprovedPartiesCode(), license.getApprovedParties(),
									license.getCountryParties(), connection);
						} catch (SQLException e) {
							recordMessage(0, "Error inserting company [" + license.getApprovedPartiesCode() + "]");
							getSQLExceptionMessage(messages, e, OP_ENTITY_INSERT);
							logger.debug(e.getMessage(), e);
							exceptionOccurred = true;
						}
						iEntIdx = new Integer(iEntityIndex);
					}

					// insert Approved Entity
					try {
						insertApprovedEntity(++iApprovedEntityID, iLicenseIndex, iEntIdx.intValue(), "P", 1,
								connection);
					} catch (SQLException e) {
						recordMessage(0, "Error inserting association between license [" + licenseName
								+ "] and approved party [" + license.getApprovedParties() + "]");
						getSQLExceptionMessage(messages, e, OP_APPROVED_PARTY_INSERT);
						logger.debug(e.getMessage(), e);
						exceptionOccurred = true;
					}

					logger.debug("Finished adding Approved Parties for license " + licenseName);
				}
			}

			if (license.getApprovedSublicenseesCode() != null && license.getApprovedSublicenseesCode().length() > 1) {

				if (license.getApprovedSublicenseesCode().trim().equalsIgnoreCase("#N/A")) {
					logger.info("A company with code as #N/A found, skip adding approved sublicensee.");
				} else if (license.getApprovedSublicenseesCode().trim().equalsIgnoreCase("-")) {
					logger.info("A company with code as - found, skip adding approved sublicensee.");
				} else {

					Integer iEntIdx = hMapEntities.get(license.getApprovedSublicenseesCode());

					if (iEntIdx == null) {
						hMapEntities.put(license.getApprovedSublicenseesCode(), ++iEntityIndex);
						// insert new entity to DB
						try {
							insertEntity(iEntityIndex, license.getApprovedSublicenseesCode(),
									license.getApprovedSublicensees(), license.getCountrySublicensees(), connection);
						} catch (SQLException e) {
							recordMessage(0, "Error inserting company [" + license.getApprovedSublicenseesCode() + "]");
							getSQLExceptionMessage(messages, e, OP_ENTITY_INSERT);
							logger.debug(e.getMessage(), e);
							exceptionOccurred = true;
						}

						iEntIdx = new Integer(iEntityIndex);
					}

					String sNDASubLicensees = license.getnDASublicensees();
					int iNDASublicensees = 0;

					logger.debug("NDA for " + license.getLicenseName() + " and " + license.getApprovedSublicenseesCode()
							+ " is " + sNDASubLicensees);

					if (sNDASubLicensees != null && sNDASubLicensees.equalsIgnoreCase("Yes"))
						iNDASublicensees = 1;

					// insert Approved Entity
					try {
						insertApprovedEntity(++iApprovedEntityID, iLicenseIndex, iEntIdx.intValue(), "S",
								iNDASublicensees, connection);
					} catch (SQLException e) {
						recordMessage(0, "Error inserting association between license [" + licenseName
								+ "] and approved sublicencee [" + license.getApprovedSublicensees() + "]");
						getSQLExceptionMessage(messages, e, OP_APPROVED_SUBLICENSEE_INSERT);
						logger.debug(e.getMessage(), e);
						exceptionOccurred = true;
					}

					logger.debug("Finished adding Approved Sublicensees for license " + licenseName);
				}
			}

			if (license.getApprovedNationalities() != null && license.getApprovedNationalities().length() > 1) {

				if (license.getApprovedNationalities().trim().equals("#N/A")
						|| license.getApprovedNationalities().equals("-")) {
					logger.info("An approved nationalities with invalid code found. Skipped");
				} else {

					// insert Approved Nationality
					try {
						insertApprovedNationality(++iApprovedNationalityID, iLicenseIndex,
								license.getApprovedNationalities(), connection);
					} catch (SQLException e) {
						recordMessage(0, "Error inserting association between license [" + licenseName
								+ "] and approved nationality [" + license.getApprovedNationalities() + "]");
						getSQLExceptionMessage(messages, e, OP_APPROVED_NATIONALITY_INSERT);
						logger.debug(e.getMessage(), e);
						exceptionOccurred = true;
					}
				}

				logger.debug("Finish processing Approved Nationalities for license " + licenseName);
			}

			if (license.getDeniedNationalities() != null && license.getDeniedNationalities().length() > 1) {

				if (license.getDeniedNationalities().trim().equals("#N/A")
						|| license.getDeniedNationalities().equals("-")) {
					logger.info("An denied nationalities with invalid code found. Skipped");
				} else {

					logger.debug("Processing denied nationality [" + license.getDeniedNationalities() + "]");

					// insert Denied Nationality
					try {
						insertDeniedNationality(++iDeniedNationalityID, iLicenseIndex, license.getDeniedNationalities(),
								connection);
					} catch (SQLException e) {
						recordMessage(0, "Error inserting association between license [" + licenseName + "]"
								+ " and denied nationality [" + license.getDeniedNationalities() + "]");
						getSQLExceptionMessage(messages, e, OP_DENIED_NATIONALITY_INSERT);
						logger.debug(e.getMessage(), e);
						exceptionOccurred = true;
					}
				}

				logger.debug("Finished adding Denied Nationalities for license " + licenseName);
			}

		}

		processLicenseCount++;

		for (Object object : nDAUsers) {
			NDAUser user = (NDAUser) object;
			// Insert into NDA table
			try {
				insertNDAUser(++iNDAID, user.getEmployeeName(), user.getLicenseName(), connection);
				logger.debug("Finished processing NDA between [" + user.getEmployeeName() + "] and license ["
						+ user.getLicenseName() + "]");
				processNDACount++;
			} catch (SQLException e) {
				recordMessage(0, "Error inserting NDA association between license [" + user.getLicenseName()
						+ "] and user [" + user.getEmployeeName() + "]");
				getSQLExceptionMessage(messages, e, OP_NDA_INSERT);
				logger.debug(e.getMessage(), e);
				exceptionOccurred = true;
			}
		}

		logger.debug("Finished processing NDA sheet");

		if (exceptionOccurred) {
			recordMessage(0, "Error occurred, will roll back the transaction");
			try {
				connection.rollback();
				connection.close();
			} catch (SQLException e) {
				recordMessage(0, "Unable to rollback the transaction. Please contact administrator");
				logger.debug(e);
			}
		} else {
			// Update the last sync time
			try {
				updateLastSyncTime(connection);

				connection.commit();
				connection.close();
			} catch (Exception e) {
				recordMessage(0, "Unable to commit the insert. Please contact administrator");
				logger.debug(e.getMessage(), e);
				try {
					connection.rollback();
					connection.close();
				} catch (SQLException ex) {
					logger.debug(ex);
				}
			}
		}

	}

	private int insertNewLicense(int iID, String sName, String sType, Connection connection) throws SQLException {

		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("INSERT INTO Licenses (ID, Name, Type) VALUES (?,?,?)");
			statement.setInt(1, iID);
			statement.setString(2, sName);
			statement.setString(3, sType);
			int iRowInserted = statement.executeUpdate();

			if (iRowInserted > 0) {
				// logger.debug("Record inserted for license " + sName);
			}

		} finally {
			try {
				if (statement != null) {
					statement.close();
				}

			} catch (SQLException ex) {
				logger.debug("Error : " + ex.getMessage(), ex);
			}
		}
		return -1;
	}

	private int insertEntity(int iID, String sCode, String sName, String sCountry, Connection connection)
			throws SQLException {

		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("INSERT INTO Entities (ID, Code, Name, Country) VALUES (?,?,?,?)");
			statement.setInt(1, iID);
			statement.setString(2, sCode);
			statement.setString(3, sName);
			statement.setString(4, sCountry);
			int iRowInserted = statement.executeUpdate();

			if (iRowInserted > 0) {
				// logger.debug("Record inserted for entity " + sName);
			}

		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException ex) {
				logger.debug("Error : " + ex.getMessage(), ex);
			}
		}
		return -1;

	}

	private int insertApprovedEntity(int iID, int iLicenseID, int iEntityID, String sType, Integer iNDA,
			Connection connection) throws SQLException {

		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(
					"INSERT INTO APPROVEDENTITIES (ID, LicenseID, EntityID, Type, NDA) VALUES (?,?,?,?,?)");
			statement.setInt(1, iID);
			statement.setInt(2, iLicenseID);
			statement.setInt(3, iEntityID);
			statement.setString(4, sType);
			statement.setInt(5, iNDA);

			int iRowInserted = statement.executeUpdate();

			if (iRowInserted > 0) {
				// logger.debug("Record inserted for entity " + iEntityID);
			}

		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException ex) {
				logger.debug("Error : " + ex.getMessage(), ex);
			}
		}
		return -1;

	}

	private int insertApprovedNationality(int iID, int iLicenseID, String sCountryName, Connection connection)
			throws SQLException {

		if (hMapNationality.get(sCountryName) == null) {
			recordMessage(1, "Undefined country name " + sCountryName + " , skip inserting approved nationality");
			return -1;
		}

		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(
					"INSERT INTO APPROVEDNATIONALITIES (ID, LicenseID, NationalityID) VALUES (?,?,?)");
			statement.setInt(1, iID);
			statement.setInt(2, iLicenseID);
			statement.setString(3, hMapNationality.get(sCountryName));

			int iRowInserted = statement.executeUpdate();

			if (iRowInserted > 0) {
				// logger.debug("Record inserted for license " + iLicenseID + "
				// with approved nationality " + sCountryName);
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException ex) {
				logger.debug("Error : " + ex.getMessage(), ex);
			}
		}
		return -1;

	}

	/**
	 * Load nationality to Map table in memory
	 */
	private void loadNationalityTable() {

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			statement = connection.prepareStatement("SELECT ID, CODE FROM NATIONALITIES");
			resultSet = statement.executeQuery();

			while (resultSet.next()) {
				hMapNationality.put(resultSet.getString("CODE"), resultSet.getString("ID"));
			}

		} catch (Exception ex) {
			logger.debug("Error : " + ex.getMessage(), ex);
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
				logger.debug("Error : " + ex.getMessage(), ex);
			}
		}

	}

	private int insertDeniedNationality(int iID, int iLicenseID, String sCountryName, Connection connection)
			throws SQLException {

		if (hMapNationality.get(sCountryName) == null) {
			recordMessage(1, "Undefined country name " + sCountryName + " , skip inserting denied nationality");
			return -1;
		}

		PreparedStatement statement = null;
		try {
			statement = connection
					.prepareStatement("INSERT INTO DENIEDNATIONALITIES (ID, LicenseID, NationalityID) VALUES (?,?,?)");
			statement.setInt(1, iID);
			statement.setInt(2, iLicenseID);
			statement.setString(3, hMapNationality.get(sCountryName));

			int iRowInserted = statement.executeUpdate();

			if (iRowInserted > 0) {
				// logger.debug("Record inserted for license " + iLicenseID + "
				// with denied nationality " + sCountryName);
			}

		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException ex) {
				logger.debug("Error : " + ex.getMessage(), ex);
			}
		}
		return -1;
	}

	private int insertNDAUser(int iID, String sUserName, String sLicenseName, Connection connection)
			throws SQLException {

		if (hMapLicenses.get(sLicenseName) == null) {
			recordMessage(1, "Undefined license name " + sLicenseName + " , skip inserting NDA User");
			return -1;
		}
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("INSERT INTO NDA (ID, UserID, LicenseID) VALUES (?,?,?)");
			statement.setInt(1, iID);
			statement.setString(2, sUserName);
			statement.setInt(3, hMapLicenses.get(sLicenseName));

			int iRowInserted = statement.executeUpdate();

			if (iRowInserted > 0) {
				// logger.info("Record inserted for user " + sUserName + " with
				// license " + sLicenseName);
			}

		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException ex) {
				logger.debug("Error : " + ex.getMessage(), ex);
			}
		}
		return -1;
	}

	private void getSQLExceptionMessage(List<String> messages, SQLException e, int operation) {
		String msg;

		switch (e.getErrorCode()) {
		case ERR_UNIQUE:
			msg = "The record exists. ";
			switch (operation) {
			case OP_LICENSE_INSERT:
				msg += "License name must be unique.";
				break;
			case OP_APPROVED_PARTY_INSERT:
				msg += "Approved Parties list of a license should contain unique values only.";
				break;
			case OP_APPROVED_SUBLICENSEE_INSERT:
				msg += "Approved Sublicensees list of a license should contain unique values only.";
				break;
			case OP_APPROVED_NATIONALITY_INSERT:
				msg += "Approved Nationalities list of a license should contain unique values only.";
				break;
			case OP_DENIED_NATIONALITY_INSERT:
				msg += "Denied Nationalities list of a license should contain unique values only.";
				break;
			case OP_NDA_INSERT:
				msg += "[User ID - License] pair should be unique.";
				break;
			default:
				break;
			}

			break;
		case ERR_NULL:
			msg = "One or more required values are empty. ";
			switch (operation) {
			case OP_LICENSE_INSERT:
				msg += "License Name and License Type cannot be empty.";
				break;
			case OP_ENTITY_INSERT:
				msg += "Company Name and its corresponding Country cannot be empty.";
				break;
			case OP_NDA_INSERT:
				msg += "UserID and License cannot be empty.";
				break;
			default:
				break;
			}
			break;
		case ERROR_PARENT_NOT_FOUND:
			switch (operation) {
			case OP_APPROVED_PARTY_INSERT:
				msg = "The license or the company was not successfully inserted, hence the association cannot be established.";
				break;
			case OP_APPROVED_SUBLICENSEE_INSERT:
				msg = "The license or the company was not successfully inserted, hence the association cannot be established.";
				break;
			case OP_APPROVED_NATIONALITY_INSERT:
				msg = "The license was not successfully inserted or the nationality does not exist in the database, hence the association cannot be established.";
				break;
			case OP_DENIED_NATIONALITY_INSERT:
				msg = "The license was not successfully inserted or the nationality does not exist in the database, hence the association cannot be established.";
				break;
			case OP_NDA_INSERT:
				msg = "The license does not exist in the database, hence the association cannot be established.";
				break;
			default:
				msg = "One of both values in the association are missing in the database.";
				break;
			}
			break;
		default:
			msg = e.getMessage();
		}

		recordMessage(0, msg);

	}

	private int updateLastSyncTime(Connection connection) throws SQLException {

		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("UPDATE STATUS SET LASTSYNCTIME=?");
			statement.setLong(1, System.currentTimeMillis());

			int iRowInserted = statement.executeUpdate();

			if (iRowInserted > 0) {
				logger.info("Last Sync time updated!");
			}

		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException ex) {
				logger.debug("Error : " + ex.getMessage(), ex);
			}
		}
		return -1;
	}

	private void cleanupTable(Connection connection) throws SQLException {
		Statement statement = null;

		statement = connection.createStatement();
		statement.executeUpdate("delete approvedentities");
		statement.close();

		statement = connection.createStatement();
		statement.executeUpdate("delete entities");
		statement.close();

		statement = connection.createStatement();
		statement.executeUpdate("delete APPROVEDNATIONALITIES");
		statement.close();

		statement = connection.createStatement();
		statement.executeUpdate("delete DENIEDNATIONALITIES");
		statement.close();

		statement = connection.createStatement();
		statement.executeUpdate("delete NDA");
		statement.close();

		statement = connection.createStatement();
		statement.executeUpdate("delete licenses");
		statement.close();
	}

	public int getProcessLicenseCount() {
		return processLicenseCount;
	}

	public void setProcessLicenseCount(int processLicenseCount) {
		this.processLicenseCount = processLicenseCount;
	}

	public int getProcessNDACount() {
		return processNDACount;
	}

	public void setProcessNDACount(int processNDACount) {
		this.processNDACount = processNDACount;
	}

}
