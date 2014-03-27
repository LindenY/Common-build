package com.lopal.commonBuild.GPDO;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericDaoImpl<T extends DomainObject> implements GenericDao<T> {
	
	private Class<T> type;
	private SessionFactory sessionFactory;
	
	public GenericDaoImpl(Class<T> type) {
		super();
		this.type = type;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.setSessionFactory(sessionFactory);
	}

	public T get(Long id) {
		return (T) this.sessionFactory.getCurrentSession().get(type, id);
	}
	
	public List<T> getAll() {
		return null;
	}
	
	public void save(T object) {
		this.sessionFactory.getCurrentSession().save(object);
		this.sessionFactory.getCurrentSession().flush();
	}
	
	public void update(T object) {
		this.sessionFactory.getCurrentSession().update(object);
	}
	
	public void delete(T object) {
		this.sessionFactory.getCurrentSession().delete(object);
	}
	
}
