package de.feinstaubr.server.boundary;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import de.feinstaubr.server.control.ConfigurationController;
import de.feinstaubr.server.entity.MvgDeparture;
import de.feinstaubr.server.entity.MvgProduct;
import de.feinstaubr.server.entity.MvgStation;

@Stateless
@Path("mvg")
public class MvgApi {
	private static final Logger LOGGER = Logger.getLogger(MvgApi.class.getName());

	@PersistenceContext
	private EntityManager em;
	
	@Inject
	private ConfigurationController configurationController;
	
	@GET
	@Path("{latitude}/{longitude}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<MvgStation> getStations(@PathParam("latitude") Double latitude, @PathParam("longitude") Double longitude) {
		List<MvgStation> stations = getStations();
		Collections.sort(stations, new Comparator<MvgStation>() {

			@Override
			public int compare(MvgStation o1, MvgStation o2) {
				return (int) (o1.getDistanceTo(latitude, longitude) * 1000 - o2.getDistanceTo(latitude, longitude) * 1000); 
			}
		});
		return stations;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<MvgStation> getStations() {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<MvgStation> query = criteriaBuilder.createQuery(MvgStation.class);
		Root<MvgStation> root = query.from(MvgStation.class);
		query.select(root);
		List<MvgStation> stations = em.createQuery(query).getResultList();
		Client client = ClientBuilder.newClient();
		
		for (MvgStation station : stations) {
			WebTarget target = client.target("https://www.mvg.de/fahrinfo/api/departure/" + station.getStationId());
			String footway = configurationController.getConfiguration("mvg", "footway", "10");
			if (station.getFootway() != null) {
				footway = station.getFootway().toString();
			}
			JsonObject o = target.queryParam("footway", footway).request(MediaType.APPLICATION_JSON_TYPE).header("X-MVG-Authorization-Key", "5af1beca494712ed38d313714d4caff6").get(JsonObject.class);
			JsonArray mvgDepartures = (JsonArray) o.get("departures");
			if (mvgDepartures.isEmpty()) {
				station.setDepartures(Collections.emptyList());
			} else {
				List<MvgDeparture> liveDepartureList = new ArrayList<>();
				Pattern destinationPattern = Pattern.compile(station.getDestinationFilter());
				Calendar cal = Calendar.getInstance();
				int departureInterval = configurationController.getConfigurationInt("mvg", "departureInterval", 20);
				Integer maxDepartures = configurationController.getConfigurationInt("mvg", "maxDepartures");
				cal.add(Calendar.MINUTE, departureInterval);
				Date nowPlusHalfHour = cal.getTime();
				for (JsonValue jsonValue : mvgDepartures) {
					if (jsonValue instanceof JsonObject) {
						JsonObject jsonDeparture = (JsonObject) jsonValue;
						String destination = jsonDeparture.getString("destination");
						if (!destinationPattern.matcher(destination).matches()) {
							continue;
						}
						Date departureTime = new Date(jsonDeparture.getJsonNumber("departureTime").longValue());
						if (departureTime.after(nowPlusHalfHour)) {
							continue;
						}
						MvgDeparture departure = new MvgDeparture();
						departure.setDepartureTime(departureTime);
						departure.setDestination(destination);
						departure.setLine(jsonDeparture.getString("label"));
						departure.setProduct(MvgProduct.getEnum(jsonDeparture.getString("product")));
						liveDepartureList.add(departure);
						if (liveDepartureList.size() >= maxDepartures) {
							break;
						}
					} else {
						LOGGER.severe("unexpected format of MVG api, received: " + jsonValue);
					}
				}
				station.setDepartures(liveDepartureList);
			}
		}
		return stations;
	}
}
