package dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import model_joc.RecursJugador;

public class RecursJugadorDAO {
	
	 private final EntityManager entityManager;

	    public RecursJugadorDAO(EntityManager entityManager) {
	        this.entityManager = entityManager;
	    }

	    public List<RecursJugador> mostrarRecursos(Long idJugador) {

	        if (idJugador == null) {
	            throw new IllegalArgumentException(
	                "L'identificador del jugador no pot ser null"
	            );
	        }

	        return entityManager
	            .createNamedQuery(
	                "RecursJugador.buscarPerJugador",
	                RecursJugador.class
	            )
	            .setParameter("idJugador", idJugador)
	            .getResultList();
	    }

}
