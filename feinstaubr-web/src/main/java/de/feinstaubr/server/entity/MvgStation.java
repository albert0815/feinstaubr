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
	private Integer footway;
	private double latitude;
	private double longitude;

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

	public Integer getFootway() {
		return footway;
	}

	public void setFootway(Integer footway) {
		this.footway = footway;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public double getDistanceTo(double currentLocationLatitude, double currentLocationLongitude) {
	    double earthRadius = 6371.0; // miles (or 6371.0 kilometers)
	    double dLat = Math.toRadians(currentLocationLatitude - latitude);
	    double dLng = Math.toRadians(currentLocationLongitude - longitude);
	    double sindLat = Math.sin(dLat / 2);
	    double sindLng = Math.sin(dLng / 2);
	    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
	            * Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(currentLocationLatitude));
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    return dist;
	}
}
