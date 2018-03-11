package de.feinstaubr.server.boundary;

import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
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

import de.feinstaubr.server.entity.WeatherForecast;
import de.feinstaubr.server.entity.DwdForecast_;
import de.feinstaubr.server.entity.SensorLocation_;

@Stateless
@Path("forecast")
public class ForecastApi {
	@PersistenceContext
	private EntityManager em;
	
	@GET
	@Path("{poi}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNextForecastJson(@PathParam("poi") String poi) {
		WeatherForecast result = getNextForecast(poi);
		return Response.ok(mapForecastToJson(result).build()).build(); 
	}

	public WeatherForecast getNextForecast(String poi) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<WeatherForecast> query = criteriaBuilder.createQuery(WeatherForecast.class);
		Root<WeatherForecast> root = query.from(WeatherForecast.class);
		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.HOUR_OF_DAY) > 18) {
			cal.add(Calendar.DATE, 1);
			cal.set(Calendar.HOUR_OF_DAY, 8);
		}
		Predicate dateStartPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get(DwdForecast_.forecastDate), cal.getTime());
		Predicate poiPredicate = criteriaBuilder.equal(root.get(DwdForecast_.location).get(SensorLocation_.dwdPoiId), poi);
		query.where(criteriaBuilder.and(dateStartPredicate, poiPredicate));
		query.orderBy(criteriaBuilder.asc(root.get(DwdForecast_.forecastDate)));
		WeatherForecast result = em.createQuery(query).setMaxResults(1).getSingleResult();
		return result;
	}
	
	@GET
	@Path("{poi}/{period}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getForecasts(@PathParam("poi") String poi, @PathParam("period")Integer daysToForecast) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<WeatherForecast> query = criteriaBuilder.createQuery(WeatherForecast.class);
		Root<WeatherForecast> root = query.from(WeatherForecast.class);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Predicate dateStartPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get(DwdForecast_.forecastDate), cal.getTime());
		cal.add(Calendar.DATE, daysToForecast);
		cal.set(Calendar.HOUR_OF_DAY, 1);
		Predicate dateEndPredicate = criteriaBuilder.lessThanOrEqualTo(root.get(DwdForecast_.forecastDate), cal.getTime());
		Predicate poiPredicate = criteriaBuilder.equal(root.get(DwdForecast_.location).get(SensorLocation_.dwdPoiId), poi);

		query.where(criteriaBuilder.and(dateStartPredicate, dateEndPredicate, poiPredicate));
		query.orderBy(criteriaBuilder.asc(root.get(DwdForecast_.forecastDate)));
		List<WeatherForecast> result = em.createQuery(query).getResultList();
		JsonArrayBuilder jsonArray = Json.createArrayBuilder();
		for (WeatherForecast forecast : result) {
			jsonArray.add(mapForecastToJson(forecast));
		}
		return Response.ok(jsonArray.build()).build();
	}
	
	public List<WeatherForecast> getForecastFor24hours(String poi) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<WeatherForecast> query = criteriaBuilder.createQuery(WeatherForecast.class);
		Root<WeatherForecast> root = query.from(WeatherForecast.class);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 6);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Predicate dateStartPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get(DwdForecast_.forecastDate), cal.getTime());
		//cal.add(Calendar.DATE, 1);		
		cal.set(Calendar.HOUR_OF_DAY, 20);

		Predicate dateEndPredicate = criteriaBuilder.lessThanOrEqualTo(root.get(DwdForecast_.forecastDate), cal.getTime());
		Predicate poiPredicate = criteriaBuilder.equal(root.get(DwdForecast_.location).get(SensorLocation_.dwdPoiId), poi);
		query.where(criteriaBuilder.and(dateStartPredicate, dateEndPredicate, poiPredicate));
		query.orderBy(criteriaBuilder.asc(root.get(DwdForecast_.forecastDate)));
		List<WeatherForecast> result = em.createQuery(query).getResultList();
		return result;

	}

	private JsonObjectBuilder mapForecastToJson(WeatherForecast forecast) {
		JsonObjectBuilder jsonForecast = Json.createObjectBuilder();
		jsonForecast.add("forecastDate", forecast.getForecastDate().getTime());
		jsonForecast.add("temperature", forecast.getTemperature());
		jsonForecast.add("pressure", forecast.getPressure());
		if (forecast.getWeather() != null) {
			jsonForecast.add("weather", forecast.getWeather().getCodepoint());
		}
		jsonForecast.add("chanceOfRain", forecast.getChanceOfRain());
		jsonForecast.add("cloudCover", forecast.getCloudCoverTotal());
//		jsonForecast.add("windSpeed", forecast.getMeanWindSpeed());
//		jsonForecast.add("windDirection", forecast.getMeanWindDirection());
		return jsonForecast;
	}

}
