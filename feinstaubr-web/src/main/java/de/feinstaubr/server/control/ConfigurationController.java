package de.feinstaubr.server.control;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.feinstaubr.server.entity.FeinstaubrConfiguration;
import de.feinstaubr.server.entity.FeinstaubrConfiguration_;

@Stateless
public class ConfigurationController {
	@PersistenceContext
	private EntityManager em;
	
	public String getConfiguration(String category, String key) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<FeinstaubrConfiguration> query = criteriaBuilder.createQuery(FeinstaubrConfiguration.class);
		Root<FeinstaubrConfiguration> root = query.from(FeinstaubrConfiguration.class);
		Predicate predicateCategory = criteriaBuilder.equal(root.get(FeinstaubrConfiguration_.category), category);
		Predicate predicateKey = criteriaBuilder.equal(root.get(FeinstaubrConfiguration_.key), key);
		query.where(criteriaBuilder.and(predicateCategory, predicateKey));
		return em.createQuery(query).getSingleResult().getValue();
	}
}
