package main.java.bean;

import java.io.Serializable;

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
import main.java.database.NationalityDBHelper;
import main.java.lazy.model.LazyNationalityDataModel;
import main.java.object.Nationality;

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

@ManagedBean(name = "manageNationalityBean")
@ViewScoped
public class ManageNationalityBean implements Serializable {

	private static final Log LOG = LogFactory.getLog(ManageNationalityBean.class);
	private static final long serialVersionUID = 1L;

	@ManagedProperty(value = "#{userSessionBean}")
	private UserSessionBean userSessionBean;
	private LazyNationalityDataModel nationalityLazyList;
	private Nationality newNationality;

	/**
	 * Executed after the bean is constructed
	 * 
	 * @exception Exception
	 *                Any exception
	 */
	@PostConstruct
	public void init() {
		nationalityLazyList = new LazyNationalityDataModel();
		userSessionBean.setActiveTab("nationalities");
		newNationality = new Nationality();
	}

	public ManageNationalityBean() {
	}

	public void createNationality() {
		try {
			
			if (NationalityDBHelper.getNationalityByCode(newNationality.getCode()) != null) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Duplicate Entry", "Country code exists"));
				return;
			}
			
			boolean result = NationalityDBHelper.createNationality(newNationality);
			if (!result) {
				returnMessage(null, FacesMessage.SEVERITY_ERROR, "Unexpected Error", "UNEXPECTED_ERROR_DES");
			}
			
			RequestContext.getCurrentInstance().execute(
					"PF('create-new-dialog').hide()");
			
		} catch (Exception e) {
			LOG.error("createNationality() Error :" + e.getMessage(), e);
			returnMessage(null, FacesMessage.SEVERITY_ERROR, "Unexpected Error", "UNEXPECTED_ERROR_DES");
		}
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

	public LazyNationalityDataModel getNationalityLazyList() {
		return nationalityLazyList;
	}

	public void setNationalityLazyList(LazyNationalityDataModel nationalityLazyList) {
		this.nationalityLazyList = nationalityLazyList;
	}

	public Nationality getNewNationality() {
		return newNationality;
	}

	public void setNewNationality(Nationality newNationality) {
		this.newNationality = newNationality;
	}

}
