package de.feinstaubr.server.control;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import de.feinstaubr.server.entity.WeatherEnum;
import de.feinstaubr.server.entity.WeatherForecast;

@Stateless
public class OpenWeatherController {
	@PersistenceContext
	private EntityManager em;
	
	@Inject
	private ConfigurationController config;
	
	public List<WeatherForecast> getForecast(String id) {
		String apikey = config.getConfiguration("openweather", "apikey");
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://api.openweathermap.org/data/2.5/forecast");
		JsonObject o = target.queryParam("id", id).queryParam("apikey", apikey).queryParam("units", "metric").request(MediaType.APPLICATION_JSON_TYPE).get(JsonObject.class);
		JsonArray jsonForecasts = o.getJsonArray("list");
		List<WeatherForecast> forecastResultList = new ArrayList<>();
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
				forecast.setWeather(WeatherEnum.getEnumForOpenWeatherId(jsonWeather.getString("main")));
			}
			forecastResultList.add(forecast);
		}
		
		return forecastResultList;
		
	}
}
