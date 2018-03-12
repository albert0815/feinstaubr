package de.feinstaubr.server.control;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.feinstaubr.server.entity.SensorLocation;
import de.feinstaubr.server.entity.WeatherForecast;
import de.feinstaubr.server.entity.WeatherForecast_;

@Singleton
@Startup
public class ForecastDataReadScheduler {
	private static final Logger LOGGER = Logger.getLogger(ForecastDataReadScheduler.class.getName());
	
	@PersistenceContext
	private EntityManager em;
	
	@Inject
	private DwdController dwd;
	
	@Inject
	private OpenWeatherController openWeather;

	@PostConstruct
	public void init() {
		updateDwdData();
	}
	
	@Schedule(hour="*", persistent=false)
	public void updateDwdData() {
		LOGGER.info("updating forecast data");
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorLocation> sensorLocationQuery = criteriaBuilder.createQuery(SensorLocation.class);
		Root<SensorLocation> sensorLocationRoot = sensorLocationQuery.from(SensorLocation.class);
		sensorLocationQuery.select(sensorLocationRoot);
		List<SensorLocation> locations = em.createQuery(sensorLocationQuery).getResultList();
		for (SensorLocation loc : locations) {
			List<WeatherForecast> forecastsDwd = dwd.getForecasts(loc.getDwdPoiId());
			updateForecastsInDb(loc, forecastsDwd);
			
			List<WeatherForecast> forecastsOpen = openWeather.getForecast(loc.getOpenWeatherId());
			updateForecastsInDb(loc, forecastsOpen);
		}
	}

	private void updateForecastsInDb(SensorLocation loc, List<WeatherForecast> forecasts) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		int update = 0;
		int insert = 0;
		for (WeatherForecast forecast : forecasts) {
			forecast.setLocation(loc);
			CriteriaQuery<WeatherForecast> forecastQuery = criteriaBuilder.createQuery(WeatherForecast.class);
			Root<WeatherForecast> forecastRoot = forecastQuery.from(WeatherForecast.class);
			Predicate forecastDatePredicate = criteriaBuilder.equal(forecastRoot.get(WeatherForecast_.forecastDate), forecast.getForecastDate());
			Predicate locationPredicate = criteriaBuilder.equal(forecastRoot.get(WeatherForecast_.location), loc);
			Predicate typePredicate = criteriaBuilder.equal(forecastRoot.get(WeatherForecast_.forecastSource), forecast.getForecastSource());
			forecastQuery.where(criteriaBuilder.and(forecastDatePredicate, locationPredicate, typePredicate));
			WeatherForecast forecastInDb = null;
			try {
				forecastInDb = em.createQuery(forecastQuery).getSingleResult();
				if (forecastInDb != null) {
					if (!sameForecast(forecast, forecastInDb)) {
						forecast.setId(forecastInDb.getId());
						update++;
					} else {
						continue;//don't update the forecast if it didn't change
					}
				} else {
					insert++;
				}
			} catch (NoResultException e) {
				//okay forecast not yet available
			}
			forecast.setLastUpdate(new Date());
			em.merge(forecast);
		}
		LOGGER.info("update of forecast data done. in total " + forecasts.size() + " forecasts where provided, " + update + " where updated and " + insert + "  where inserted.");
	}

	private boolean sameForecast(WeatherForecast forecast, WeatherForecast other) {
		if (forecast.getChanceOfRain() == null) {
			if (other.getChanceOfRain() != null)
				return false;
		} else if (forecast.getChanceOfRain().compareTo(other.getChanceOfRain()) > 0)
			return false;
		if (forecast.getCloudCoverTotal() == null) {
			if (other.getCloudCoverTotal() != null)
				return false;
		} else if (forecast.getCloudCoverTotal().compareTo(other.getCloudCoverTotal()) > 0)
			return false;
		if (forecast.getHumidity() == null) {
			if (other.getHumidity() != null)
				return false;
		} else if (forecast.getHumidity().compareTo(other.getHumidity()) > 0)
			return false;
		if (forecast.getMeanWindDirection() == null) {
			if (other.getMeanWindDirection() != null)
				return false;
		} else if (forecast.getMeanWindDirection().compareTo(other.getMeanWindDirection()) > 0)
			return false;
		if (forecast.getMeanWindSpeed() == null) {
			if (other.getMeanWindSpeed() != null)
				return false;
		} else if (forecast.getMeanWindSpeed().compareTo(other.getMeanWindSpeed()) > 0)
			return false;
		if (forecast.getPrecipitation() == null) {
			if (other.getPrecipitation() != null)
				return false;
		} else if (forecast.getPrecipitation().compareTo(other.getPrecipitation()) > 0)
			return false;
		if (forecast.getPressure() == null) {
			if (other.getPressure() != null)
				return false;
		} else if (forecast.getPressure().compareTo(other.getPressure()) > 0)
			return false;
		if (forecast.getTemperature() == null) {
			if (other.getTemperature() != null)
				return false;
		} else if (forecast.getTemperature().compareTo(other.getTemperature()) > 0)
			return false;
		if (!forecast.getWeather().equals(other.getWeather()))
			return false;
		return true;
	}
}
