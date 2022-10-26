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
import main.java.lazy.model.LazyLicenseDataModel;
import main.java.object.Company;
import main.java.object.License;
import main.java.object.NDA;
import main.java.object.Nationality;

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

@ManagedBean(name = "manageLicenseBean")
@ViewScoped
public class ManageLicenseBean implements Serializable {

	private static final Log LOG = LogFactory.getLog(ManageLicenseBean.class);
	private static final long serialVersionUID = 1L;

	@ManagedProperty(value = "#{userSessionBean}")
	private UserSessionBean userSessionBean;
	private List<License> licenseList;
	private License selectedLicense;
	private List<Nationality> licenseApprovedNationalities;
	private List<Nationality> licenseDeniedNationalities;
	private List<Company> licenseApprovedParties;
	private List<Company> licenseApprovedSublicensees;
	private LazyLicenseDataModel licenseLazyList;
	private List<NDA> nDAList;

	/**
	 * Executed after the bean is constructed
	 * 
	 * @exception Exception
	 *                Any exception
	 */
	@PostConstruct
	public void init() {
		licenseLazyList = new LazyLicenseDataModel();
		userSessionBean.setActiveTab("licenses");
	}

	public ManageLicenseBean() {
	}

	public void getLicenseDetails() {

		if (selectedLicense == null) {
			returnUnexpectedError(null);
			LOG.error("getLicenseDetails() Cannot receive selected license");
			return;
		}

		LOG.debug("getLicenseDetails() Get license details of " + selectedLicense.getName());

		licenseApprovedNationalities = RelationshipDBHelper.getLicenseApprovedNationalities(selectedLicense.getName());
		licenseDeniedNationalities = RelationshipDBHelper.getLicenseDeniedNationalities(selectedLicense.getName());
		licenseApprovedParties = RelationshipDBHelper.getLicenseApprovedCompanies(selectedLicense.getName(), "P");
		licenseApprovedSublicensees = RelationshipDBHelper.getLicenseApprovedCompanies(selectedLicense.getName(), "S");
		nDAList = RelationshipDBHelper.getLicenseNDA(selectedLicense.getName());

		RequestContext.getCurrentInstance().execute("PF('license-details-dialog').show()");
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

	public List<License> getLicenseList() {
		return licenseList;
	}

	public void setLicenseList(List<License> licenseList) {
		this.licenseList = licenseList;
	}

	public LazyLicenseDataModel getLicenseLazyList() {
		return licenseLazyList;
	}

	public void setLicenseLazyList(LazyLicenseDataModel licenseLazyList) {
		this.licenseLazyList = licenseLazyList;
	}

	public License getSelectedLicense() {
		return selectedLicense;
	}

	public void setSelectedLicense(License selectedLicense) {
		this.selectedLicense = selectedLicense;
	}

	public List<Nationality> getLicenseApprovedNationalities() {
		return licenseApprovedNationalities;
	}

	public void setLicenseApprovedNationalities(List<Nationality> licenseApprovedNationalities) {
		this.licenseApprovedNationalities = licenseApprovedNationalities;
	}

	public List<Company> getLicenseApprovedParties() {
		return licenseApprovedParties;
	}

	public void setLicenseApprovedParties(List<Company> licenseApprovedParties) {
		this.licenseApprovedParties = licenseApprovedParties;
	}

	public List<Company> getLicenseApprovedSublicensees() {
		return licenseApprovedSublicensees;
	}

	public void setLicenseApprovedSublicensees(List<Company> licenseApprovedSublicensees) {
		this.licenseApprovedSublicensees = licenseApprovedSublicensees;
	}

	public List<Nationality> getLicenseDeniedNationalities() {
		return licenseDeniedNationalities;
	}

	public void setLicenseDeniedNationalities(List<Nationality> licenseDeniedNationalities) {
		this.licenseDeniedNationalities = licenseDeniedNationalities;
	}

	public List<NDA> getnDAList() {
		return nDAList;
	}

	public void setnDAList(List<NDA> nDAList) {
		this.nDAList = nDAList;
	}

}
