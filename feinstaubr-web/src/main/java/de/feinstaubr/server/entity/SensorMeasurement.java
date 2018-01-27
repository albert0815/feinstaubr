package de.feinstaubr.server.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SensorMeasurement {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private Date date;
	private String sensorId;
	private String softwareVersion;
	private BigDecimal temperatur;
	private BigDecimal humidity;
	private BigDecimal p1;
	private BigDecimal p2;

	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getSensorId() {
		return sensorId;
	}
	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}
	public String getSoftwareVersion() {
		return softwareVersion;
	}
	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}
	public BigDecimal getTemperatur() {
		return temperatur;
	}
	public void setTemperatur(BigDecimal temperatur) {
		this.temperatur = temperatur;
	}
	public BigDecimal getHumidity() {
		return humidity;
	}
	public void setHumidity(BigDecimal humidity) {
		this.humidity = humidity;
	}
	public BigDecimal getP1() {
		return p1;
	}
	public void setP1(BigDecimal p1) {
		this.p1 = p1;
	}
	public BigDecimal getP2() {
		return p2;
	}
	public void setP2(BigDecimal p2) {
		this.p2 = p2;
	}
	@Override
	public String toString() {
		return "SensorMeasurement [id=" + id + ", date=" + date + ", sensorId=" + sensorId + ", softwareVersion="
				+ softwareVersion + ", temperatur=" + temperatur + ", humidity=" + humidity + ", p1=" + p1 + ", p2="
				+ p2 + "]";
	}
	
	
	
	
}
