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
import main.java.lazy.model.LazyNDADataModel;
import main.java.lazy.model.LazyNationalityDataModel;
import main.java.object.Company;
import main.java.object.License;
import main.java.object.Nationality;

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

@ManagedBean(name = "manageNDABean")
@ViewScoped
public class ManageNDABean implements Serializable {

	private static final Log LOG = LogFactory.getLog(ManageNDABean.class);
	private static final long serialVersionUID = 1L;

	@ManagedProperty(value = "#{userSessionBean}")
	private UserSessionBean userSessionBean;
	private LazyNDADataModel ndaLazyList;

	/**
	 * Executed after the bean is constructed
	 * 
	 * @exception Exception
	 *                Any exception
	 */
	@PostConstruct
	public void init() {
		ndaLazyList = new LazyNDADataModel();
		userSessionBean.setActiveTab("nda");
	}

	public ManageNDABean() {
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

	public LazyNDADataModel getNdaLazyList() {
		return ndaLazyList;
	}

	public void setNdaLazyList(LazyNDADataModel ndaLazyList) {
		this.ndaLazyList = ndaLazyList;
	}

}
