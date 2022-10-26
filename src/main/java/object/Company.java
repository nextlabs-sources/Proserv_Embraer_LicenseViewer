package main.java.object;

import java.io.Serializable;

public class Company implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long ID;
	private String name;
	private String code;
	private String country;
	private int nda;

	public Company(long ID, String name, String code, String country) {
		super();
		this.ID = ID;
		this.name = name;
		this.code = code;
		this.country = country;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public long getID() {
		return ID;
	}

	public void setID(long iD) {
		ID = iD;
	}

	public int getNda() {
		return nda;
	}

	public void setNda(int nda) {
		this.nda = nda;
	}

}
