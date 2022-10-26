package main.java.sync.object;

public class NDAUser {

	private String employeeName;
	private String licenseName;

	public NDAUser(String employeeName, String licenseName) {
		super();
		this.employeeName = employeeName;
		this.licenseName = licenseName;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public String getLicenseName() {
		return licenseName;
	}

	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}

}
