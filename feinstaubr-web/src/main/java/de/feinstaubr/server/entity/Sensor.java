package de.feinstaubr.server.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Sensor {
	@Id
	private String sensorId;
	
	private String name;
	
	@OneToMany(mappedBy="sensorId", fetch=FetchType.LAZY)
	private List<SensorMeasurement> measurements;

	public String getSensorId() {
		return sensorId;
	}
	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
