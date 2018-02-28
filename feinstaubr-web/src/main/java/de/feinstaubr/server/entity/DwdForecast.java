package de.feinstaubr.server.entity;

import java.math.BigDecimal;
import java.util.Date;

public class DwdForecast {
	private Date forecastDate;
	private BigDecimal temperature;
	private BigDecimal pressure;
	private DwdWeather weather;
	private BigDecimal cloudCoverTotal;
	private BigDecimal chanceOfRain;
	private BigDecimal meanWindDirection;
	private BigDecimal meanWindSpeed;
	public Date getForecastDate() {
		return forecastDate;
	}
	public void setForecastDate(Date forecastDate) {
		this.forecastDate = forecastDate;
	}
	public BigDecimal getTemperature() {
		return temperature;
	}
	public void setTemperature(BigDecimal temperature) {
		this.temperature = temperature;
	}
	public BigDecimal getPressure() {
		return pressure;
	}
	public void setPressure(BigDecimal pressure) {
		this.pressure = pressure;
	}
	public DwdWeather getWeather() {
		return weather;
	}
	public void setWeather(DwdWeather weather) {
		this.weather = weather;
	}
	public BigDecimal getCloudCoverTotal() {
		return cloudCoverTotal;
	}
	public void setCloudCoverTotal(BigDecimal cloudCoverTotal) {
		this.cloudCoverTotal = cloudCoverTotal;
	}
	public BigDecimal getChanceOfRain() {
		return chanceOfRain;
	}
	public void setChanceOfRain(BigDecimal chanceOfRain) {
		this.chanceOfRain = chanceOfRain;
	}
	public BigDecimal getMeanWindDirection() {
		return meanWindDirection;
	}
	public void setMeanWindDirection(BigDecimal meanWindDirection) {
		this.meanWindDirection = meanWindDirection;
	}
	public BigDecimal getMeanWindSpeed() {
		return meanWindSpeed;
	}
	public void setMeanWindSpeed(BigDecimal meanWindSpeed) {
		this.meanWindSpeed = meanWindSpeed;
	}
	
}
