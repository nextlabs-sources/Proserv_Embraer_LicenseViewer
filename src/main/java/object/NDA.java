package main.java.object;

import java.io.Serializable;

public class NDA implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long ID;
	private String userID;
	private long licenseID;
	private String licenseName;

	public NDA(long iD, String userID, long licenseID, String licenseName) {
		super();
		ID = iD;
		this.userID = userID;
		this.licenseID = licenseID;
		this.licenseName = licenseName;
	}

	public long getID() {
		return ID;
	}

	public void setID(long iD) {
		ID = iD;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public long getLicenseID() {
		return licenseID;
	}

	public void setLicenseID(long licenseID) {
		this.licenseID = licenseID;
	}

	public String getLicenseName() {
		return licenseName;
	}

	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}

}
