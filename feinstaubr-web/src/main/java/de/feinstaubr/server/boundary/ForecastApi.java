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

import de.feinstaubr.server.entity.DwdForecast;
import de.feinstaubr.server.entity.DwdForecast_;

@Stateless
@Path("forecast")
public class ForecastApi {
	@PersistenceContext
	private EntityManager em;
	
	@GET
	@Path("{poi}/{period}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getForecasts(@PathParam("poi") String poi, @PathParam("period")Integer daysToForecast) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<DwdForecast> query = criteriaBuilder.createQuery(DwdForecast.class);
		Root<DwdForecast> root = query.from(DwdForecast.class);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Predicate dateStartPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get(DwdForecast_.forecastDate), cal.getTime());
		cal.add(Calendar.DATE, daysToForecast);
		cal.set(Calendar.HOUR_OF_DAY, 1);
		Predicate dateEndPredicate = criteriaBuilder.lessThanOrEqualTo(root.get(DwdForecast_.forecastDate), cal.getTime());
		query.where(criteriaBuilder.and(dateStartPredicate, dateEndPredicate));
		query.orderBy(criteriaBuilder.asc(root.get(DwdForecast_.forecastDate)));
		List<DwdForecast> result = em.createQuery(query).getResultList();
		JsonArrayBuilder jsonArray = Json.createArrayBuilder();
		for (DwdForecast forecast : result) {
			JsonObjectBuilder jsonForecast = Json.createObjectBuilder();
			jsonForecast.add("forecastDate", forecast.getForecastDate().getTime());
			jsonForecast.add("temperature", forecast.getTemperature());
			jsonForecast.add("pressure", forecast.getPressure());
			jsonArray.add(jsonForecast);
		}
		return Response.ok(jsonArray.build()).build();
	}

}
