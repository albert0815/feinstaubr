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
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
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

import de.feinstaubr.server.entity.SensorMeasurement;
import de.feinstaubr.server.entity.SensorMeasurementType;
import de.feinstaubr.server.entity.SensorMeasurementType_;
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
			measurement.setSensorId(o.getString("esp8266id"));
			
			measurement.setType(measurementType);
			measurement.setValue(new BigDecimal(sensorData.getString("value")));
			LOGGER.fine("saving new measurement " + measurement);
			em.persist(measurement);
		}
//		tracer.endSpan(traceContext);
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

		EntityGraph<SensorMeasurementType> graph = em.createEntityGraph(SensorMeasurementType.class);
		graph.addSubgraph(SensorMeasurementType_.measurements);
		  
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorMeasurementType> query = criteriaBuilder.createQuery(SensorMeasurementType.class);
		Root<SensorMeasurementType> root = query.from(SensorMeasurementType.class);
		query.select(root).orderBy(criteriaBuilder.asc(root.get(SensorMeasurementType_.sortOrder))).distinct(true);
		
		Join<SensorMeasurementType, SensorMeasurement> measurementValuesJoin = root.join(SensorMeasurementType_.measurements, JoinType.LEFT);

		Predicate predicateId = criteriaBuilder.equal(measurementValuesJoin.get(SensorMeasurement_.sensorId), sensorId);
		Predicate predicatePeriod = criteriaBuilder.greaterThan(measurementValuesJoin.get(SensorMeasurement_.date), getPeriodStartDate(period).getTime());
		query.where(criteriaBuilder.and(predicateId, predicatePeriod));
		
//	    Tracer tracer = Trace.getTracer();
//		TraceContext traceContext = tracer.startSpan("db-get-chart-entries");
		long start;
		start = System.currentTimeMillis();
		List<SensorMeasurementType> resultList = em.createQuery(query).setHint("javax.persistence.fetchgraph", graph).getResultList();
		LOGGER.info("duration for db: " + (System.currentTimeMillis() - start));
//		tracer.endSpan(traceContext);
		if (resultList.isEmpty()) {
			return Response.ok().build();
		}

		JsonObjectBuilder chartsJson = Json.createObjectBuilder();
		JsonObjectBuilder detailsJson = Json.createObjectBuilder();

		start = System.currentTimeMillis();
		for (SensorMeasurementType measurementType : resultList) {
			List<SensorMeasurement> measurements = measurementType.getMeasurements();
			int measurementsSize = measurements.size();
			SensorMeasurement last = measurements.get(measurementsSize - 1);
			SensorMeasurement min = last;
			SensorMeasurement max = last;
			BigDecimal sum = last.getValue();
			BigDecimal lastValue = last.getValue();
//			JsonArrayBuilder chartEntries = Json.createArrayBuilder();
//			chartEntries.add(Json.createArrayBuilder().add(last.getDate().getTime()).add(last.getValue()));
			for (int i = measurementsSize - 2; i >= 0; i--) {
				SensorMeasurement sensorMeasurement = measurements.get(i);
				if (min.getValue().compareTo(sensorMeasurement.getValue()) > 0) {
					min = sensorMeasurement;
				}
				if (max.getValue().compareTo(sensorMeasurement.getValue()) < 0) {
					max = sensorMeasurement;
				}
				sum = sum.add(sensorMeasurement.getValue());
				
				//maybe we can do simplify on the fly (always get distance from the line between the last two points)
				if (lastValue.subtract(sensorMeasurement.getValue()).abs().compareTo(measurementType.getMinDiffBetweenTwoValues()) > 0 || i == 0) {
//					chartEntries.add(Json.createArrayBuilder().add(sensorMeasurement.getDate().getTime()).add(sensorMeasurement.getValue()));
					measurements.remove(i);
					lastValue = sensorMeasurement.getValue();
				}
			}
//			chartsJson.add(measurementType.getType(), chartEntries);
			
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
		}
		LOGGER.info("duration for min/max/delete: " + (System.currentTimeMillis() - start));

		Simplify<SensorMeasurement> simplify = new Simplify<>(new SensorMeasurement[0], new PointExtractor<SensorMeasurement>() {
			@Override
			public double getX(SensorMeasurement arg0) {
				return arg0.getDate().getTime() * 10000;//interval between two measures
			}
			@Override
			public double getY(SensorMeasurement arg0) {
				return arg0.getValue().doubleValue() * 10000;//so the square diff between two measures is better
			}
		});
			
		for (SensorMeasurementType type : resultList) {
			start = System.currentTimeMillis();
			SensorMeasurement[] simplified = simplify.simplify(type.getMeasurements().toArray(new SensorMeasurement[0]), type.getEpsilonForSimplify(), false);

			JsonArrayBuilder chartEntries = Json.createArrayBuilder();
			for (SensorMeasurement m : simplified) {
				if (m == null) {
					continue;
				}
				chartEntries.add(Json.createArrayBuilder().add(m.getDate().getTime()).add(m.getValue()));
			}
			chartsJson.add(type.getType(), chartEntries);
			LOGGER.info("duration for simplify for " + type.getType() + ": " + (System.currentTimeMillis() - start));
		}

		List<SensorMeasurement> currentSensorData = getCurrentSensorData(sensorId);
		JsonObjectBuilder currentJson = Json.createObjectBuilder();
		for (SensorMeasurement m : currentSensorData) {
			currentJson.add("date", m.getDate().getTime());
			currentJson.add(m.getType().getType(), m.getValue());
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

	public List<SensorMeasurementType> getTypes() {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorMeasurementType> query = criteriaBuilder.createQuery(SensorMeasurementType.class);
		Root<SensorMeasurementType> root = query.from(SensorMeasurementType.class);
		query.select(root);
		query.orderBy(criteriaBuilder.asc(root.get(SensorMeasurementType_.sortOrder)));
		return em.createQuery(query).getResultList();
	}
	
}
