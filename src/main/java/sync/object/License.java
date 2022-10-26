package main.java.sync.object;

import java.io.Serializable;

public class License implements Serializable {

	private static final long serialVersionUID = 1L;
	private String licenseName;
	private String type;
	private String approvedParties;
	private String approvedPartiesCode;
	private String countryParties;
	private String approvedSublicensees;
	private String approvedSublicenseesCode;
	private String nDASublicensees;
	private String countrySublicensees;
	private String approvedNationalities;
	private String deniedNationalities;
	
	

	public License(String licenseName, String type, String approvedParties, String approvedPartiesCode,
			String countryParties, String approvedSublicensees, String approvedSublicenseesCode, String nDASublicensees,
			String countrySublicensees, String approvedNationalities, String deniedNationalities) {
		super();
		this.licenseName = licenseName;
		this.type = type;
		this.approvedParties = approvedParties;
		this.approvedPartiesCode = approvedPartiesCode;
		this.countryParties = countryParties;
		this.approvedSublicensees = approvedSublicensees;
		this.approvedSublicenseesCode = approvedSublicenseesCode;
		this.nDASublicensees = nDASublicensees;
		this.countrySublicensees = countrySublicensees;
		this.approvedNationalities = approvedNationalities;
		this.deniedNationalities = deniedNationalities;
	}

	public String getLicenseName() {
		return licenseName;
	}

	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getApprovedParties() {
		return approvedParties;
	}

	public void setApprovedParties(String approvedParties) {
		this.approvedParties = approvedParties;
	}

	public String getCountryParties() {
		return countryParties;
	}

	public void setCountryParties(String countryParties) {
		this.countryParties = countryParties;
	}

	public String getApprovedSublicensees() {
		return approvedSublicensees;
	}

	public void setApprovedSublicensees(String approvedSublicensees) {
		this.approvedSublicensees = approvedSublicensees;
	}

	public String getnDASublicensees() {
		return nDASublicensees;
	}

	public void setnDASublicensees(String nDASublicensees) {
		this.nDASublicensees = nDASublicensees;
	}

	public String getCountrySublicensees() {
		return countrySublicensees;
	}

	public void setCountrySublicensees(String countrySublicensees) {
		if (countrySublicensees != null) {
			countrySublicensees = countrySublicensees.trim();
		}
		this.countrySublicensees = countrySublicensees;
	}

	public String getApprovedNationalities() {
		return approvedNationalities;
	}

	public void setApprovedNationalities(String approvedNationalities) {
		if (approvedNationalities != null) {
			approvedNationalities = approvedNationalities.trim();
		}
		this.approvedNationalities = approvedNationalities;
	}


	public String getDeniedNationalities() {
		return deniedNationalities;
	}

	public void setDeniedNationalities(String deniedNationalities) {
		if (deniedNationalities != null) {
			deniedNationalities = deniedNationalities.trim();
		}
		this.deniedNationalities = deniedNationalities;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getApprovedPartiesCode() {
		return approvedPartiesCode;
	}

	public void setApprovedPartiesCode(String approvedPartiesCode) {
		if (approvedPartiesCode != null) {
			approvedPartiesCode = approvedPartiesCode.trim();
		}
		this.approvedPartiesCode = approvedPartiesCode;
	}

	public String getApprovedSublicenseesCode() {
		return approvedSublicenseesCode;
	}

	public void setApprovedSublicenseesCode(String approvedSublicenseesCode) {
		if (approvedSublicenseesCode != null) {
			approvedSublicenseesCode = approvedSublicenseesCode.trim();
		}
		this.approvedSublicenseesCode = approvedSublicenseesCode;
	}

}
