package main.java.object;

import java.io.Serializable;

public class Nationality implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long ID;

	private String name;

	private String code;

	public Nationality() {
	}

	public Nationality(long ID, String name, String code) {
		super();
		this.ID = ID;
		this.name = name;
		this.code = code;
	}

	public long getID() {
		return ID;
	}

	public void setID(long iD) {
		ID = iD;
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

}
