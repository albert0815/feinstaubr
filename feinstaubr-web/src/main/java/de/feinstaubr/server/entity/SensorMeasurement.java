package de.feinstaubr.server.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="sensormeasurementvalues")
public class SensorMeasurement {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private Date date;
	private String sensorId;
	private BigDecimal value;
	
	@ManyToOne
	@JoinColumn(name="type")
	private SensorMeasurementType type;
	
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
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public SensorMeasurementType getType() {
		return type;
	}
	public void setType(SensorMeasurementType type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "SensorMeasurement [id=" + id + ", value=" + value + ", date=" + date + ", sensorId=" + sensorId 
				+ ", type=" + type + "]";
	}
	
	

	
}
