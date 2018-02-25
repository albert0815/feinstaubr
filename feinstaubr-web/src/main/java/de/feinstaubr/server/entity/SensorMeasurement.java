package de.feinstaubr.server.entity;

import java.math.BigDecimal;
import java.util.Date;

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
	
	@ManyToOne
	@JoinColumn(name="sensorId")
	private Sensor sensorId;
	
	private BigDecimal value;
	private BigDecimal calculatedValue;
	
	@ManyToOne
	@JoinColumn(name="type")
	private SensorMeasurementType type;
	
	private transient Trend trend;
	
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Sensor getSensorId() {
		return sensorId;
	}
	public void setSensorId(Sensor sensorId) {
		this.sensorId = sensorId;
	}
	
	public BigDecimal getCalculatedValue() {
		return calculatedValue;
	}
	public void setCalculatedValue(BigDecimal calculatedValue) {
		this.calculatedValue = calculatedValue;
	}
	public BigDecimal getValue() {
		if (calculatedValue != null) {
			return calculatedValue;
		} else {
			
		}
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
	
	public Trend getTrend() {
		return trend;
	}
	public void setTrend(Trend trend) {
		this.trend = trend;
	}
	@Override
	public String toString() {
		return "SensorMeasurement [id=" + id + ", value=" + value + ", date=" + date + ", sensorId=" + sensorId 
				+ ", type=" + type + "]";
	}
	
	

	
}
