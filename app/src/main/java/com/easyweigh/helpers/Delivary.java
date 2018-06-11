package com.easyweigh.helpers;

public class Delivary {
	private String id;
	private String number;
	private String deldate;
	private String totalkgs;
	
	public Delivary(String id,String number, String deldate, String totalkgs) {
		super();
		this.id = id;
		this.number = number;
		this.deldate = deldate;
		this.totalkgs = totalkgs;
	}
	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public String getName() {
		return number;
	}

	public void setName(String number) {
		this.number = number;
	}

	public String getAge() {
		return deldate;
	}

	public void setAge(String deldate) {
		this.deldate = deldate;
	}

	public String getAddress() {
		return totalkgs;
	}

	public void setAddress(String totalkgs) {
		this.totalkgs = totalkgs;
	}

}