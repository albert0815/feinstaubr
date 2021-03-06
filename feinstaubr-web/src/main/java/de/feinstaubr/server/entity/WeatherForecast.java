package de.feinstaubr.server.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class WeatherForecast {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@OneToOne
	@JoinColumn(name = "location")
	private SensorLocation location;
	
	private Date forecastDate;
	private Date lastUpdate;
	private BigDecimal temperature;
	private BigDecimal pressure;

	private String weather;
	private BigDecimal cloudCoverTotal;
	private BigDecimal chanceOfRain;
	private BigDecimal meanWindDirection;
	private BigDecimal meanWindSpeed;
	
	@Enumerated(EnumType.STRING)
	private ForecastSource forecastSource;
	private BigDecimal precipitation;
	private BigDecimal humidity;
	
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
	public String getWeather() {
		return weather;
	}
	public void setWeather(String weather) {
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
	public SensorLocation getLocation() {
		return location;
	}
	public void setLocation(SensorLocation location) {
		this.location = location;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public ForecastSource getForecastSource() {
		return forecastSource;
	}
	public void setForecastSource(ForecastSource forecastSource) {
		this.forecastSource = forecastSource;
	}
	public BigDecimal getPrecipitation() {
		return precipitation;
	}
	public void setPrecipitation(BigDecimal precipitation) {
		this.precipitation = precipitation;
	}
	public BigDecimal getHumidity() {
		return humidity;
	}
	public void setHumidity(BigDecimal humidity) {
		this.humidity = humidity;
	}	
}
