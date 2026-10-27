<<<<<<< HEAD
package dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class GenericDAO<T, ID extends Serializable> implements IGenericDAO<T, ID> {

	protected SessionFactory sessionFactory;

	/*
	 * Constructor de GenericDAO.
	 *
	 * El nom del constructor ha de coincidir exactament amb el nom de la classe,
	 * incloses les majúscules.
	 *
	 * Obtenim la SessionFactory gestionada per SessionManager.
	 */
	public GenericDAO() {
		sessionFactory = SessionManager.getSessionFactory();
	}

	/*
	 * Guarda una entitat nova o actualitza una entitat existent.
	 */
	@Override
	public void saveOrUpdate(T entity) {

		Session session = sessionFactory.getCurrentSession();

		try {
			session.beginTransaction();

			/*
			 * merge() retorna una instància gestionada. Permet treballar també amb entitats
			 * detached.
			 */
			session.merge(entity);

			session.getTransaction().commit();

		} catch (RuntimeException e) {
			handleException(session, e);
		}
	}

	/*
	 * Busca una entitat mitjançant la seva clau primària.
	 */
	@Override
	public T get(ID id) {

		Session session = sessionFactory.getCurrentSession();

		try {
			session.beginTransaction();

			T entity = session.get(getEntityClass(), id);

			session.getTransaction().commit();

			return entity;

		} catch (RuntimeException e) {
			handleException(session, e);
			return null;
		}
	}

	/*
	 * Elimina una entitat a partir del seu identificador.
	 */
	@Override
	public void delete(ID id) {

		Session session = sessionFactory.getCurrentSession();

		try {
			session.beginTransaction();

			/*
			 * Utilitzem la mateixa sessió i transacció. No cridem get(), perquè get()
			 * obriria la seva pròpia transacció.
			 */
			T entity = session.get(getEntityClass(), id);

			if (entity != null) {
				session.remove(entity);
			} else {
				System.out.println("No existeix cap entitat amb aquest ID");
			}

			session.getTransaction().commit();

		} catch (RuntimeException e) {
			handleException(session, e);
		}
	}

	/*
	 * Elimina directament una entitat.
	 */
	@Override
	public void delete(T entity) {

		Session session = sessionFactory.getCurrentSession();

		try {
			session.beginTransaction();

			/*
			 * L'entitat pot venir d'una altra sessió i estar detached. merge() torna una
			 * instància gestionada per la sessió actual.
			 */
			T managedEntity = session.merge(entity);

			session.remove(managedEntity);

			session.getTransaction().commit();

		} catch (RuntimeException e) {
			handleException(session, e);
		}
	}

	/*
	 * Retorna totes les entitats del tipus T.
	 */
	@Override
	public List<T> list() {

		Session session = sessionFactory.getCurrentSession();

		try {
			session.beginTransaction();

			/*
			 * JPQL/HQL utilitza el nom de l'entitat Java, no el nom de la taula de la base
			 * de dades.
			 */
			List<T> entities = session.createQuery("FROM " + getEntityClass().getName(), getEntityClass())
					.getResultList();

			session.getTransaction().commit();

			return entities;

		} catch (RuntimeException e) {
			handleException(session, e);
			return List.of();
		}
	}

	/*
	 * Utilitza reflection per obtenir el tipus real de l'entitat.
	 *
	 * Per exemple: JugadorDAO extends GenericDAO<Jugador, Integer>
	 *
	 * getEntityClass() retornarà Jugador.class.
	 */
	@SuppressWarnings("unchecked")
	private Class<T> getEntityClass() {

		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();

		return (Class<T>) genericSuperclass.getActualTypeArguments()[0];
	}

	/*
	 * Gestiona els errors de les operacions DAO.
	 *
	 * Si hi ha una transacció activa, es fa rollback per desfer tots els canvis de
	 * l'operació.
	 */
	protected void handleException(Session session, RuntimeException exception) {

		exception.printStackTrace();

		if (session != null && session.getTransaction() != null && session.getTransaction().isActive()) {

			session.getTransaction().rollback();
		}
	}
}
=======
package dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;


public class GenericDAO<T, ID extends Serializable> implements IGenericDAO<T, ID>  {

	protected SessionFactory sessionFactory;

	// Utilitzem el constructor per obtenir la SessionFactory (singleton)
	public GenericDAO() {
		sessionFactory = SessionManager.getSessionFactory();
	}

	/*
	 * Ara si, desenvolupem el codi de manera genèrica ja que totes les classes necessiten aquests mètodes
	 */
	@Override
	public void saveOrUpdate(T entity) {
		Session session = sessionFactory.getCurrentSession();
		try {
			// Tota operació de modificació necessita una transacció
			session.beginTransaction();
			session.merge(entity);
			session.getTransaction().commit();
		} catch (RuntimeException e) {
			handleException(session, e);
		}
	}

	@Override
	public T get(ID id) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			// Recuperem la classe mitjançant getEntityClass
			T entity = session.find(getEntityClass(), id);
			session.getTransaction().commit();
			return entity;
		} catch (RuntimeException e) {
			handleException(session, e);
			return null;
		}
	}

	@Override
	public void delete(ID id) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			// Dins de la transacció fem servir la mateixa sessió
			// (no podem cridar get() perquè obriria una altra transacció)
			T entity = session.find(getEntityClass(), id);
			if (entity != null) {
				session.remove(entity);
			} else {
				System.out.println("No existeix aquest ID");
			}
			session.getTransaction().commit();
		} catch (RuntimeException e) {
			handleException(session, e);
		}
	}

	@Override
	public void delete(T entity) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			// Si l'objecte ve d'una altra sessió (detached), remove() el rebutja:
			// primer el tornem gestionat amb merge
			session.remove(session.merge(entity));
			session.getTransaction().commit();
		} catch (RuntimeException e) {
			handleException(session, e);
		}
	}

	@Override
	public List<T> list() {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			// Fem el SELECT ALL
			List<T> entities = session.createQuery("FROM " + getEntityClass().getName(), getEntityClass())
					.getResultList();
			session.getTransaction().commit();
			return entities;
		} catch (RuntimeException e) {
			handleException(session, e);
			return null;
		}
	}

	/**
	 * Mètode auxiliar que utilitza reflection per obtenir la classe de l'entitat
	 */
	@SuppressWarnings("unchecked")
	private Class<T> getEntityClass() {
		return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	/**
	 * Mètode auxiliar per gestionar les excepcions: les mostrem i fem rollback.
	 * És protected perquè també el fan servir els DAO concrets.
	 * RuntimeException i no HibernateException: també volem rollback amb
	 * IllegalArgumentException, IllegalStateException, errors de validació...
	 */
	protected void handleException(Session session, RuntimeException e) {
		e.printStackTrace();
		if (session != null && session.getTransaction() != null && session.getTransaction().isActive()) {
			session.getTransaction().rollback();
		}
	}
	
}
>>>>>>> 6b192e0e3268a8ecd0ec5b3205110e636b5bab58
