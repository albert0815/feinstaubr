package de.feinstaubr.server.boundary;

import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.feinstaubr.server.control.ConfigurationController;
import de.feinstaubr.server.control.WeatherIconCreator;
import de.feinstaubr.server.entity.ForecastSource;
import de.feinstaubr.server.entity.SensorLocation_;
import de.feinstaubr.server.entity.WeatherForecast;
import de.feinstaubr.server.entity.WeatherForecast_;

@Stateless
@Path("forecast")
public class ForecastApi {
	@PersistenceContext
	private EntityManager em;
	
	@Inject
	private WeatherIconCreator weatherIconCreator;
	
	@Inject
	private ConfigurationController configurationController;
	
	@GET
	@Path("{sensorLocationExternalId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNextForecastJson(@PathParam("sensorLocationExternalId") String sensorLocationExternalId) {
		WeatherForecast result = getNextForecast(sensorLocationExternalId);
		if (result != null) {
			return Response.ok(mapForecastToJson(result).build()).build(); 
		} else {
			return Response.ok().build(); 
		}
	}

	public WeatherForecast getNextForecast(String sensorLocationExternalId) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<WeatherForecast> query = criteriaBuilder.createQuery(WeatherForecast.class);
		Root<WeatherForecast> root = query.from(WeatherForecast.class);
		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.HOUR_OF_DAY) > 18) {
			cal.add(Calendar.DATE, 1);
			cal.set(Calendar.HOUR_OF_DAY, 8);
		}
		Predicate dateStartPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get(WeatherForecast_.forecastDate), cal.getTime());
		Predicate poiPredicate = criteriaBuilder.equal(root.get(WeatherForecast_.location).get(SensorLocation_.externalId), sensorLocationExternalId);
		Predicate sourcePredicate = criteriaBuilder.equal(root.get(WeatherForecast_.forecastSource), getCurrentForecastSource());
		query.where(criteriaBuilder.and(dateStartPredicate, poiPredicate, sourcePredicate));
		query.orderBy(criteriaBuilder.asc(root.get(WeatherForecast_.forecastDate)));
		try {
			WeatherForecast result = em.createQuery(query).setMaxResults(1).getSingleResult();
			return result;
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@GET
	@Path("{sensorLocationExternalId}/{period}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getForecasts(@PathParam("sensorLocationExternalId") String sensorLocationExternalId, @PathParam("period")Integer daysToForecast) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<WeatherForecast> query = criteriaBuilder.createQuery(WeatherForecast.class);
		Root<WeatherForecast> root = query.from(WeatherForecast.class);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Predicate dateStartPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get(WeatherForecast_.forecastDate), cal.getTime());
		cal.add(Calendar.DATE, daysToForecast);
		cal.set(Calendar.HOUR_OF_DAY, 1);
		Predicate dateEndPredicate = criteriaBuilder.lessThanOrEqualTo(root.get(WeatherForecast_.forecastDate), cal.getTime());
		Predicate poiPredicate = criteriaBuilder.equal(root.get(WeatherForecast_.location).get(SensorLocation_.externalId), sensorLocationExternalId);
		Predicate sourcePredicate = criteriaBuilder.equal(root.get(WeatherForecast_.forecastSource), getCurrentForecastSource());

		query.where(criteriaBuilder.and(dateStartPredicate, dateEndPredicate, poiPredicate, sourcePredicate));
		query.orderBy(criteriaBuilder.asc(root.get(WeatherForecast_.forecastDate)));
		List<WeatherForecast> result = em.createQuery(query).getResultList();
		JsonArrayBuilder jsonArray = Json.createArrayBuilder();
		for (WeatherForecast forecast : result) {
			jsonArray.add(mapForecastToJson(forecast));
		}
		return Response.ok(jsonArray.build()).build();
	}
	
	public List<WeatherForecast> getForecastFor24hours(String sensorLocationExternalId) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<WeatherForecast> query = criteriaBuilder.createQuery(WeatherForecast.class);
		Root<WeatherForecast> root = query.from(WeatherForecast.class);
		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.HOUR_OF_DAY) >= 12) {
			cal.add(Calendar.DATE, 1);
		}

		cal.set(Calendar.HOUR_OF_DAY, 6);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Predicate dateStartPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get(WeatherForecast_.forecastDate), cal.getTime());
		//cal.add(Calendar.DATE, 1);		
		cal.set(Calendar.HOUR_OF_DAY, 19);

		Predicate dateEndPredicate = criteriaBuilder.lessThanOrEqualTo(root.get(WeatherForecast_.forecastDate), cal.getTime());
		Predicate poiPredicate = criteriaBuilder.equal(root.get(WeatherForecast_.location).get(SensorLocation_.externalId), sensorLocationExternalId);
		Predicate sourcePredicate = criteriaBuilder.equal(root.get(WeatherForecast_.forecastSource), getCurrentForecastSource());

		query.where(criteriaBuilder.and(dateStartPredicate, dateEndPredicate, poiPredicate, sourcePredicate));
		query.orderBy(criteriaBuilder.asc(root.get(WeatherForecast_.forecastDate)));
		List<WeatherForecast> result = em.createQuery(query).getResultList();
		return result;
	}
	
	private ForecastSource getCurrentForecastSource() {
		return configurationController.getConfigurationEnum("weather", "forecast.source", ForecastSource.class, ForecastSource.OPEN_WEATHER);
	}

	private JsonObjectBuilder mapForecastToJson(WeatherForecast forecast) {
		JsonObjectBuilder jsonForecast = Json.createObjectBuilder();
		jsonForecast.add("forecastDate", forecast.getForecastDate().getTime());
		if (forecast.getTemperature() != null) {
			jsonForecast.add("temperature", forecast.getTemperature());
		}
		if (forecast.getPressure() != null) {
			jsonForecast.add("pressure", forecast.getPressure());
		}
		if (forecast.getWeather() != null) {
			Integer weatherIconForWeather = weatherIconCreator.getWeatherIconForWeather(forecast);
			if (weatherIconForWeather != null) {
				jsonForecast.add("weather", weatherIconForWeather);
			}
		}
		if (forecast.getChanceOfRain() != null) {
			jsonForecast.add("chanceOfRain", forecast.getChanceOfRain());
		}
		if (forecast.getCloudCoverTotal() != null) {
			jsonForecast.add("cloudCover", forecast.getCloudCoverTotal());
		}
		if (forecast.getHumidity() != null) {
			jsonForecast.add("humidity", forecast.getHumidity());
		}
		if (forecast.getPrecipitation() != null) {
			jsonForecast.add("precipitation", forecast.getPrecipitation());
		} else {
			jsonForecast.add("precipitation", 0);
		}
		jsonForecast.add("forecastSource", forecast.getForecastSource().toString());
//		jsonForecast.add("windDirection", forecast.getMeanWindDirection());
		return jsonForecast;
	}

}
