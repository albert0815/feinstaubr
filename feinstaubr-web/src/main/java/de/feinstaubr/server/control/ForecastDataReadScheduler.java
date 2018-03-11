package de.feinstaubr.server.control;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
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
			List<WeatherForecast> forecasts = dwd.getForecasts(loc.getDwdPoiId());
			updateForecastsInDb(loc, forecasts);
			
			forecasts = openWeather.getForecast(loc.getOpenWeatherId());
			updateForecastsInDb(loc, forecasts);
		}
	}

	private void updateForecastsInDb(SensorLocation loc, List<WeatherForecast> forecasts) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
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
					forecast.setId(forecastInDb.getId());
				}
			} catch (NoResultException e) {
				//okay forecast not yet available
			}
			forecast.setLastUpdate(new Date());
			em.merge(forecast);
		}
	}
}
