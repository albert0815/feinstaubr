package de.feinstaubr.server.boundary;

import java.util.Date;
import java.util.List;

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

import de.feinstaubr.server.entity.DwdForecast;
import de.feinstaubr.server.entity.DwdForecast_;
import de.feinstaubr.server.entity.SensorLocation;

@Singleton
@Startup
public class DwdDataReadScheduler {
	@PersistenceContext
	private EntityManager em;
	
	@Inject
	private DwdApi dwd;

	@PostConstruct
	public void init() {
		updateDwdData();
	}
	
	@Schedule(hour="*", persistent=false)
	public void updateDwdData() {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<SensorLocation> sensorLocationQuery = criteriaBuilder.createQuery(SensorLocation.class);
		Root<SensorLocation> sensorLocationRoot = sensorLocationQuery.from(SensorLocation.class);
		sensorLocationQuery.select(sensorLocationRoot);
		List<SensorLocation> locations = em.createQuery(sensorLocationQuery).getResultList();
		for (SensorLocation loc : locations) {
			List<DwdForecast> forecasts = dwd.getForecasts(loc.getDwdPoiId());
			for (DwdForecast forecast : forecasts) {
				forecast.setLocation(loc);
				CriteriaQuery<DwdForecast> forecastQuery = criteriaBuilder.createQuery(DwdForecast.class);
				Root<DwdForecast> forecastRoot = forecastQuery.from(DwdForecast.class);
				Predicate forecastDatePredicate = criteriaBuilder.equal(forecastRoot.get(DwdForecast_.forecastDate), forecast.getForecastDate());
				Predicate locationPredicate = criteriaBuilder.equal(forecastRoot.get(DwdForecast_.location), loc);
				forecastQuery.where(criteriaBuilder.and(forecastDatePredicate, locationPredicate));
				DwdForecast forecastInDb = null;
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
}
