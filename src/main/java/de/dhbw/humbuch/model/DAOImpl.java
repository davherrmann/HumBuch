package de.dhbw.humbuch.model;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.persist.Transactional;

import de.dhbw.humbuch.event.EntityUpdateEvent;
import de.dhbw.humbuch.model.entity.Entity;

public class DAOImpl<EntityType extends Entity> implements
		DAO<EntityType> {

	private Provider<EntityManager> emProvider;
	private EventBus eventBus;
	
	private Class<EntityType> entityClass;

	
	@SuppressWarnings("unchecked")
	@Inject
	public DAOImpl(TypeLiteral<EntityType> entityType, Provider<EntityManager> emProvider, EventBus eventBus) {
		this.emProvider = emProvider;
		this.eventBus = eventBus;
		entityClass = (Class<EntityType>) entityType.getRawType();
	}

	@Transactional
	public EntityType insert(EntityType entity) {
		EntityType mergedEntity = getEntityManager().merge(entity);
		fireEntityUpdateEvent();
		return mergedEntity;
	}

	public EntityType find(final Object id) {
		return (EntityType) getEntityManager().find(getEntityClass(),
				id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EntityType> findAllWithCriteria(
			final Criterion... criteriaArray) {
		Session session = (Session) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria(getEntityClass());
		for (Criterion criterion : criteriaArray) {
			criteria.add(criterion);
		}
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EntityType> findAllWithCriteria(Order order,
			Criterion... criteriaArray) {
		Session session = (Session) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria(getEntityClass());
		criteria.addOrder(order);
		for(Criterion criterion : criteriaArray) {
			criteria.add(criterion);
		}
		
		return criteria.list();
	}

	@Transactional
	public void update(EntityType entity) {
		getEntityManager().merge(entity);
		fireEntityUpdateEvent();
	}

	@Transactional
	public void delete(EntityType entity) {
		getEntityManager().remove(getEntityManager().merge(entity));
		fireEntityUpdateEvent();
	}

	public EntityManager getEntityManager() {
		return emProvider.get();
	}

	public Class<EntityType> getEntityClass() {
		return entityClass;
	}

	@Override
	public List<EntityType> findAll() {
		return getEntityManager().createQuery(
				"from " + getEntityClass().getSimpleName(), getEntityClass())
				.getResultList();
	}

	@Override
	public EntityType findSingleWithCriteria(Criterion... criteriaArray) {
		List<EntityType> resultList = findAllWithCriteria(criteriaArray);
		if (resultList.size() > 0) {
			return resultList.get(0);
		} else {
			return null;
		}
	}

	@Override
	public EntityType findSingleWithCriteria(Order order,
			Criterion... criteriaArray) {
		List<EntityType> resultList = findAllWithCriteria(order, criteriaArray);
		if(resultList.size() > 0) {
			return resultList.get(0);
		} else {
			return null;
		}
	}
	
	private void fireEntityUpdateEvent() {
		eventBus.post(new EntityUpdateEvent(getEntityClass()));
	}
}