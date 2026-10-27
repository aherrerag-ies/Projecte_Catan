package dao;

import java.util.List;

import org.hibernate.Session;

import enumeration.TipusRecurs;
import herencia.Carretera;
import herencia.Construccio;
import relacions.Jugador;
import taulell.Aresta;
import taulell.Vertex;

public class JugadorDAO extends GenericDAO<Jugador, Integer> implements IJugadorDAO {

	@Override
	public void MostrarRecursos(int idJugador) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ConstruirCarretera(int idJugador, int idAresta) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			Jugador jugador = session.find(Jugador.class, idJugador);
			if (jugador == null) {
				System.out.println("El jugador no existe");
				throw new IllegalArgumentException();
			}
			Aresta aresta = session.find(Aresta.class, idAresta);
			if (aresta == null) {
				System.out.println("La aresta no existe");
				throw new IllegalArgumentException();
			}
			if (!jugador.getPartida().getId().equals(aresta.getPartida().getId())) {
				throw new IllegalArgumentException("El jugador y la aresta no pertenecen a la misma partida");
			}
			Long total = session
					.createQuery("SELECT COUNT(c) FROM Carretera c " + "WHERE c.aresta.id = :idAresta", Long.class)
					.setParameter("idAresta", idAresta).getSingleResult();
			if (total > 0) {
				throw new IllegalArgumentException("Ja hi ha una carretera en aquesta aresta");
			}
			Carretera carretera = new Carretera(aresta);
			jugador.afegirConstruccio(carretera);
			session.persist(carretera);
			session.getTransaction().commit();

		} catch (RuntimeException e) {
			handleException(session, e);
		}

	}

	@Override
	public void ConstruirPoblat(int idJugador, int idVertex) {

	}

	@Override
	public void MillorarACiutat(int idJugador, int idVertex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ComerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep) {
		// TODO Auto-generated method stub

	}

	@Override
	public int CalcularPunts(int idJugador) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void PassarTorn(int idJugador) {
		// TODO Auto-generated method stub

	}

	@Override
	public void MoureLladre(int idJugador, int idHexagon, Integer idJugadorVictima) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ComprarCarta(int idJugador) {
		// TODO Auto-generated method stub

	}

	@Override
	public void JugarCarta(int idJugador, int idCarta, TipusRecurs recurs) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Vertex> VertexsDisponibles(int idJugador) {
		// TODO Auto-generated method stub
		return null;
	}

}
