package de.feinstaubr.server.boundary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.goebl.simplify.PointExtractor;
import com.goebl.simplify.Simplify;

import de.feinstaubr.server.entity.SensorMeasurement;
import de.feinstaubr.server.entity.SensorMeasurement_;

@Stateless
@Path("sensor")
public class Sensor {
	private static final Logger LOGGER = Logger.getLogger(Sensor.class.getName());
	private static final Map<String, Integer[]> INTERVAL_MAP = new HashMap<>();
	
	static {
		INTERVAL_MAP.put("day", new Integer[] {0, 15 * 60, 1000});//period to select: today, interval 15 minutes
		INTERVAL_MAP.put("week", new Integer[] {6 * 24, 60 * 60, 10000});//period to select: 1 week, interval 60 minutes
		INTERVAL_MAP.put("month", new Integer[] {28 * 24, 120 * 60, 100000});//period to select: 28 days, interval 2 hours
		INTERVAL_MAP.put("year", new Integer[] {365 * 24, 1440 * 60, 100000});//period to select: 1 year, interval 24 hours
	}
	
	@PersistenceContext
	private EntityManager em;
	
	@Path("/save")
	@POST
	public void save(JsonObject o) {
		Date now = new Date();
		JsonArray sensorDataValues = o.getJsonArray("sensordatavalues");
		for (int i = 0; i < sensorDataValues.size(); i++) {
			SensorMeasurement measurement = new SensorMeasurement();
			if (o.containsKey("date")) {
				// for testing purposes, this is not provided by the sensor
				try {
					Date parsed = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(o.getString("date"));
					measurement.setDate(parsed);
				} catch (ParseException e) {
					measurement.setDate(now);
				}
			} else {
				measurement.setDate(now);
			}
			measurement.setSensorId(o.getString("esp8266id"));
			JsonObject sensorData = sensorDataValues.getJsonObject(i);
			measurement.setType(sensorData.getString("value_type"));
			measurement.setValue(new BigDecimal(sensorData.getString("value")));
			LOGGER.fine("saving new measurement " + measurement);
			em.persist(measurement);
		}
	}
	
	public List<SensorMeasurement> getCurrentSensorData(String sensorId) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorMeasurement> query = criteriaBuilder.createQuery(SensorMeasurement.class);
		Root<SensorMeasurement> root = query.from(SensorMeasurement.class);
		query.select(root);
		

		Subquery<Date> sq = query.subquery(Date.class);
		Root<SensorMeasurement> subQueryRoot = sq.from(SensorMeasurement.class);
		sq.select(criteriaBuilder.greatest(subQueryRoot.get(SensorMeasurement_.date)));
		sq.where(criteriaBuilder.equal(subQueryRoot.get(SensorMeasurement_.sensorId), sensorId));

		Predicate predicateId = criteriaBuilder.equal(root.get(SensorMeasurement_.sensorId), sensorId);
		Predicate predicateDate = criteriaBuilder.equal(root.get(SensorMeasurement_.date), sq);
		
		query.where(criteriaBuilder.and(predicateId, predicateDate));

