package de.feinstaubr.server.entity;

public enum ForecastSource {
	DWD("dwd"), OPEN_WEATHER("openweather");
	
	private String id;
	
	ForecastSource(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
}
