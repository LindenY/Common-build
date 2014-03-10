package com.lopal.commonBuild.GPDO;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

public class GenericDaoImpl<T extends DomainObject> extends HibernateDaoSupport implements GenericDao<T> {
	
	private Class<T> type;
	
	public GenericDaoImpl(Class<T> type) {
		super();
		this.type = type;
	}
	
	@Autowired
	public void setupSessionFactory(SessionFactory sessionFactory) {
		this.setSessionFactory(sessionFactory);
	}

	public T get(Long id) {
		return (T) getHibernateTemplate().get(type, id);
	}
	
	public List<T> getAll() {
		return getHibernateTemplate().loadAll(type);
	}
	
	public void save(T object) {
		getHibernateTemplate().save(object);
		getHibernateTemplate().flush();
	}
	
	public void delete(T object) {
		getHibernateTemplate().delete(object);
	}
	
}
