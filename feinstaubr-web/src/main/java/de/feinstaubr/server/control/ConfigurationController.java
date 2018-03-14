package de.feinstaubr.server.control;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.feinstaubr.server.entity.FeinstaubrConfiguration;
import de.feinstaubr.server.entity.FeinstaubrConfiguration_;

@Stateless
public class ConfigurationController {
	private static final Logger LOGGER = Logger.getLogger(ConfigurationController.class.getName());
	
	@PersistenceContext
	private EntityManager em;
	
	public String getConfiguration(String category, String key) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<FeinstaubrConfiguration> query = criteriaBuilder.createQuery(FeinstaubrConfiguration.class);
		Root<FeinstaubrConfiguration> root = query.from(FeinstaubrConfiguration.class);
		Predicate predicateCategory = criteriaBuilder.equal(root.get(FeinstaubrConfiguration_.category), category);
		Predicate predicateKey = criteriaBuilder.equal(root.get(FeinstaubrConfiguration_.key), key);
		query.where(criteriaBuilder.and(predicateCategory, predicateKey));
		try {
			FeinstaubrConfiguration singleResult = em.createQuery(query).getSingleResult();
			return singleResult.getValue();
		} catch (NoResultException e) {
			LOGGER.warning("no configuration available for category " + category + " and key " + key);
			return null;
		}
	}
	public String getConfiguration(String category, String key, String defaultValue) {
		String val = getConfiguration(category, key);
		if (val == null) {
			return defaultValue;
		}
		return val;
	}
	
	public Integer getConfigurationInt(String category, String key) {
		String val = getConfiguration(category, key);
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			LOGGER.warning("unable to parse config value as string in category " + category + " with key " + key + " (found value " + val);
		}
		return null;
	}
	
	public int getConfigurationInt(String category, String key, int defaultValue) {
		Integer val = getConfigurationInt(category, key);
		if (val == null) {
			return defaultValue;
		}
		return val;
	}
	
	public <E extends Enum<E>> E getConfigurationEnum(String category, String key, Class<E> enumClass, E defaultEnumValue) {
		String enumString = getConfiguration(category, key);
		E enumValue = defaultEnumValue;
		try {
			enumValue = Enum.valueOf(enumClass, enumString);
		} catch (IllegalArgumentException | NullPointerException e) {
			LOGGER.warning("unexpected value configured for enum. We found " + enumString + " in the config db and were not able to create a enum for this string due to " + e);
		}
		return enumValue;

	}


}
