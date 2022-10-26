package main.java.database;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import main.java.object.Company;
import main.java.object.License;
import main.java.object.NDA;
import main.java.object.Nationality;

public class RelationshipDBHelper implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(RelationshipDBHelper.class);

	/**
	 * Get license approved nationalities from license name
	 * 
	 * @param name
	 * @return
	 */
	public static List<Nationality> getLicenseApprovedNationalities(String name) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<Nationality> resultList = new ArrayList<Nationality>();
		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			StringBuffer queryString = new StringBuffer(
					"SELECT n.ID as ID, n.Code as Code, n.Name as Name FROM Nationalities n, Licenses e, ApprovedNationalities an ");
			queryString.append("WHERE e.name = ? ");
			queryString.append("AND e.ID  = an.LicenseID ");
			queryString.append("AND an.NationalityID = n.ID");

			preparedStatement = connection.prepareStatement(queryString.toString());
			preparedStatement.setString(1, name);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				long id = resultSet.getLong("ID");
				String nName = resultSet.getString("Name");
				String code = resultSet.getString("Code");
				Nationality nationality = new Nationality(id, nName, code);
				resultList.add(nationality);
			}
		} catch (Exception ex) {
			LOG.error("getLicenseApprovedNationalities(): " + ex.getMessage(), ex);
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
				LOG.error("getLicenseApprovedNationalities(): " + ex.getMessage(), ex);
			}
		}
		return resultList;
	}

	/**
	 * Get license denied nationalities
	 * 
	 * @param name
	 * @return
	 */
	public static List<Nationality> getLicenseDeniedNationalities(String name) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<Nationality> resultList = new ArrayList<Nationality>();
		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			StringBuffer queryString = new StringBuffer(
					"SELECT n.ID as ID, n.Code as Code, n.Name as Name FROM Nationalities n, Licenses e, DeniedNationalities an ");
			queryString.append("WHERE e.name = ? ");
			queryString.append("AND e.ID  = an.LicenseID ");
			queryString.append("AND an.NationalityID = n.ID");

			preparedStatement = connection.prepareStatement(queryString.toString());
			preparedStatement.setString(1, name);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				long id = resultSet.getLong("ID");
				String nName = resultSet.getString("Name");
				String code = resultSet.getString("Code");
				Nationality nationality = new Nationality(id, nName, code);
				resultList.add(nationality);
			}
		} catch (Exception ex) {
			LOG.error("getLicenseDeniedNationalities(): " + ex.getMessage(), ex);
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
				LOG.error("getLicenseDeniedNationalities(): " + ex.getMessage(), ex);
			}
		}
		return resultList;
	}

	/**
	 * Get license approved parties from license name
	 * 
	 * @param name
	 * @return
	 */
	public static List<Company> getLicenseApprovedCompanies(String name, String type) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<Company> resultList = new ArrayList<Company>();
		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			StringBuffer queryString = new StringBuffer(
					"SELECT e.ID as ID, e.Name as Name, e.Code as Code, e.Country as Country, ae.NDA as NDA FROM Entities e, Licenses l, ApprovedEntities ae ");
			queryString.append("WHERE l.name = ? ");
			queryString.append("AND l.ID  = ae.LicenseID ");
			queryString.append("AND ae.EntityID = e.ID");

			if (type != null) {
				queryString.append(" AND ae.Type = ?");
			}

			preparedStatement = connection.prepareStatement(queryString.toString());
			preparedStatement.setString(1, name);
			if (type != null) {
				preparedStatement.setString(2, type);
			}
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				long id = resultSet.getLong("ID");
				String cName = resultSet.getString("Name");
				String cCode = resultSet.getString("Code");
				String country = resultSet.getString("Country");
				Company company = new Company(id, cName, cCode, country);
				if (type != null && type.equals("S")) {
					int nDA = resultSet.getInt("NDA");
					company.setNda(nDA);
				}
				resultList.add(company);
			}
		} catch (Exception ex) {
			LOG.error("getLicenseApprovedCompanies(): " + ex.getMessage(), ex);
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
				LOG.error("getLicenseApprovedCompanies(): " + ex.getMessage(), ex);
			}
		}
		return resultList;
	}

	/**
	 * Get NDA list of a license
	 * 
	 * @param name
	 * @return
	 */
	public static List<NDA> getLicenseNDA(String name) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<NDA> resultList = new ArrayList<NDA>();
		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			StringBuffer queryString = new StringBuffer(
					"SELECT n.ID as ID, n.UserID as UserID, n.LicenseID as LicenseID FROM NDA n, Licenses l ");
			queryString.append("WHERE l.name = ? ");
			queryString.append("AND l.ID  = n.LicenseID ");

			preparedStatement = connection.prepareStatement(queryString.toString());
			preparedStatement.setString(1, name);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				long id = resultSet.getLong("ID");
				String userID = resultSet.getString("UserID");
				long licenseID = resultSet.getLong("LicenseID");
				NDA nda = new NDA(id, userID, licenseID, name);
				resultList.add(nda);
			}
		} catch (Exception ex) {
			LOG.error("getLicenseNDA(): " + ex.getMessage(), ex);
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
				LOG.error("getLicenseNDA(): " + ex.getMessage(), ex);
			}
		}
		return resultList;
	}

	/**
	 * Get authorized licenses of a company based on relationship type
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	public static List<License> getCompanyAuthorizedLicenses(String code, String type) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<License> resultList = new ArrayList<License>();
		try {
			connection = DestinationDBHelper.getDatabaseConnection();
			StringBuffer queryString = new StringBuffer(
					"SELECT l.ID as ID, l.Name as Name, l.Type as Type FROM Entities e, Licenses l, ApprovedEntities ae ");
			queryString.append("WHERE e.code = ? ");
			queryString.append("AND l.ID  = ae.LicenseID ");
			queryString.append("AND ae.EntityID = e.ID");

			if (type != null) {
				queryString.append(" AND ae.Type = ?");
			}

			preparedStatement = connection.prepareStatement(queryString.toString());
			preparedStatement.setString(1, code);
			if (type != null) {
				preparedStatement.setString(2, type);
			}
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				long id = resultSet.getLong("ID");
				String licenseName = resultSet.getString("Name");
				String licenseType = resultSet.getString("Type");
				License license = new License(id, licenseName, licenseType);
				resultList.add(license);
			}
		} catch (Exception ex) {
			LOG.error("getCompanyAuthorizedLicenses(): " + ex.getMessage(), ex);
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
				LOG.error("getCompanyAuthorizedLicenses(): " + ex.getMessage(), ex);
			}
		}
		return resultList;
	}

}
