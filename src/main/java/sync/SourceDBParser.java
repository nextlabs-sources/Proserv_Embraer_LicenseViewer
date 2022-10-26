package main.java.sync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import main.java.database.SourceDBHelper;
import main.java.sync.object.License;
import main.java.sync.object.NDAUser;

public class SourceDBParser {

	private static final Log LOG = LogFactory.getLog(SourceDBParser.class);
	private static String licenseTable;
	private static String ndaTable;
	private static String columnLicenseName;
	private static String columnLicenseType;
	private static String columnApprovedPartiesCode;
	private static String columnApprovedPartiesName;
	private static String columnCountryParties;
	private static String columnApprovedSublicenseesCode;
	private static String columnApprovedSublicenseesName;
	private static String columnCountrySublicensees;
	private static String columnNdaSublicensees;
	private static String columnApprovedNationalities;
	private static String columnProvisoNationalities;
	private static String columnNDALicense;
	private static String columnNDAEmployee;

	public static void setUpSourceDBParser(Properties props) {
		licenseTable = props.getProperty("license_table");

		if (licenseTable == null || licenseTable.length() == 0) {
			LOG.error("License table is undefined");
		}

		ndaTable = props.getProperty("nda_table");

		if (ndaTable == null || ndaTable.length() == 0) {
			LOG.error("NDA table is undefined");
		}

		columnLicenseName = props.getProperty("name", "LICENSE");
		columnLicenseType = props.getProperty("type", "LICENSE_TYPE");
		columnApprovedPartiesCode = props.getProperty("approved_parties_code", "APPROVED_PARTIES");
		columnCountryParties = props.getProperty("country_parties", "COUNTRY_PARTIES");
		columnApprovedSublicenseesCode = props.getProperty("approved_sublicensees_code", "APPROVED_SUBLICENSEES");
		columnCountrySublicensees = props.getProperty("country_sublicensees", "COUNTRY_SUBLICENSEES");
		columnNdaSublicensees = props.getProperty("nda_sublicensees", "NDA_SUBLICENSEES");
		columnApprovedNationalities = props.getProperty("approved_nationalities", "APPROVED_NATIONALITIES");
		columnProvisoNationalities = props.getProperty("proviso_nationalities", "PROVISO_NATIONALITIES");
		columnNDALicense = props.getProperty("license_name", "TAA");
		columnNDAEmployee = props.getProperty("employee_id", "NOME");
		columnApprovedPartiesName = props.getProperty("approved_parties_name", "APPROVED_PARTIES_NAME");
		columnApprovedSublicenseesName = props.getProperty("approved_sublicensees_name", "APPROVED_SUBLICENSEES_NAME");

	}

	public static List<License> getAllLicenses() {
		List<License> results = new ArrayList<License>();

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = SourceDBHelper.getDatabaseConnection();
			String queryString = "SELECT * FROM " + licenseTable + " ORDER BY " + columnLicenseName;

			preparedStatement = connection.prepareStatement(queryString);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				String name = resultSet.getString(columnLicenseName);
				String type = resultSet.getString(columnLicenseType);
				String approvedPartyCode = resultSet.getString(columnApprovedPartiesCode);
				String approvedPartyName;
				try {
					approvedPartyName = resultSet.getString(columnApprovedPartiesName);
				} catch (SQLException s) {
					LOG.error("Column " + columnApprovedPartiesName + " is invalid");
					approvedPartyName = "";
				}
				String countryParty = resultSet.getString(columnCountryParties);
				String approvedSublicenseeCode = resultSet.getString(columnApprovedSublicenseesCode);

				String approvedSublicenseeName;
				try {
					approvedSublicenseeName = resultSet.getString(columnApprovedSublicenseesName);
				} catch (SQLException s) {
					LOG.error("Column " + columnApprovedSublicenseesName + " is invalid");
					approvedSublicenseeName = "";
				}
				String countrySublicensee = resultSet.getString(columnCountrySublicensees);
				String ndaSublicensee = resultSet.getString(columnNdaSublicensees);
				String approvedNationality = resultSet.getString(columnApprovedNationalities);
				String provisoNationality = resultSet.getString(columnProvisoNationalities);

				License license = new License(name, type, approvedPartyName, approvedPartyCode, countryParty,
						approvedSublicenseeName, approvedSublicenseeCode, ndaSublicensee, countrySublicensee,
						approvedNationality, provisoNationality);

				results.add(license);
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
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
				LOG.error(ex.getMessage(), ex);
			}
		}
		return results;

	}

	public static List<NDAUser> getAllNDAs() {
		List<NDAUser> results = new ArrayList<NDAUser>();

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = SourceDBHelper.getDatabaseConnection();
			String queryString = "SELECT * FROM " + ndaTable;

			preparedStatement = connection.prepareStatement(queryString);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				String license = resultSet.getString(columnNDALicense);
				String employee = resultSet.getString(columnNDAEmployee);

				NDAUser nda = new NDAUser(employee, license);

				results.add(nda);
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
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
				LOG.error(ex.getMessage(), ex);
			}
		}
		return results;

	}
}
