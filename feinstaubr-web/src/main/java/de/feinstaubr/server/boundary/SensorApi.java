package de.feinstaubr.server.boundary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
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

import de.feinstaubr.server.entity.Sensor;
import de.feinstaubr.server.entity.SensorMeasurement;
import de.feinstaubr.server.entity.SensorMeasurementType;
import de.feinstaubr.server.entity.SensorMeasurementType_;
import de.feinstaubr.server.entity.SensorMeasurement_;
import de.feinstaubr.server.entity.Sensor_;
import de.feinstaubr.server.entity.Trend;

@Stateless
@Path("sensor")
public class SensorApi {
	private static final Logger LOGGER = Logger.getLogger(SensorApi.class.getName());
	private static final Map<String, Integer[]> INTERVAL_MAP = new HashMap<>();
	
	static {
		INTERVAL_MAP.put("day", new Integer[] {0, 15 * 60, 1000});//period to select: today, interval 15 minutes
		INTERVAL_MAP.put("week", new Integer[] {6, 60 * 60, 10000});//period to select: 1 week, interval 60 minutes
		INTERVAL_MAP.put("month", new Integer[] {28, 120 * 60, 100000});//period to select: 28 days, interval 2 hours
		INTERVAL_MAP.put("year", new Integer[] {365, 1440 * 60, 100000});//period to select: 1 year, interval 24 hours
	}
	
	@PersistenceContext
	private EntityManager em;
	
	@Inject
	private UserApi user;
	
	@Path("/save")
	@POST
	public void save(JsonObject o) {
		
		if (!o.containsKey("esp8266id") || !o.containsKey("sensordatavalues")) {
			return;
		}
		
		Sensor sensor = em.find(Sensor.class, o.getString("esp8266id"));
		if (sensor == null) {
			return;
		}

		
//	    Tracer tracer = Trace.getTracer();
//		TraceContext traceContext = tracer.startSpan("save");
		
		
		Date now = new Date();
		JsonArray sensorDataValues = o.getJsonArray("sensordatavalues");
		for (int i = 0; i < sensorDataValues.size(); i++) {
			JsonObject sensorData = sensorDataValues.getJsonObject(i);
			SensorMeasurementType measurementType = em.find(SensorMeasurementType.class, sensorData.getString("value_type"));
			if (measurementType == null) {
				continue;
			}
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
			measurement.setSensorId(sensor);
			
			measurement.setType(measurementType);
			measurement.setValue(new BigDecimal(sensorData.getString("value")));
			if ("co2".equals(measurementType.getType())) {
				if (measurement.getValue().intValue() == 0) {
					LOGGER.warning("ignoring value 0 as this is clearly wrong");
					return;
				}
				int minimum;
				try {
					minimum = getMinimumCo2ValueOfLast7Days(sensor.getSensorId());
					if (minimum > measurement.getValue().intValue()) {
						minimum = measurement.getValue().intValue();
					}
				} catch (NoResultException e) {
					minimum = measurement.getValue().intValue();
				}
				double ro = Mq135Calculator.getRo(minimum);
				int ppm = Mq135Calculator.getPpm(measurement.getValue().intValue(), ro);
				if (ppm == -1) {
					LOGGER.warning("ignoring value " + sensorData.getString("value") + " as the corresponding ppm cannot be calculated due to invalid ro... ");
					return;
				}
				BigDecimal calculatedPpm = new BigDecimal(ppm);
				measurement.setCalculatedValue(calculatedPpm);
			} else if ("pressure".equals(measurementType.getType())) {
//				Luftdruck auf Meereshöhe = Barometeranzeige / (1-Temperaturgradient*Höhe/Temperatur + Temperaturgradient * Höhe in Kelvin)^(0,03416/Temperaturgradient)
				BigDecimal temperatureMeasurement = getLatestOutsideTemperature(sensor);
				measurement.setCalculatedValue(PressureCalculator.calculatePressure(measurement.getValue(), temperatureMeasurement, 545));//FIXME sensor.getHeight()
			}
			LOGGER.info("saving new measurement " + measurement);
			em.persist(measurement);
		}
//		tracer.endSpan(traceContext);
	}
	
