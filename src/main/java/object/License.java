package main.java.object;

import java.io.Serializable;

public class License implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long ID;

	private String name;

	private String type;

	public License(long id, String name, String type) {
		super();
		this.ID = id;
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getID() {
		return ID;
	}

	public void setID(long iD) {
		ID = iD;
	}

}
