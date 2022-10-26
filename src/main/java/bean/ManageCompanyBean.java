package main.java.bean;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.context.RequestContext;

import main.java.common.PropertyLoader;
import main.java.database.RelationshipDBHelper;
import main.java.lazy.model.LazyCompanyDataModel;
import main.java.object.Company;
import main.java.object.License;

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

@ManagedBean(name = "manageCompanyBean")
@ViewScoped
public class ManageCompanyBean implements Serializable {

	private static final Log LOG = LogFactory.getLog(ManageCompanyBean.class);
	private static final long serialVersionUID = 1L;

	@ManagedProperty(value = "#{userSessionBean}")
	private UserSessionBean userSessionBean;
	private LazyCompanyDataModel companyLazyList;
	private List<License> authorizedLicensesParties;
	private List<License> authorizedLicensesSublicensees;
	private Company selectedCompany;

	/**
	 * Executed after the bean is constructed
	 * 
	 * @exception Exception
	 *                Any exception
	 */
	@PostConstruct
	public void init() {
		companyLazyList = new LazyCompanyDataModel();
		userSessionBean.setActiveTab("companies");
	}

	public ManageCompanyBean() {
	}

	public void getCompanyDetails() {

		if (selectedCompany == null) {
			returnUnexpectedError(null);
			LOG.error("getCompanyDetails() Cannot receive selected company");
			return;
		}

		LOG.debug("getCompanyDetails() Get company details of " + selectedCompany.getCode());

		authorizedLicensesParties = RelationshipDBHelper.getCompanyAuthorizedLicenses(selectedCompany.getCode(), "P");
		authorizedLicensesSublicensees = RelationshipDBHelper.getCompanyAuthorizedLicenses(selectedCompany.getCode(),
				"S");

		RequestContext.getCurrentInstance().execute("PF('company-details-dialog').show()");
	}

	public void returnUnexpectedError(String id) {
		returnMessage(id, FacesMessage.SEVERITY_ERROR, "Unexpected Error", "UNEXPECTED_ERROR_DES");
	}

	public void returnMessage(String id, Severity level, String sum, String des) {
		FacesContext.getCurrentInstance().addMessage(id,
				new FacesMessage(level, PropertyLoader.getConstant(sum), PropertyLoader.getConstant(des)));
	}

	public UserSessionBean getUserSessionBean() {
		return userSessionBean;
	}

	public void setUserSessionBean(UserSessionBean userSessionBean) {
		this.userSessionBean = userSessionBean;
	}

	public LazyCompanyDataModel getCompanyLazyList() {
		return companyLazyList;
	}

	public void setCompanyLazyList(LazyCompanyDataModel companyLazyList) {
		this.companyLazyList = companyLazyList;
	}

	public List<License> getAuthorizedLicensesParties() {
		return authorizedLicensesParties;
	}

	public void setAuthorizedLicensesParties(List<License> authorizedLicensesParties) {
		this.authorizedLicensesParties = authorizedLicensesParties;
	}

	public List<License> getAuthorizedLicensesSublicensees() {
		return authorizedLicensesSublicensees;
	}

	public void setAuthorizedLicensesSublicensees(List<License> authorizedLicensesSublicensees) {
		this.authorizedLicensesSublicensees = authorizedLicensesSublicensees;
	}

	public Company getSelectedCompany() {
		return selectedCompany;
	}

	public void setSelectedCompany(Company selectedCompany) {
		this.selectedCompany = selectedCompany;
	}

}