	private BigDecimal getLatestOutsideTemperature(Sensor sensor) {
		String sensorIdOutsideSensor = "7620363";//something like sensor.getLocation().getMainOutsideSensor()
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorMeasurement> query = criteriaBuilder.createQuery(SensorMeasurement.class);
		Root<SensorMeasurement> root = query.from(SensorMeasurement.class);
		query.select(root);
		Predicate predicateId = criteriaBuilder.equal(root.get(SensorMeasurement_.sensorId).get(Sensor_.sensorId), sensorIdOutsideSensor);
		Predicate predicateType = criteriaBuilder.equal(root.get(SensorMeasurement_.type).get(SensorMeasurementType_.type), "temperature");
		query.where(criteriaBuilder.and(predicateId, predicateType)).orderBy(criteriaBuilder.desc(root.get(SensorMeasurement_.date)));
		return em.createQuery(query).setMaxResults(1).getSingleResult().getValue();
	}

	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCurrentSensorDataJson(@PathParam("id") String sensorId) {
		List<SensorMeasurement> list = getCurrentSensorData(sensorId);
		JsonObjectBuilder result = Json.createObjectBuilder();
		for (SensorMeasurement m : list) {
			BigDecimal value = m.getValue().setScale(1, RoundingMode.HALF_UP);
			result.add(m.getType().getType(), value);
		}
		SimpleDateFormat format = new SimpleDateFormat("dd.MM. HH:mm");
		if (!list.isEmpty()) {
			result.add("date", format.format(list.get(0).getDate()));
		}
		return Response.ok(result.build()).build();
	}
	
	
	private int getMinimumCo2ValueOfLast7Days(String sensorId) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Number> query = criteriaBuilder.createQuery(Number.class);
		Root<SensorMeasurement> root = query.from(SensorMeasurement.class);
		query.select(criteriaBuilder.min(root.get(SensorMeasurement_.value)));
		Predicate predicateId = criteriaBuilder.equal(root.get(SensorMeasurement_.sensorId).get(Sensor_.sensorId), sensorId);
		Predicate predicateType = criteriaBuilder.equal(root.get(SensorMeasurement_.type).get(SensorMeasurementType_.type), "co2");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		Predicate predicateLastWeek = criteriaBuilder.greaterThan(root.get(SensorMeasurement_.date), cal.getTime());
		query.where(criteriaBuilder.and(predicateId, predicateLastWeek, predicateType));
		Number singleResult = em.createQuery(query).getSingleResult();
		if (singleResult == null) {
			throw new NoResultException();
		}
		return singleResult.intValue();
	}

	public List<SensorMeasurement> getCurrentSensorData(String sensorId) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorMeasurement> query = criteriaBuilder.createQuery(SensorMeasurement.class);
		Root<SensorMeasurement> root = query.from(SensorMeasurement.class);
		query.select(root);
		

		Subquery<Date> sq = query.subquery(Date.class);
		Root<SensorMeasurement> subQueryRoot = sq.from(SensorMeasurement.class);
		sq.select(criteriaBuilder.greatest(subQueryRoot.get(SensorMeasurement_.date)));
		sq.where(criteriaBuilder.equal(subQueryRoot.get(SensorMeasurement_.sensorId).get(Sensor_.sensorId), sensorId));

		Predicate predicateId = criteriaBuilder.equal(root.get(SensorMeasurement_.sensorId).get(Sensor_.sensorId), sensorId);
		Predicate predicateDate = criteriaBuilder.equal(root.get(SensorMeasurement_.date), sq);
		
		query.where(criteriaBuilder.and(predicateId, predicateDate)).orderBy(criteriaBuilder.asc(root.get(SensorMeasurement_.type).get(SensorMeasurementType_.sortOrder)));

		TypedQuery<SensorMeasurement> createQuery = em.createQuery(query);
		List<SensorMeasurement> resultList = createQuery.getResultList();
		
		for (SensorMeasurement m : resultList) {
			calculateTrend(m);
		}
		
		return resultList;
	}

	
	private void calculateTrend(SensorMeasurement m) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Number> query = criteriaBuilder.createQuery(Number.class);
		Root<SensorMeasurement> root = query.from(SensorMeasurement.class);
		query.select(criteriaBuilder.avg(root.get(SensorMeasurement_.value)));
		Predicate predicateId = criteriaBuilder.equal(root.get(SensorMeasurement_.sensorId), m.getSensorId());
		Predicate predicateType = criteriaBuilder.equal(root.get(SensorMeasurement_.type), m.getType());
		Calendar cal = Calendar.getInstance();
		cal.setTime(m.getDate());
		cal.add(Calendar.MINUTE, -90);
		Predicate predicateLastHalfHour = criteriaBuilder.greaterThan(root.get(SensorMeasurement_.date), cal.getTime());
		query.where(criteriaBuilder.and(predicateId, predicateLastHalfHour, predicateType));
		Number avgLastHalfHour = em.createQuery(query).getSingleResult();

		Date end = cal.getTime();
		cal.add(Calendar.MINUTE, -90);
		Date start = cal.getTime();
		
		Predicate predicateAfterLastHour = criteriaBuilder.greaterThan(root.get(SensorMeasurement_.date), start);
		Predicate predicateBeforeLastHalfHour = criteriaBuilder.lessThan(root.get(SensorMeasurement_.date), end);
		query.where(criteriaBuilder.and(predicateId, predicateAfterLastHour, predicateBeforeLastHalfHour, predicateType));
		Number avgHalfHourBefore = em.createQuery(query).getSingleResult();
		if (avgHalfHourBefore == null) {
			m.setTrend(Trend.FLAT);
		} else {
			double diff = avgLastHalfHour.doubleValue() - avgHalfHourBefore.doubleValue();
			if (Math.abs(diff) < m.getType().getMinDiffBetweenTwoValues().doubleValue()) {
				m.setTrend(Trend.FLAT);
			} else {
				if (diff > 0) {
					m.setTrend(Trend.UP);
				} else {
					m.setTrend(Trend.DOWN);
				}
			}
		}
	}

	@Path("{id}/{period}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChartEntries(@PathParam("id")String sensorId, @PathParam("period")String period) {
		LOGGER.fine("reading data for " + sensorId + " period " + period);
		if (INTERVAL_MAP.get(period) == null) {
			throw new RuntimeException("unknown period");
		}

		EntityGraph<SensorMeasurementType> graph = em.createEntityGraph(SensorMeasurementType.class);
		graph.addSubgraph(SensorMeasurementType_.measurements);
		  
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorMeasurementType> query = criteriaBuilder.createQuery(SensorMeasurementType.class);
		Root<SensorMeasurementType> root = query.from(SensorMeasurementType.class);
		query.select(root).orderBy(criteriaBuilder.asc(root.get(SensorMeasurementType_.sortOrder))).distinct(true);
		
		Join<SensorMeasurementType, SensorMeasurement> measurementValuesJoin = root.join(SensorMeasurementType_.measurements, JoinType.LEFT);

		Predicate predicateId = criteriaBuilder.equal(measurementValuesJoin.get(SensorMeasurement_.sensorId).get(Sensor_.sensorId), sensorId);
		Predicate predicatePeriod = criteriaBuilder.greaterThan(measurementValuesJoin.get(SensorMeasurement_.date), getPeriodStartDate(period).getTime());
		query.where(criteriaBuilder.and(predicateId, predicatePeriod));
		
//	    Tracer tracer = Trace.getTracer();
//		TraceContext traceContext = tracer.startSpan("db-get-chart-entries");
		long start;
		start = System.currentTimeMillis();
		List<SensorMeasurementType> resultList = em.createQuery(query).setHint("javax.persistence.fetchgraph", graph).getResultList();
		LOGGER.finest("duration for db: " + (System.currentTimeMillis() - start));
//		tracer.endSpan(traceContext);
		if (resultList.isEmpty()) {
			return Response.ok().build();
		}

		JsonObjectBuilder chartsJson = Json.createObjectBuilder();
		JsonObjectBuilder detailsJson = Json.createObjectBuilder();

		for (SensorMeasurementType measurementType : resultList) {
			start = System.currentTimeMillis();
			int sizeBefore = measurementType.getMeasurements().size();
			List<SensorMeasurement> measurements = measurementType.getMeasurements();
			int measurementsSize = measurements.size();
			SensorMeasurement last = measurements.get(measurementsSize - 1);
			SensorMeasurement min = last;
			SensorMeasurement max = last;
			BigDecimal sum = last.getValue();
			BigDecimal lastValue = last.getValue();
			for (int i = measurementsSize - 2; i >= 0; i--) {
				SensorMeasurement sensorMeasurement = measurements.get(i);
				if (min.getValue().compareTo(sensorMeasurement.getValue()) > 0) {
					min = sensorMeasurement;
				}
				if (max.getValue().compareTo(sensorMeasurement.getValue()) < 0) {
					max = sensorMeasurement;
				}
				sum = sum.add(sensorMeasurement.getValue());
				
				//maybe this is not needed..
				if (lastValue.subtract(sensorMeasurement.getValue()).abs().compareTo(measurementType.getMinDiffBetweenTwoValues()) <= 0 && i > 0) {
					measurements.remove(i);
				} else {
					lastValue = sensorMeasurement.getValue();
				}
			}
			
			detailsJson.add(measurementType.getType(), Json.createObjectBuilder()
					.add("min", Json.createObjectBuilder()
							.add("value", min.getValue())
							.add("date", min.getDate().getTime())
						)
					.add("max", Json.createObjectBuilder()
							.add("value", max.getValue())
							.add("date", max.getDate().getTime())
						)
					.add("avg", Json.createObjectBuilder()
							.add("value", sum.divide(new BigDecimal(measurementsSize), RoundingMode.HALF_UP))
						)
					);
			LOGGER.finest("duration for min/max/delete for " + measurementType.getType() + " : " + (System.currentTimeMillis() - start) + " - reduced from " + sizeBefore + " -> " + measurementType.getMeasurements().size());
		}

		Simplify<SensorMeasurement> simplify = new Simplify<>(new SensorMeasurement[0], new PointExtractor<SensorMeasurement>() {
			@Override
			public double getX(SensorMeasurement arg0) {
				return arg0.getDate().getTime() * 10000;//interval between two measures
			}
			@Override
			public double getY(SensorMeasurement arg0) {
				return arg0.getValue().doubleValue() * 100;//so the square diff between two measures is better
			}
		});
			
		for (SensorMeasurementType type : resultList) {
			start = System.currentTimeMillis();
			SensorMeasurement[] simplified = simplify.simplify(type.getMeasurements().toArray(new SensorMeasurement[0]), type.getEpsilonForSimplify() * INTERVAL_MAP.get(period)[0], false);

			JsonArrayBuilder chartEntries = Json.createArrayBuilder();
			for (SensorMeasurement m : simplified) {
				if (m == null) {
					continue;
				}
				chartEntries.add(Json.createArrayBuilder().add(m.getDate().getTime()).add(m.getValue()));
			}
			chartsJson.add(type.getType(), chartEntries);
			LOGGER.finest("duration for simplify for " + type.getType() + ": " + (System.currentTimeMillis() - start) + " - reduced from " + type.getMeasurements().size() + " -> " + simplified.length);
		}

		List<SensorMeasurement> currentSensorData = getCurrentSensorData(sensorId);
		JsonObjectBuilder currentJson = Json.createObjectBuilder();
		if (currentSensorData.size() > 0) {
			JsonObjectBuilder currentValuesJson = Json.createObjectBuilder();
			for (SensorMeasurement m : currentSensorData) {
				currentValuesJson.add(m.getType().getType(), m.getValue());
			}
			currentJson.add("date", currentSensorData.get(0).getDate().getTime());
			currentJson.add("values", currentValuesJson);
		}

		JsonObjectBuilder resultJson = Json.createObjectBuilder()
				.add("charts", chartsJson)
				.add("current", currentJson)
				.add("details", detailsJson);
		
		return Response.ok(resultJson.build()).build();
	
	}


	private Calendar getPeriodStartDate(String period) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -INTERVAL_MAP.get(period)[0]);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}

	public List<SensorMeasurementType> getTypes() {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorMeasurementType> query = criteriaBuilder.createQuery(SensorMeasurementType.class);
		Root<SensorMeasurementType> root = query.from(SensorMeasurementType.class);
		query.select(root);
		query.orderBy(criteriaBuilder.asc(root.get(SensorMeasurementType_.sortOrder)));
		return em.createQuery(query).getResultList();
	}

	public List<Sensor> getSensors() {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Sensor> query = criteriaBuilder.createQuery(Sensor.class);
		Root<Sensor> root = query.from(Sensor.class);
		query.select(root);
		query.orderBy(criteriaBuilder.asc(root.get(Sensor_.name)));
		return em.createQuery(query).getResultList();
	}
	
}