		TypedQuery<SensorMeasurement> createQuery = em.createQuery(query);
		List<SensorMeasurement> resultList = createQuery.getResultList();
		return resultList;
	}

	
	@Path("{id}/{period}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChartEntries(@PathParam("id")String sensorId, @PathParam("period")String period) {
		LOGGER.fine("reading data for " + sensorId + " period " + period);
		if (INTERVAL_MAP.get(period) == null) {
			throw new RuntimeException("unknown period");
		}

		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorMeasurement> query = criteriaBuilder.createQuery(SensorMeasurement.class);
		Root<SensorMeasurement> root = query.from(SensorMeasurement.class);
		query.select(root);
		
		Predicate predicateId = criteriaBuilder.equal(root.get(SensorMeasurement_.sensorId), sensorId);
		Predicate predicatePeriod = criteriaBuilder.greaterThan(root.get(SensorMeasurement_.date), getPeriodStartDate(period).getTime());
		query.where(criteriaBuilder.and(predicateId, predicatePeriod));
		query.orderBy(criteriaBuilder.asc(root.get(SensorMeasurement_.date)));
		
		List<SensorMeasurement> resultList = em.createQuery(query).getResultList();
		if (resultList.isEmpty()) {
			return Response.ok().build();
		}
		Map<String, SensorMeasurement> mins = new HashMap<>();
		Map<String, SensorMeasurement> maxs = new HashMap<>();
		Map<String, BigDecimal> sums = new HashMap<>();
		Map<String, BigDecimal> counts = new HashMap<>();
		Map<String, List<SensorMeasurement>> measuresPerType = new HashMap<>();
		
		for (SensorMeasurement m : resultList) {
			List<SensorMeasurement> measuresList = measuresPerType.get(m.getType());
			if (measuresList == null) {
				measuresList = new ArrayList<>();
				measuresPerType.put(m.getType(), measuresList);
			}
			measuresList.add(m);
			
			SensorMeasurement currentMin = mins.get(m.getType());
			if (currentMin == null || currentMin.getValue().compareTo(m.getValue()) > 0) {
				mins.put(m.getType(), m);
			}
			SensorMeasurement currentMax = maxs.get(m.getType());
			if (currentMax == null || currentMax.getValue().compareTo(m.getValue()) < 0) {
				maxs.put(m.getType(), m);
			}
			BigDecimal currentSum = sums.get(m.getType());
			if (currentSum == null) {
				sums.put(m.getType(), m.getValue());
			} else {
				sums.put(m.getType(), m.getValue().add(currentSum));
			}
			BigDecimal currentCount = counts.get(m.getType());
			if (currentCount == null) {
				currentCount = BigDecimal.ONE;
				counts.put(m.getType(), currentCount);
			} else {
				counts.put(m.getType(), currentCount.add(BigDecimal.ONE));
			}
		}
		
		JsonObjectBuilder detailsJson = Json.createObjectBuilder();
		
		JsonObjectBuilder minsJson = Json.createObjectBuilder();
		for (String type : mins.keySet()) {
			minsJson.add(type, Json.createObjectBuilder()
						.add("date", mins.get(type).getDate().getTime())
						.add("value", mins.get(type).getValue())
				);
		}
		detailsJson.add("mins", minsJson);

		JsonObjectBuilder maxsJson = Json.createObjectBuilder();
		for (String type : maxs.keySet()) {
			maxsJson.add(type, Json.createObjectBuilder()
						.add("date", maxs.get(type).getDate().getTime())
						.add("value", maxs.get(type).getValue())
				);
		}
		detailsJson.add("maxs", maxsJson);
		
		JsonObjectBuilder avgJson = Json.createObjectBuilder();
		for (String type : sums.keySet()) {
			avgJson.add(type, Json.createObjectBuilder()
						.add("value", sums.get(type).divide(counts.get(type), 1, RoundingMode.HALF_UP))
				);
		}
		detailsJson.add("avg", avgJson);
		
		Simplify<SensorMeasurement> simplify = new Simplify<>(new SensorMeasurement[0], new PointExtractor<SensorMeasurement>() {
			@Override
			public double getX(SensorMeasurement arg0) {
				return arg0.getDate().getTime() * 10000;
			}
			@Override
			public double getY(SensorMeasurement arg0) {
				return arg0.getValue().doubleValue() * 10000;
			}
		});
		
		JsonObjectBuilder chartsJson = Json.createObjectBuilder();
		for (String type : measuresPerType.keySet()) {
			SensorMeasurement[] simplified = simplify.simplify(measuresPerType.get(type).toArray(new SensorMeasurement[0]), INTERVAL_MAP.get(period)[2], false);

			JsonArrayBuilder chartEntries = Json.createArrayBuilder();
			for (SensorMeasurement m : simplified) {
				if (m == null) {
					continue;
				}
				chartEntries.add(Json.createArrayBuilder().add(m.getDate().getTime()).add(m.getValue()));
			}
			chartsJson.add(type, chartEntries);
		}
		
		List<SensorMeasurement> currentSensorData = getCurrentSensorData(sensorId);
		JsonObjectBuilder currentJson = Json.createObjectBuilder();
		for (SensorMeasurement m : currentSensorData) {
			currentJson.add("date", m.getDate().getTime());
			currentJson.add(m.getType(), m.getValue());
		}

		JsonObjectBuilder resultJson = Json.createObjectBuilder()
				.add("charts", chartsJson)
				.add("current", currentJson)
				.add("details", detailsJson);
		
		return Response.ok(resultJson.build()).build();

	
	}


	private Calendar getPeriodStartDate(String period) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.HOUR_OF_DAY, -INTERVAL_MAP.get(period)[0]);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}
	
}
