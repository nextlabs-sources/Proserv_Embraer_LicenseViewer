package main.java.bean;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.context.RequestContext;

import main.java.ad.ActiveDirectoryHelper;
import main.java.common.PropertyLoader;
import main.java.database.DestinationDBHelper;
import main.java.object.User;

@ManagedBean(name = "userSessionBean")
@SessionScoped
public class UserSessionBean implements Serializable {

	private static final Log LOG = LogFactory.getLog(UserSessionBean.class);
	private static final long serialVersionUID = 1L;
	private String activeTab = "licenses";
	private Boolean isLoggedIn;
	private String userName;
	private String password;
	private User loggedInUser;

	public void preRender() {
		// LOG.debug("preRender() Prerender");

		if (isLoggedIn != null && isLoggedIn) {
			checkDB();
			return;
		}

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

		try {
			if (isLoggedIn == null || !isLoggedIn) {
				LOG.info("preRender() User is not logged in");
				externalContext.redirect(externalContext.getRequestContextPath() + "/login.xhtml");
				return;
			}
		} catch (Exception e) {
			LOG.error("UserSessionBean preRender(): " + e.getMessage(), e);
		}

		// updateProperties();

	}

	public void preRenderLogin() {
		LOG.info("preRenderLogin() Prerender");

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

		try {
			if (isLoggedIn != null && isLoggedIn) {
				LOG.info("UserSessionBean preRender(): User is logged in");

				externalContext.redirect(externalContext.getRequestContextPath() + "/licenses.xhtml");
				return;

			} else {
				updateProperties();
				isLoggedIn = false;
				return;
			}
		} catch (Exception e) {
			LOG.error("UserSessionBean preRender(): " + e.getMessage(), e);
		}

	}

	public void checkDB() {
		Connection connection = DestinationDBHelper.getDatabaseConnectionSimple();
		if (connection == null) {
			returnMessage("growl", FacesMessage.SEVERITY_ERROR, "DB_FAILED_MSG", "DB_FAILED_DES");
			return;
		} else {
			try {
				connection.close();
			} catch (SQLException e) {
				LOG.error("checkDB(): Cannot close DB connection");
			}
		}
	}

	public void updateProperties() {
		try {
			// get properties file
			// FacesContext ctx = FacesContext.getCurrentInstance();

			PropertyLoader.loadProperties();
			PropertyLoader.loadConstants();
		} catch (Exception e) {
			LOG.error("updateProperties(): " + e.getMessage(), e);
		}
	}

	public String login() {
		try {
			String authorizedGroup = PropertyLoader.properties.getProperty("authorized_group");

			if (authorizedGroup == null || authorizedGroup.length() == 0) {
				returnMessage(null, FacesMessage.SEVERITY_ERROR, "LOGIN_FAILED_MSG",
						"LOGIN_FAILED_INVALID_PROPERTY_DES");
				return null;
			}

			if (userName == null || userName.length() == 0 || password == null || password.length() == 0) {
				LOG.info("login(): Invalid input");
				returnMessage(null, FacesMessage.SEVERITY_ERROR, "LOGIN_FAILED_MSG", "LOGIN_FAILED_INVALID_INPUT_DES");
				return null;
			}

			// AD Authentication
			if (!ActiveDirectoryHelper.authenticate(userName, password)) {
				LOG.info("Invalid user name or password");
				returnMessage(null, FacesMessage.SEVERITY_ERROR, "LOGIN_FAILED_MSG",
						"LOGIN_FAILED_INVALID_CREDENTIAL_DES");
				return null;
			}

			

			// get all groups that the logged in user is member of

			if (PropertyLoader.properties.getProperty("require-group-authentication", "false")
					.equalsIgnoreCase("true")) {
				
				String filter = "(entryDN=" + userName + ")";

				loggedInUser = ActiveDirectoryHelper.getUser(filter, null, userName, password);

				List<String> userGroups = ActiveDirectoryHelper.getAllUserGroup(filter, userName, password);

				if (userGroups.contains(authorizedGroup)) {
					returnMessage(null, FacesMessage.SEVERITY_ERROR, "LOGIN_FAILED_MSG", "UNAUTHORIZED_DES");
				}
				
				LOG.info("UserSessionBean login(): Logged in user: " + loggedInUser.getAttribute("distinguishedName"));
			}

			

			// set login flag
			isLoggedIn = true;
		} catch (Exception e) {
			LOG.error("UserSessionBean login():" + e.getMessage(), e);
			returnUnexpectedError(null);
		}

		return "licenses?faces-redirect=true";
	}

	public String logout() {
		isLoggedIn = false;
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		return "login?faces-redirect=true";
	}

	public UserSessionBean() {
	}

	public String getMenuClass(String tab) {
		if (tab.equalsIgnoreCase(activeTab)) {
			return "menu-active";
		} else {
			return "menu-inactive";
		}
	}

	public String setActive(String tab) {
		LOG.debug("setActive() Set tab " + tab + " to active");
		activeTab = tab;
		return tab + "?faces-redirect=true";
	}

	public void setTab(String tab) {
		activeTab = tab;
	}

	public void returnUnexpectedError(String id) {
		returnMessage(id, FacesMessage.SEVERITY_ERROR, "Unexpected Error", "UNEXPECTED_ERROR_DES");
	}

	public void returnMessage(String id, Severity level, String sum, String des) {
		FacesContext.getCurrentInstance().addMessage(id,
				new FacesMessage(level, PropertyLoader.getConstant(sum), PropertyLoader.getConstant(des)));
	}

	public String getActiveTab() {
		return activeTab;
	}

	public void setActiveTab(String activeTab) {
		this.activeTab = activeTab;
		RequestContext.getCurrentInstance().update(":menu-form");
	}

	public Boolean getIsLoggedIn() {
		return isLoggedIn;
	}

	public void setIsLoggedIn(Boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public User getLoggedInUser() {
		return loggedInUser;
	}

	public void setLoggedInUser(User loggedInUser) {
		this.loggedInUser = loggedInUser;
	}

}
