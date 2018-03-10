package de.feinstaubr.server.entity;

import java.util.Date;

public class MvgDeparture {
	private MvgProduct product;
	private String line;
	private String destination;
	private Date departureTime;
	private MvgStation station;
	
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public Date getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(Date departureTime) {
		this.departureTime = departureTime;
	}
	public String getLine() {
		if (line != null && getProduct() == MvgProduct.SUBWAY) {
			return "U" + line;
		}
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public MvgProduct getProduct() {
		return product;
	}
	public void setProduct(MvgProduct product) {
		this.product = product;
	}
	public MvgStation getStation() {
		return station;
	}
	public void setStation(MvgStation station) {
		this.station = station;
	}
	@Override
	public String toString() {
		return "MvgDeparture [product=" + product + ", line=" + line + ", destination=" + destination
				+ ", departureTime=" + departureTime + "]";
	}
	
}
