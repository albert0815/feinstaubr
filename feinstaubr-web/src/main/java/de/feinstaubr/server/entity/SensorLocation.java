package de.feinstaubr.server.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class SensorLocation {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private String locationName;
	private String dwdPoiId;
	private String openWeatherId;
	
	@OneToMany(mappedBy="location")
	private List<Sensor> sensors;

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}

	public String getDwdPoiId() {
		return dwdPoiId;
	}

	public void setDwdPoiId(String dwdPoiId) {
		this.dwdPoiId = dwdPoiId;
	}

	public String getOpenWeatherId() {
		return openWeatherId;
	}

	public void setOpenWeatherId(String openWeatherId) {
		this.openWeatherId = openWeatherId;
	}
	
	
}
