package de.feinstaubr.server.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class DisplayConfiguration {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="sensorLocationId")
	private SensorLocation sensorLocation;
	
	private String displayType;

	public SensorLocation getSensorLocation() {
		return sensorLocation;
	}

	public void setSensorLocation(SensorLocation sensorLocation) {
		this.sensorLocation = sensorLocation;
	}

	public String getDisplayType() {
		return displayType;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

}
