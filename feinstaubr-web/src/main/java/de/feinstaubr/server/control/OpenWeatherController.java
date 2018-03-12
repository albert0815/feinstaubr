package de.feinstaubr.server.control;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import de.feinstaubr.server.entity.ForecastSource;
import de.feinstaubr.server.entity.WeatherForecast;

@Stateless
public class OpenWeatherController {
	private static final Logger LOGGER = Logger.getLogger(OpenWeatherController.class.getName());
	
	@PersistenceContext
	private EntityManager em;
	
	@Inject
	private ConfigurationController config;
	
	public List<WeatherForecast> getForecast(String id) {
		String apikey = config.getConfiguration("openweather", "apikey");
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://api.openweathermap.org/data/2.5/forecast");
		LOGGER.info("requesting forecasts from open weather from " + target.getUri().toString() + " for " + id);
		List<WeatherForecast> forecastResultList = new ArrayList<>();
		try {
			JsonObject o = target.queryParam("id", id).queryParam("apikey", apikey).queryParam("units", "metric").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
			JsonArray jsonForecasts = o.getJsonArray("list");
			for (JsonValue forecastJsonValue : jsonForecasts) {
				JsonObject forecastJson = (JsonObject) forecastJsonValue;
				WeatherForecast forecast = new WeatherForecast();
				forecast.setForecastSource(ForecastSource.OPEN_WEATHER);
				forecast.setForecastDate(new Date(forecastJson.getJsonNumber("dt").longValue() * 1000));
				if (forecastJson.containsKey("clouds")) {
					forecast.setCloudCoverTotal(forecastJson.getJsonObject("clouds").getJsonNumber("all").bigDecimalValue().setScale(2, RoundingMode.HALF_UP));
				}
				forecast.setLastUpdate(new Date());
				if (forecastJson.containsKey("wind")) {
					forecast.setMeanWindDirection(forecastJson.getJsonObject("wind").getJsonNumber("deg").bigDecimalValue().setScale(2, RoundingMode.HALF_UP));
					forecast.setMeanWindSpeed(forecastJson.getJsonObject("wind").getJsonNumber("speed").bigDecimalValue().setScale(2, RoundingMode.HALF_UP));
				}
				if (forecastJson.containsKey("rain")) {
					JsonNumber jsonNumber = forecastJson.getJsonObject("rain").getJsonNumber("3h");
					if  (jsonNumber != null) {
						forecast.setPrecipitation(jsonNumber.bigDecimalValue().setScale(2, RoundingMode.HALF_UP));
					}
				}
				if (forecastJson.containsKey("main")) {
					forecast.setPressure(forecastJson.getJsonObject("main").getJsonNumber("sea_level").bigDecimalValue().setScale(2, RoundingMode.HALF_UP));
					forecast.setTemperature(forecastJson.getJsonObject("main").getJsonNumber("temp").bigDecimalValue().setScale(2, RoundingMode.HALF_UP));
					forecast.setHumidity(forecastJson.getJsonObject("main").getJsonNumber("humidity").bigDecimalValue().setScale(2, RoundingMode.HALF_UP));
				}
				if (forecastJson.containsKey("weather")) {
					JsonObject jsonWeather = (JsonObject) forecastJson.getJsonArray("weather").get(0);
					forecast.setWeather(String.valueOf(jsonWeather.getInt("id")));
				}
				forecastResultList.add(forecast);
			}
		} catch (RuntimeException e) {
			LOGGER.log(Level.SEVERE, "unable to retrieve forcasts for " + id, e);
		}
		
		return forecastResultList;
		
	}
	
}
