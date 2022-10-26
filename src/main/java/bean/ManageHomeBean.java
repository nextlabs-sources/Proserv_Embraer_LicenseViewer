package main.java.bean;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@ManagedBean(name = "manageHomeBean")
@ViewScoped
public class ManageHomeBean implements Serializable {

	private static final Log LOG = LogFactory.getLog(ManageHomeBean.class);
	private static final long serialVersionUID = 1L;
	@ManagedProperty(value = "#{userSessionBean}")
	private UserSessionBean userSessionBean;

	@PostConstruct
	public void init() {
		userSessionBean.setActiveTab("home");
	}

}
