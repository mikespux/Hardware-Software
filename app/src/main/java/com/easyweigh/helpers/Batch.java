package com.easyweigh.helpers;

public class Batch {
	private String id;
	private String number;
	private String deldate;
	private String batchno;
	private String totalkgs;

	public Batch(String id, String number, String deldate, String batchno, String totalkgs) {
		super();
		this.id = id;
		this.number = number;
		this.deldate = deldate;
		this.totalkgs = totalkgs;
		this.batchno = batchno;
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

	public String getBatchNo() {
		return batchno;
	}

	public void setBatchNo(String batchno) {
		this.batchno = batchno;
	}


	public String getAddress() {
		return totalkgs;
	}

	public void setAddress(String totalkgs) {
		this.totalkgs = totalkgs;
	}

}