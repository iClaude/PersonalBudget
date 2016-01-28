package com.flingsoftware.personalbudget.database;

public class Conto {
	private long id;
	private String conto;
	private double saldo;
	private long dataSaldo;
	
	
	public Conto() {
		
	}
	
	public Conto(long id, String conto, double saldo, long dataSaldo) {
		setId(id);
		setConto(conto);
		setSaldo(saldo);
		setDataSaldo(dataSaldo);
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return this.id;
	}
	
	public void setConto(String conto) {
		this.conto = conto;
	}
	
	public String getConto() {
		return this.conto;
	}
	
	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}
	
	public double getSaldo() {
		return this.saldo;
	}
	
	public void setDataSaldo(long dataSaldo) {
		this.dataSaldo = dataSaldo;
	}
	
	public long getDataSaldo() {
		return this.dataSaldo;
	}
}
