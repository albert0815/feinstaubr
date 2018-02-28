package de.feinstaubr.server.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MvgStation {
	@Id
	private long stationId;
	private String name;
	private String destinationFilter;

	private transient List<MvgDeparture> departures;
	
	public long getStationId() {
		return stationId;
	}

	public void setStationId(long stationId) {
		this.stationId = stationId;
	}
	
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDestinationFilter() {
		return destinationFilter;
	}

	public void setDestinationFilter(String destinationFilter) {
		this.destinationFilter = destinationFilter;
	}

	public List<MvgDeparture> getDepartures() {
		return departures;
	}

	public void setDepartures(List<MvgDeparture> departures) {
		this.departures = departures;
	}
}
