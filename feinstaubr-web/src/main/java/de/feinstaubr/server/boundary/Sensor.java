package de.feinstaubr.server.boundary;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.feinstaubr.server.entity.SensorMeasurement;
import de.feinstaubr.server.entity.SensorMeasurement_;

@Stateless
@Path("sensor")
public class Sensor {
	private static final Logger LOGGER = Logger.getLogger(Sensor.class.getName());
	
	@PersistenceContext
	private EntityManager em;
	
	@Path("/save")
	@POST
	public void save(JsonObject o) {
		SensorMeasurement measurement = new SensorMeasurement();
		if (o.containsKey("date")) {
			// for testing purposes, this is not provided by the sensor
			try {
				measurement.setDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(o.getString("date")));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			measurement.setDate(new Date());
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
	
	@Path("/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSensorData(@PathParam("id") String id) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorMeasurement> query = criteriaBuilder.createQuery(SensorMeasurement.class);
		Root<SensorMeasurement> root = query.from(SensorMeasurement.class);
		query.select(root);
		
		Predicate predicateId = criteriaBuilder.equal(root.get(SensorMeasurement_.sensorId), id);
		query.where(predicateId);
		query.orderBy(criteriaBuilder.desc(root.get(SensorMeasurement_.date)));
		TypedQuery<SensorMeasurement> createQuery = em.createQuery(query);
		return Response.ok(createQuery.getResultList().get(createQuery.getFirstResult())).build();
	}

	
	@Path("/{id}/{type}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSensorData(@PathParam("id") String id, @PathParam("type")String type) {
		LOGGER.fine("reading data for " + id + " type " + type);
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorMeasurement> query = criteriaBuilder.createQuery(SensorMeasurement.class);
		Root<SensorMeasurement> root = query.from(SensorMeasurement.class);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, -24);
		Date nowBefore24h = cal.getTime();
		query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(SensorMeasurement_.sensorId), id), criteriaBuilder.greaterThanOrEqualTo(root.get(SensorMeasurement_.date), nowBefore24h)));
		query.select(root);
		List<SensorMeasurement> resultList = em.createQuery(query).getResultList();
		
		
		JsonArrayBuilder rowBuilder = Json.createArrayBuilder();
		for (SensorMeasurement m : resultList) {
			//https://developers.google.com/chart/interactive/docs/reference#dataparam
			//https://stackoverflow.com/questions/10286204/the-right-json-date-format
			//2012-04-23T18:25:43.511Z
			//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			
			//https://developers.google.com/chart/interactive/docs/dev/implementing_data_source?hl=pt-BR#jsondatatable
			SimpleDateFormat df = new SimpleDateFormat("yyyy,M,d,H,m,s");
			BigDecimal value;
			switch (type) {
			case "temperature":
				value = m.getTemperatur();
				break;
			case "humidity":
				value = m.getHumidity();
				break;
			case "p1":
				value = m.getP1();
				break;
			case "p2":
				value = m.getP2();
				break;
			default:
				throw new RuntimeException("unknown type " + type);
			}
			rowBuilder.add(
					Json.createArrayBuilder()
						.add(m.getDate().getTime())
						.add(value)
					);
		}
		
		String label;
		switch (type) {
		case "temperature":
			label = "Degree Celsius";
			break;
		case "humidity":
			label = "Humidity in Percent";
			break;
		case "p1":
			label = "P2.5 µg/m³";
			break;
		case "p2":
			label = "P10 µg/m³";
			break;
		default:
			throw new RuntimeException("unknown type " + type);
		}

		return Response.ok(rowBuilder.build()).build();
	}
	
}
