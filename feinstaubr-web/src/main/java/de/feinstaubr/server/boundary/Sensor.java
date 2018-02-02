package de.feinstaubr.server.boundary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.feinstaubr.server.entity.SensorMeasurement;
import de.feinstaubr.server.entity.SensorMeasurement_;

@Stateless
@Path("sensor")
public class Sensor {
	private static final Logger LOGGER = Logger.getLogger(Sensor.class.getName());
	private static final Map<String, Integer[]> INTERVAL_MAP = new HashMap<>();
	
	static {
		INTERVAL_MAP.put("day", new Integer[] {0, 15 * 60});//period to select: today, interval 15 minutes
		INTERVAL_MAP.put("week", new Integer[] {6 * 24, 60 * 60});//period to select: 1 week, interval 60 minutes
		INTERVAL_MAP.put("month", new Integer[] {28 * 24, 120 * 60});//period to select: 28 days, interval 2 hours
		INTERVAL_MAP.put("year", new Integer[] {365 * 24, 1440 * 60});//period to select: 1 year, interval 24 hours
	}
	
	@PersistenceContext
	private EntityManager em;
	
	@Path("/save")
	@POST
	public void save(JsonObject o) {
		SensorMeasurement measurement = new SensorMeasurement();
		Date now = new Date();
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
		if (o.containsKey("software_version")) {
			measurement.setSoftwareVersion(o.getString("software_version"));
		}
		JsonArray sensorDataValues = o.getJsonArray("sensordatavalues");
		for (int i = 0; i < sensorDataValues.size(); i++) {
			JsonObject sensorData = sensorDataValues.getJsonObject(i);
			switch (sensorData.getString("value_type")) {
			case "SDS_P1":
				measurement.setP1(new BigDecimal(sensorData.getString("value")));
				break;
			case "SDS_P2":
				measurement.setP2(new BigDecimal(sensorData.getString("value")));
				break;
			case "temperature":
				measurement.setTemperatur(new BigDecimal(sensorData.getString("value")));
				break;
			case "humidity":
				measurement.setHumidity(new BigDecimal(sensorData.getString("value")));
				break;
			}
		}
		LOGGER.fine("saving new measurement " + measurement);
		em.persist(measurement);
	}
	
	public SensorMeasurement getCurrentSensorData(String sensorId) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorMeasurement> query = criteriaBuilder.createQuery(SensorMeasurement.class);
		Root<SensorMeasurement> root = query.from(SensorMeasurement.class);
		query.select(root);
		
