package main.java.object;

import java.io.Serializable;

import javax.naming.directory.Attributes;

import main.java.ad.ActiveDirectoryHelper;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private Attributes attributes;
	private String aduser;
	private String displayName;
	private String email;

	public User() {
	}

	public User(Attributes attributes) {
		this.attributes = attributes;
		this.aduser = getAttribute("sAMAccountName");
		this.displayName = getAttribute("displayName");
		this.email = getAttribute("mail");
	}

	public User(String aduser, String displayName, String email) {
		this.aduser = aduser;
		this.displayName = displayName;
		this.email = email;
		this.attributes = null;
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public String getAttribute(String attributeName) {
		return ActiveDirectoryHelper.getAttribute(attributes, attributeName);
	}

	public String getAduser() {
		return aduser;
	}

	public void setAduser(String aduser) {
		this.aduser = aduser;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