		Predicate predicateId = criteriaBuilder.equal(root.get(SensorMeasurement_.sensorId), sensorId);
		query.where(predicateId);
		query.orderBy(criteriaBuilder.desc(root.get(SensorMeasurement_.date)));
		TypedQuery<SensorMeasurement> createQuery = em.createQuery(query).setMaxResults(1);
		List<SensorMeasurement> resultList = createQuery.getResultList();
		if (resultList.isEmpty()) {
			return null;
		}
		SensorMeasurement sensorMeasurement = resultList.get(createQuery.getFirstResult());
		return sensorMeasurement;
	}

	
	@Path("{id}/{period}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getChartEntries(@PathParam("id")String id, @PathParam("period")String period) {
		LOGGER.fine("reading data for " + id + " period " + period);
		if (INTERVAL_MAP.get(period) == null) {
			throw new RuntimeException("unknown period");
		}

		Query query = em.createNativeQuery("select to_timestamp(floor(extract('epoch' from cast(date as timestamp with time zone)) / ?1) * ?2), trunc(avg(temperatur),1) as avg_temperature, trunc(avg(humidity),1) as avg_humidity, trunc(avg(p1),1) as avg_p1, trunc(avg(p2),1) as avg_p2 from SensorMeasurement where date >= ?3 group by 1 order by 1");
//		Query query = em.createNativeQuery("select TIMESTAMP WITHout TIME ZONE 'epoch' + INTERVAL '1 second' * round((extract('epoch' from date) / ?1) * ?2), trunc(avg(temperatur),1) as avg_temperature, trunc(avg(humidity),1) as avg_humidity, trunc(avg(p1),1) as avg_p1, trunc(avg(p2),1) as avg_p2 from SensorMeasurement where date >= date_trunc('day', cast(now() as timestamp) - ?3  * interval '1 hour') group by 1 order by 1");
		query.setParameter(1, INTERVAL_MAP.get(period)[1]);
		query.setParameter(2, INTERVAL_MAP.get(period)[1]);
		Calendar periodStartDate = getPeriodStartDate(period);
		query.setParameter(3, periodStartDate);
		List<Object[]> result = query.getResultList();
		
		JsonArrayBuilder temperatureJson = Json.createArrayBuilder();
		JsonArrayBuilder humidityJson = Json.createArrayBuilder();
		JsonArrayBuilder p1Json = Json.createArrayBuilder();
		JsonArrayBuilder p2Json = Json.createArrayBuilder();
		
		for (Object[] o : result) {
			Timestamp timestamp = (Timestamp)o[0];
			
			if (o[1] != null) {
				temperatureJson.add(Json.createArrayBuilder().add(timestamp.getTime() / 100000).add((BigDecimal)o[1]));
			}
			if (o[2] != null) {
				humidityJson.add(Json.createArrayBuilder().add(timestamp.getTime() / 100000).add((BigDecimal)o[2]));
			}
			if (o[3] != null) {
				p1Json.add(Json.createArrayBuilder().add(timestamp.getTime() / 100000).add((BigDecimal)o[3]));
			}
			if (o[4] != null) {
				p2Json.add(Json.createArrayBuilder().add(timestamp.getTime() / 100000).add((BigDecimal)o[4]));
			}
		}
		JsonObjectBuilder response = Json.createObjectBuilder();
		
		response.add("charts", Json.createObjectBuilder()
					.add("temperature", temperatureJson)
					.add("humidity", humidityJson)
					.add("p1", p1Json)
					.add("p2", p2Json));
		
		JsonObjectBuilder detailJson = Json.createObjectBuilder();
		getMinMaxSensorData(id, period, detailJson);
		response.add("details", detailJson);
		JsonObjectBuilder current = fromMeasureToJson(getCurrentSensorData(id));
		if (current != null) {
			response.add("current", current);
		}
		return response.build();
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
	
	private Object[] getAvg(String id, String period) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);
		Root<SensorMeasurement> root = query.from(SensorMeasurement.class);
		
		Predicate predicateId = criteriaBuilder.equal(root.get(SensorMeasurement_.sensorId), id);
		Predicate predicatePeriod = criteriaBuilder.greaterThan(root.get(SensorMeasurement_.date), getPeriodStartDate(period).getTime());
		query.where(criteriaBuilder.and(predicateId, predicatePeriod));
		query.multiselect(
				criteriaBuilder.avg(root.get(SensorMeasurement_.temperatur)), 
				criteriaBuilder.avg(root.get(SensorMeasurement_.humidity)), 
				criteriaBuilder.avg(root.get(SensorMeasurement_.p1)), 
				criteriaBuilder.avg(root.get(SensorMeasurement_.p2))
				);
		TypedQuery<Object[]> createQuery = em.createQuery(query);
		return createQuery.getSingleResult();
	}

	private void getMinMaxSensorData(String id, String period, JsonObjectBuilder detailJson) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorMeasurement> query = criteriaBuilder.createQuery(SensorMeasurement.class);
		Root<SensorMeasurement> root = query.from(SensorMeasurement.class);
		
		Predicate predicateId = criteriaBuilder.equal(root.get(SensorMeasurement_.sensorId), id);
		Predicate predicatePeriod = criteriaBuilder.greaterThan(root.get(SensorMeasurement_.date), getPeriodStartDate(period).getTime());
		query.where(criteriaBuilder.and(predicateId, predicatePeriod));

		
		Object[] avg = getAvg(id, period);
		
		JsonObjectBuilder detailsTemp = Json.createObjectBuilder();
		addIfNotNull(detailsTemp, "min", getMinMax(query, criteriaBuilder.desc(root.get(SensorMeasurement_.temperatur))));
		addIfNotNull(detailsTemp, "max", getMinMax(query, criteriaBuilder.asc(root.get(SensorMeasurement_.temperatur))));
		if (avg[0] != null) {
			detailsTemp.add("avg", Json.createObjectBuilder().add("value", new BigDecimal((Double)avg[0]).setScale(1, RoundingMode.HALF_UP)));
		}
		detailJson.add("temperature", detailsTemp);

		JsonObjectBuilder detailsHum = Json.createObjectBuilder();
		addIfNotNull(detailsHum, "min", getMinMax(query, criteriaBuilder.desc(root.get(SensorMeasurement_.humidity))));
		addIfNotNull(detailsHum, "max", getMinMax(query, criteriaBuilder.asc(root.get(SensorMeasurement_.humidity))));
		if (avg[1] != null) {
			detailsHum.add("avg", Json.createObjectBuilder().add("value", new BigDecimal((Double)avg[1]).setScale(1, RoundingMode.HALF_UP)));
		}
		detailJson.add("humidity", detailsHum);

		JsonObjectBuilder detailsP1 = Json.createObjectBuilder();
		addIfNotNull(detailsP1, "min", getMinMax(query, criteriaBuilder.desc(root.get(SensorMeasurement_.p1))));
		addIfNotNull(detailsP1, "max", getMinMax(query, criteriaBuilder.asc(root.get(SensorMeasurement_.p1))));
		if (avg[2] != null) {
			detailsP1.add("avg", Json.createObjectBuilder().add("value", new BigDecimal((Double)avg[2]).setScale(1, RoundingMode.HALF_UP)));
		}
		detailJson.add("p1", detailsP1);

		JsonObjectBuilder detailsP2 = Json.createObjectBuilder();
		addIfNotNull(detailsP2, "min", getMinMax(query, criteriaBuilder.desc(root.get(SensorMeasurement_.p2))));
		addIfNotNull(detailsP2, "max", getMinMax(query, criteriaBuilder.asc(root.get(SensorMeasurement_.p2))));
		if (avg[3] != null) {
			detailsP2.add("avg", Json.createObjectBuilder().add("value", new BigDecimal((Double)avg[3]).setScale(1, RoundingMode.HALF_UP)));
		}
		detailJson.add("p2", detailsP2);

	}

	private JsonObjectBuilder getMinMax(CriteriaQuery<SensorMeasurement> query, Order sort) {
		query.orderBy(sort);
		TypedQuery<SensorMeasurement> createQuery = em.createQuery(query);
		createQuery.setMaxResults(1);
		List<SensorMeasurement> resultList = createQuery.getResultList();

		
		if (!resultList.isEmpty()) {
			SensorMeasurement sensorMeasurement = resultList.get(createQuery.getFirstResult());
			return Json.createObjectBuilder().add("date", sensorMeasurement.getDate().getTime()).add("value", sensorMeasurement.getTemperatur());
		} else {
			return null;
		}

	}

	private void addIfNotNull(JsonObjectBuilder detailsTemp, String string, JsonObjectBuilder o) {
		if (o != null) {
			detailsTemp.add(string, o);
		}
	}
	
	private JsonObjectBuilder fromMeasureToJson(SensorMeasurement currentSensorData) {
		if (currentSensorData == null) {
			return null;
		}
		JsonObjectBuilder result = Json.createObjectBuilder();
		if (currentSensorData.getDate() != null) {
			result.add("date", currentSensorData.getDate().getTime());
		}
		if (currentSensorData.getSensorId() != null) {
			result.add("sensorId", currentSensorData.getSensorId());
		}
		if (currentSensorData.getSoftwareVersion() != null) {
			result.add("softwareVersion", currentSensorData.getSoftwareVersion());
		}
		if (currentSensorData.getTemperatur() != null) {
			result.add("temperature", currentSensorData.getTemperatur());
		}
		if (currentSensorData.getHumidity() != null) {
			result.add("humidity", currentSensorData.getHumidity());
		}
		if (currentSensorData.getP1() != null) {
			result.add("p1", currentSensorData.getP1());
		}
		if (currentSensorData.getP2() != null) {
			result.add("p2", currentSensorData.getP2());
		}
		return result;
	}
	
}
