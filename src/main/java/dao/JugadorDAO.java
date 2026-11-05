
package dao;

import java.util.List;

import org.hibernate.Session;

import enumeration.EstatPartida;
import enumeration.TipusRecurs;

import herencia.Poblat;
import herencia.Ciutat;
import herencia.Carretera;
import relacions.Jugador;
import relacions.Partida;
import relacions.RecursJugador;
import taulell.Aresta;
import taulell.Hexagon;
import taulell.Vertex;

public class JugadorDAO extends GenericDAO<Jugador, Integer> implements IJugadorDAO {

	// 3. METODE PUBLIC (Interficie) --> MostrarRecursos() //

	@Override
	public void mostrarRecursos(int idJugador) {

		if (idJugador <= 0) {
			System.out.println("L'identificador del jugador ha de ser superior a zero");
			return;
		}

		Session session = sessionFactory.getCurrentSession();

		try {
			session.beginTransaction();

			String resultat = consultarRecursos(session, idJugador);

			session.getTransaction().commit();

			System.out.println(resultat);

		} catch (RuntimeException e) {
			handleException(session, e);

			System.out.println("Error en consultar els recursos del jugador");
		}
	}

	// 3. METODE PRIVAT AUXILIAR MostrarRecursos() //

	private String consultarRecursos(Session session, int idJugador) {

		Jugador jugador = session.find(Jugador.class, idJugador);

		if (jugador == null) {
			return "No s'ha trobat cap jugador amb l'ID: " + idJugador;
		}

		List<RecursJugador> recursos = session.createNamedQuery("RecursJugador.buscarPerJugador", RecursJugador.class)
				.setParameter("idJugador", idJugador).getResultList();

		StringBuilder text = new StringBuilder();

		text.append("--- Inventari de recursos de ").append(jugador.getNom()).append(" ---")
				.append(System.lineSeparator());

		if (recursos.isEmpty()) {
			text.append("L'inventari està buit.");

			return text.toString();
		}

		for (RecursJugador recurs : recursos) {
			text.append("- ").append(recurs.getTipus()).append(": ").append(recurs.getQuantitat())
					.append(System.lineSeparator());
		}

		return text.toString();
	}

	// METODE PUBLIC CONSTRUIR CARRETERA //

	@Override
	public void ConstruirCarretera(int idJugador, int idAresta) {

		if (idJugador <= 0 || idAresta <= 0) {
			System.out.println("Els identificadors han de ser superiors a zero");
			return;
		}

		Session session = sessionFactory.getCurrentSession();

		try {

			session.beginTransaction();

			// 1. Buscar el jugador

			Jugador jugador = session.find(Jugador.class, idJugador);

			if (jugador == null) {
				throw new IllegalArgumentException("El jugador no existeix");
			}

			// 2. Buscar l'aresta

			Aresta aresta = session.find(Aresta.class, idAresta);

			if (aresta == null) {
				throw new IllegalArgumentException("L'aresta no existeix");
			}

			// 3. Obtenir la partida del jugador

			Partida partida = jugador.getPartida();

			if (partida == null || aresta.getPartida() == null
					|| !partida.getId().equals(aresta.getPartida().getId())) {

				throw new IllegalArgumentException("El jugador i l'aresta no pertanyen a la mateixa partida");
			}

			// 4. Comprovar que la partida permet construir

			if (partida.getEstat() != EstatPartida.PREPARACIO && partida.getEstat() != EstatPartida.EN_CURS) {

				throw new IllegalArgumentException("La partida no permet construir");
			}

			// 5. Comprovar que és el torn del jugador

			Jugador jugadorActiu = partida.getJugadorActiu();

			if (jugadorActiu == null || !jugadorActiu.getId().equals(idJugador)) {

				throw new IllegalArgumentException("No és el teu torn");
			}

			// 6. Comprovar el límit de 15 carreteres

			Long nombreCarreteres = session.createQuery("""
					SELECT COUNT(c)
					FROM Carretera c
					WHERE c.propietari.id = :idJugador
					""", Long.class).setParameter("idJugador", idJugador).getSingleResult();

			if (nombreCarreteres >= 15) {

				throw new IllegalArgumentException("Has arribat al límit de 15 carreteres");
			}

			// 7. Comprovar que l'aresta està lliure

			Long total = session.createQuery("""
					SELECT COUNT(c)
					FROM Carretera c
					WHERE c.aresta.id = :idAresta
					""", Long.class).setParameter("idAresta", idAresta).getSingleResult();

			if (total > 0) {

				throw new IllegalArgumentException("Ja hi ha una carretera construïda en aquesta aresta");
			}

			// 8. Comprovar si estem en la fase de preparació

			boolean fasePreparacio = partida.getEstat() == EstatPartida.PREPARACIO;

			// 9. Comprovar la connectivitat

			if (fasePreparacio) {

				/*
				 * En la fase de preparació, la carretera ha de tocar un poblat propi que encara
				 * no tingui cap carretera pròpia adjacent.
				 */

				Long poblatsSenseCarretera = session.createQuery("""
						SELECT COUNT(a)
						FROM Assentament a
						WHERE a.propietari.id = :idJugador
						  AND a.vertex.id IN (:vertexA, :vertexB)
						  AND NOT EXISTS (
						      SELECT c.id
						      FROM Carretera c
						      WHERE c.propietari.id = :idJugador
						        AND (
						            c.aresta.vertexA.id = a.vertex.id
						            OR c.aresta.vertexB.id = a.vertex.id
						        )
						  )
						""", Long.class).setParameter("idJugador", idJugador)
						.setParameter("vertexA", aresta.getVertexA().getId())
						.setParameter("vertexB", aresta.getVertexB().getId()).getSingleResult();

				if (poblatsSenseCarretera == 0) {

					throw new IllegalArgumentException(
							"La carretera ha de tocar el poblat propi construït en aquest torn");
				}

				// Comprovar que primer s'ha construït el poblat d'aquesta ronda

				Long nombrePoblats = session.createNamedQuery("Poblat.comptarPerJugador", Long.class)
						.setParameter("idJugador", idJugador).getSingleResult();

				if (nombrePoblats != partida.getRonda()) {

					throw new IllegalArgumentException("Has de construir primer el poblat d'aquesta ronda");
				}

				if (nombreCarreteres != partida.getRonda() - 1) {

					throw new IllegalArgumentException("Ja has construït la carretera d'aquesta ronda");
				}

			} else {

				// 10. En partida normal, comprovar els daus

				if (!Boolean.TRUE.equals(partida.getDausTirats())) {

					throw new IllegalArgumentException("Has de tirar els daus abans de construir");
				}

				// 11. Comprovar que l'aresta està connectada a una construcció
				// o carretera pròpia

				boolean connectada = teConnexioPerCarretera(session, idJugador, idAresta,
						aresta.getVertexA().getId())
						|| teConnexioPerCarretera(session, idJugador, idAresta, aresta.getVertexB().getId());

				if (!connectada) {

					throw new IllegalArgumentException(
							"La carretera no està connectada a cap construcció ni carretera pròpia");
				}

				// 12. Comprovar que el jugador té els recursos necessaris

				if (!jugador.teRecursosPerCarretera()) {

					throw new IllegalArgumentException("No tens prou recursos. Necessites 1 FUSTA i 1 MAÓ");
				}
			}

			// 13. Crear la carretera

			Carretera carretera = new Carretera(jugador, aresta);

			// 14. Afegir la carretera a les construccions del jugador

			jugador.afegirConstruccio(carretera);

			// 15. Guardar la carretera a la base de dades

			session.persist(carretera);

			// 16. Descomptar els recursos si no estem en la fase de preparació

			if (!fasePreparacio) {

				jugador.buscarRecurs(TipusRecurs.FUSTA).restarQuantitat(1);

				jugador.buscarRecurs(TipusRecurs.MAO).restarQuantitat(1);
			}

			// 17. Confirmar els canvis a la base de dades

			session.getTransaction().commit();

			System.out.println("Carretera construïda correctament a l'aresta " + idAresta);

		} catch (RuntimeException e) {

			handleException(session, e);

			System.out.println("Error en construir la carretera: " + e.getMessage());
		}
	}

	private boolean teConnexioPerCarretera(Session session, int idJugador, int idAresta, int idVertex) {
		Long assentamentsPropietaris = session.createQuery("""
				SELECT COUNT(a)
				FROM Assentament a
				WHERE a.propietari.id = :idJugador
				  AND a.vertex.id = :idVertex
				""", Long.class).setParameter("idJugador", idJugador).setParameter("idVertex", idVertex)
				.getSingleResult();

		if (assentamentsPropietaris > 0) {
			return true;
		}

		Long carreteresConnectades = session.createQuery("""
				SELECT COUNT(c)
				FROM Carretera c
				WHERE c.propietari.id = :idJugador
				  AND c.aresta.id <> :idAresta
				  AND (c.aresta.vertexA.id = :idVertex OR c.aresta.vertexB.id = :idVertex)
				  AND NOT EXISTS (
				      SELECT a.id
				      FROM Assentament a
				      WHERE a.vertex.id = :idVertex
				        AND a.propietari.id <> :idJugador
				  )
				""", Long.class).setParameter("idJugador", idJugador).setParameter("idAresta", idAresta)
				.setParameter("idVertex", idVertex).getSingleResult();

		return carreteresConnectades > 0;
	}

	// METODE PUBLIC CONSTRUIR POBLAT //

	@Override
	public void ConstruirPoblat(int idJugador, int idVertex) {

		if (idJugador <= 0 || idVertex <= 0) {
			System.out.println("Els identificadors han de ser superiors a zero");
			return;
		}

		Session session = sessionFactory.getCurrentSession();

		try {

			session.beginTransaction();

			// 1. Buscar el jugador

			Jugador jugador = session.find(Jugador.class, idJugador);

			if (jugador == null) {
				throw new IllegalArgumentException("El jugador no existeix");
			}

			// 2. Buscar el vèrtex

			Vertex vertex = session.find(Vertex.class, idVertex);

			if (vertex == null) {
				throw new IllegalArgumentException("El vèrtex no existeix");
			}

			// 3. Obtenir la partida del jugador

			Partida partida = jugador.getPartida();

			if (partida == null || vertex.getPartida() == null
					|| !partida.getId().equals(vertex.getPartida().getId())) {

				throw new IllegalArgumentException("El jugador i el vèrtex no pertanyen a la mateixa partida");
			}

			// 4. Comprovar que la partida permet construir

			if (partida.getEstat() != EstatPartida.PREPARACIO && partida.getEstat() != EstatPartida.EN_CURS) {

				throw new IllegalArgumentException("La partida no permet construir");
			}

			// 5. Comprovar que és el torn del jugador

			Jugador jugadorActiu = partida.getJugadorActiu();

			if (jugadorActiu == null || !jugadorActiu.getId().equals(idJugador)) {

				throw new IllegalArgumentException("No és el teu torn");
			}

			// 6. Comprovar que el vèrtex està lliure

			Long assentamentsEnVertex = session.createQuery("""
					SELECT COUNT(a)
					FROM Assentament a
					WHERE a.vertex.id = :idVertex
					""", Long.class).setParameter("idVertex", idVertex).getSingleResult();

			if (assentamentsEnVertex > 0) {

				throw new IllegalArgumentException("Ja hi ha un poblat o una ciutat en aquest vèrtex");
			}

			// 7. Comprovar la regla de la distància

			for (Aresta aresta : partida.getArestes()) {

				Vertex vertexVeí = null;

				if (aresta.getVertexA().getId() == vertex.getId()) {

				    vertexVeí = aresta.getVertexB();

				} else if (aresta.getVertexB().getId() == vertex.getId()) {

				    vertexVeí = aresta.getVertexA();
				}

				if (vertexVeí != null) {

					Long assentamentsVeins = session.createQuery("""
							SELECT COUNT(a)
							FROM Assentament a
							WHERE a.vertex.id = :idVertex
							""", Long.class).setParameter("idVertex", vertexVeí.getId()).getSingleResult();

					if (assentamentsVeins > 0) {

						throw new IllegalArgumentException(
								"No es pot construir: hi ha un altre poblat o ciutat massa a prop");
					}
				}
			}

			// 8. Comprovar si estem en la fase de preparació

			boolean fasePreparacio = partida.getEstat() == EstatPartida.PREPARACIO;

			// 9. Comptar els poblats del jugador

			Long nombrePoblats = session.createNamedQuery("Poblat.comptarPerJugador", Long.class)
					.setParameter("idJugador", idJugador).getSingleResult();

			// 10. Comprovar el límit de 5 poblats

			if (nombrePoblats >= 5) {

				throw new IllegalArgumentException("Has arribat al límit de 5 poblats");
			}

			// 11. Comprovar les condicions de la fase de preparació

			if (fasePreparacio) {

				// En cada torn només es pot construir un poblat

				if (nombrePoblats != partida.getRonda() - 1) {

					throw new IllegalArgumentException("Ja has construït el poblat d'aquesta ronda");
				}

				// Comprovar que encara no s'ha construït la carretera d'aquest torn

				Long nombreCarreteres = session.createQuery("""
						SELECT COUNT(c)
						FROM Carretera c
						WHERE c.propietari.id = :idJugador
						""", Long.class).setParameter("idJugador", idJugador).getSingleResult();

				if (nombreCarreteres != partida.getRonda() - 1) {

					throw new IllegalArgumentException("Ja has construït la carretera d'aquesta ronda");
				}

			} else {

				// 12. Comprovar que s'han tirat els daus

				if (!Boolean.TRUE.equals(partida.getDausTirats())) {

					throw new IllegalArgumentException("Has de tirar els daus abans de construir");
				}

				// 13. Comprovar que el jugador té els recursos necessaris

				if (!jugador.teRecursosPerPoblat()) {

					throw new IllegalArgumentException(
							"No tens prou recursos. Necessites 1 FUSTA, 1 MAÓ, 1 LLANA i 1 BLAT");
				}

				// 15. Comprovar que el vèrtex està connectat a una carretera pròpia

				Long carreteresConnectades = session.createQuery("""
						SELECT COUNT(c)
						FROM Carretera c
						WHERE c.propietari.id = :idJugador
						  AND (
						      c.aresta.vertexA.id = :idVertex
						      OR c.aresta.vertexB.id = :idVertex
						  )
						""", Long.class).setParameter("idJugador", idJugador).setParameter("idVertex", idVertex)
						.getSingleResult();

				if (carreteresConnectades == 0) {

					throw new IllegalArgumentException("El poblat ha d'estar connectat a una carretera pròpia");
				}
			}

			// 16. Crear el poblat al vèrtex indicat

			Poblat poblat = new Poblat(jugador, vertex);

			// 17. Afegir el poblat a les construccions del jugador

			jugador.afegirConstruccio(poblat);

			// 18. Guardar el poblat a la base de dades

			session.persist(poblat);

			// 19. Repartir els recursos dels hexàgons del segon poblat inicial

			if (fasePreparacio && partida.getRonda() == 2) {
				repartirRecursosInicials(session, jugador, partida, vertex);
			}

			// 20. Descomptar els recursos si no estem en la fase de preparació

			if (!fasePreparacio) {

				jugador.buscarRecurs(TipusRecurs.FUSTA).restarQuantitat(1);

				jugador.buscarRecurs(TipusRecurs.MAO).restarQuantitat(1);

				jugador.buscarRecurs(TipusRecurs.LLANA).restarQuantitat(1);

				jugador.buscarRecurs(TipusRecurs.BLAT).restarQuantitat(1);
			}

			// 21. Confirmar els canvis a la base de dades

			session.getTransaction().commit();

			System.out.println("Poblat construït correctament al vèrtex " + idVertex);

		} catch (RuntimeException e) {

			handleException(session, e);

			System.out.println("Error en construir el poblat: " + e.getMessage());
		}
	}

	private void repartirRecursosInicials(Session session, Jugador jugador, Partida partida, Vertex vertex) {
		List<Hexagon> hexagons = session.createQuery("""
				SELECT hv.hexagon
				FROM HexagonVertex hv
				WHERE hv.vertex.id = :idVertex
				  AND hv.hexagon.partida.id = :idPartida
				ORDER BY hv.hexagon.codi
				""", Hexagon.class)
				.setParameter("idVertex", vertex.getId())
				.setParameter("idPartida", partida.getId())
				.getResultList();

		if (hexagons.isEmpty()) {
			throw new IllegalStateException("El vèrtex no està relacionat amb cap hexàgon");
		}

		for (Hexagon hexagon : hexagons) {
			TipusRecurs tipusRecurs = PartidaDAO.obtenirRecurs(hexagon.getTerreny());
			if (tipusRecurs == null) {
				continue;
			}

			RecursJugador recurs = jugador.buscarRecurs(tipusRecurs);
			if (recurs == null) {
				throw new IllegalStateException("No s'ha trobat l'inventari del recurs " + tipusRecurs);
			}

			recurs.sumarQuantitat(1);
		}
	}

	// 6. METODE PUBLIC MILLORAR CIUTAT //

	@Override
	public void MillorarACiutat(int idJugador, int idVertex) {

		if (idJugador <= 0 || idVertex <= 0) {
			System.out.println("Els identificadors han de ser superiors a zero");
			return;
		}

		Session session = sessionFactory.getCurrentSession();

		try {

			session.beginTransaction();

			String resultat = millorarACiutat(session, idJugador, idVertex);

			session.getTransaction().commit();

			System.out.println(resultat);

		} catch (RuntimeException e) {

			handleException(session, e);

			System.out.println("Error en intentar millorar el poblat a ciutat");
		}
	}

	// 6. METODE MILLORAR CIUTAT PRIVATE //

	private String millorarACiutat(Session session, int idJugador, int idVertex) {

		// 1. Buscar el jugador //

		Jugador jugador = session.find(Jugador.class, idJugador);

		if (jugador == null) {
			return "No existeix cap jugador amb aquest ID";
		}

		// 2. Obtenim la partida //

		Partida partida = jugador.getPartida();

		if (partida == null) {
			return "El jugador no pertany a cap partida";
		}

		// 3. Comprobar que la partida esta EN_CURS //

		if (partida.getEstat() != EstatPartida.EN_CURS) {
			return "La partida no esta en curs";
		}

		// 4. Comprobar que es el jugador actiu //

		Jugador jugadorActiu = partida.getJugadorActiu();

		if (jugadorActiu == null || !jugadorActiu.getId().equals(idJugador)) {

			return "No es el teu torn";
		}

		// 5. Comprobar que a tirat daus //

		if (!Boolean.TRUE.equals(partida.getDausTirats())) {
			return "Has de tirar els daus abans de construir";
		}

		// 6. Buscar vertex //

		Vertex vertex = session.find(Vertex.class, idVertex);

		if (vertex == null) {
			return "No existeix aquest vertex";
		}

		// 8. Comprobar que el vertex pertany a la partida del jugador //

		if (vertex.getPartida() == null || !vertex.getPartida().getId().equals(partida.getId())) {

			return "El vertex pertany a una altra partida";
		}

		// 9. Buscar el poblat d'aquest vertex //

		Poblat poblat = session.createQuery("""
				SELECT p
				FROM Poblat p
				WHERE p.vertex.id = :idVertex
				""", Poblat.class).setParameter("idVertex", idVertex).uniqueResult();

		if (poblat == null) {
			return "No hi ha cap poblat en aquest vertex per millorar";
		}

		// 10. Comprobar que el poblat pertany al jugador //

		if (!poblat.getPropietari().getId().equals(idJugador)) {
			return "Aquest poblat no és teu";
		}

		// 11. Comprobar el limit de 4 ciutats //

		Long nombreCiutats = session.createNamedQuery("Ciutat.comptarPerJugador", Long.class)
				.setParameter("idJugador", idJugador).getSingleResult();

		if (nombreCiutats >= 4) {
			return "Has arribat al limit de 4 ciutats";
		}

		// 12. Comprobar recursos necessaris //

		if (!jugador.teRecursosPerCiutat()) {
			return "No tens prou recursos. Necessites 2 BLAT i 3 MINERAL";
		}

		// 13. Filtrar recursos del jugador //

		RecursJugador blat = buscarRecursJugador(session, idJugador, TipusRecurs.BLAT);

		RecursJugador mineral = buscarRecursJugador(session, idJugador, TipusRecurs.MINERAL);

		if (blat == null || mineral == null) {
			return "No s'han trobat els recursos necessaris";
		}

		// 14. Descontar els recursos //

		blat.restarQuantitat(2);
		mineral.restarQuantitat(3);

		// 15. Eliminar el poblat antic //

		jugador.getConstruccions().remove(poblat);

		session.remove(poblat);

		// Forçem la eliminació abans de crear la ciutat //

		session.flush();

		// 16. Crear la nova ciutat al mateix vertex //

		Ciutat ciutat = new Ciutat(jugador, vertex);

		jugador.afegirConstruccio(ciutat);

		session.persist(ciutat);

		// 17. Tornar el resultat

		return "Ciutat creada correctament al vertex " + idVertex;
	}

	// 7. METODE COMERCIAR BANCA PUBLIC //

	@Override
	public void ComerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep) {

		if (idJugador <= 0) {
			System.out.println("L'identificador del jugador ha de ser superior a zero");
			return;
		}

		if (dona == null || rep == null) {
			System.out.println("Els tipus de recurs no poden ser null");
			return;
		}

		if (dona == rep) {
			System.out.println("El recurs que dones i el que reps han de ser diferents");
			return;
		}

		Session session = sessionFactory.getCurrentSession();

		try {
			session.beginTransaction();

			String resultat = comerciarBanca(session, idJugador, dona, rep);

			session.getTransaction().commit();

			System.out.println(resultat);

		} catch (RuntimeException e) {
			handleException(session, e);

			System.out.println("Error en realitzar el comerç amb la banca");
		}
	}

	// 7. METODE MILLORAR CIUTAT PRIVATE //

	private String comerciarBanca(Session session, int idJugador, TipusRecurs dona, TipusRecurs rep) {

		// 1. Buscar el jugador //

		Jugador jugador = session.find(Jugador.class, idJugador);

		if (jugador == null) {
			return "No s'ha trobat cap jugador amb l'ID: " + idJugador;
		}

		// 2. Comprobar pertany a una partida //

		if (jugador.getPartida() == null) {
			return "El jugador no pertany a cap partida";
		}

		// 3. Comprobar que la partida esta EN_CURS

		if (jugador.getPartida().getEstat() != EstatPartida.EN_CURS) {

			return "La partida no esta en curs";
		}

		// 4. Comprobar que es el jugador actiu

		Jugador jugadorActiu = jugador.getPartida().getJugadorActiu();

		if (jugadorActiu == null || !jugadorActiu.getId().equals(jugador.getId())) {

			return "No es el teu torn";
		}

		// 5. Comprobar que ja s'han tirat els daus //

		if (!Boolean.TRUE.equals(jugador.getPartida().getDausTirats())) {

			return "Has de tirar els daus abans de comerciar";
		}

		// 7. Buscar els dos registres de l'inventari //

		RecursJugador recursDona = buscarRecursJugador(session, idJugador, dona);

		RecursJugador recursRep = buscarRecursJugador(session, idJugador, rep);

		if (recursDona == null) {
			return "No s'ha trobat el recurs que vols donar";
		}

		if (recursRep == null) {
			return "No s'ha trobat el recurs que vols rebre";
		}

		// 8. Comprobar que té 4 unitats de recursos //

		if (recursDona.getQuantitat() < 4) {
			return "No tens 4 recursos de tipus " + dona;
		}

		// 9. Realizar intercanvi 4 per 1 //

		boolean restat = recursDona.restarQuantitat(4);

		if (!restat) {
			return "No s'han pogut restar els recursos";
		}

		recursRep.sumarQuantitat(1);

		return "Comerç realitzat correctament: " + "has donat 4 " + dona + " i has rebut 1 " + rep;
	}

	// METODE PRIVAT PER BUSCAR RECURS JUGADOR //

	private RecursJugador buscarRecursJugador(Session session, int idJugador, TipusRecurs tipus) {

		List<RecursJugador> recursos = session.createNamedQuery("RecursJugador.buscarPerJugador", RecursJugador.class)
				.setParameter("idJugador", idJugador).getResultList();

		for (RecursJugador recurs : recursos) {

			if (recurs.getTipus() == tipus) {
				return recurs;
			}
		}

		return null;
	}

	// METODE PUBLIC CALCULAR PUNTS //

	@Override
	public int CalcularPunts(int idJugador) {

		if (idJugador <= 0) {
			throw new IllegalArgumentException("L'identificador del jugador ha de ser superior a zero");
		}

		Session session = sessionFactory.getCurrentSession();

		try {
			session.beginTransaction();

			int punts = calcularPunts(session, idJugador);

			session.getTransaction().commit();

			return punts;

		} catch (RuntimeException e) {
			handleException(session, e);

			throw e;
		}
	}

	// METODE PRIVATE CALCULAR PUNTS //

	private int calcularPunts(Session session, int idJugador) {

		Jugador jugador = session.find(Jugador.class, idJugador);

		if (jugador == null) {
			throw new IllegalArgumentException("No existeix cap jugador amb l'ID: " + idJugador);
		}

		Long nombrePoblats = session.createNamedQuery("Poblat.comptarPerJugador", Long.class)
				.setParameter("idJugador", idJugador).getSingleResult();

		Long nombreCiutats = session.createNamedQuery("Ciutat.comptarPerJugador", Long.class)
				.setParameter("idJugador", idJugador).getSingleResult();

		int puntsPoblats = Math.toIntExact(nombrePoblats);

		int puntsCiutats = Math.toIntExact(nombreCiutats) * 2;

		return puntsPoblats + puntsCiutats;
	}

	// METODE PUBLIC PASSAR TORN //

	@Override
	public void PassarTorn(int idJugador) {

		if (idJugador <= 0) {
			System.out.println("L'identificador del jugador ha de ser superior a zero");
			return;
		}

		Session session = sessionFactory.getCurrentSession();

		try {
			session.beginTransaction();

			String resultat = passarTorn(session, idJugador);

			session.getTransaction().commit();

			System.out.println(resultat);

		} catch (RuntimeException e) {
			handleException(session, e);

			System.out.println("Error en intentar passar el torn");
		}

	}

	// METODE PRIVAT PASSAR TORN //

	private String passarTorn(Session session, int idJugador) {

		// 1. Busquem el jugador //

		Jugador jugador = session.find(Jugador.class, idJugador);

		if (jugador == null) {
			return "No existeix cap jugador amb l'ID: " + idJugador;
		}

		// 2. Comprovem que pertanyi a una partida //

		Partida partida = jugador.getPartida();

		if (partida == null) {
			return "El jugador no pertany a cap partida";
		}

		// 3. Una partida finalitzada no admet accions //

		if (partida.getEstat() == EstatPartida.FINALITZADA) {

			return "La partida ja esta finalitzada";
		}

		// 4. Comprovem que sigui el jugador actiu //

		Jugador jugadorActiu = partida.getJugadorActiu();

		if (jugadorActiu == null || !jugadorActiu.getId().equals(jugador.getId())) {

			return "No es el teu torn";
		}

		// 5. Obtenim els jugadors ordenats pel torn //

		List<Jugador> jugadors = session.createQuery("""
				SELECT j
				FROM Jugador j
				WHERE j.partida.id = :idPartida
				ORDER BY j.ordreTorn
				""", Jugador.class).setParameter("idPartida", partida.getId()).getResultList();

		if (jugadors.isEmpty()) {
			return "La partida no té jugadors";
		}

		// 6. Comportament segons l'estat de la partida //

		if (partida.getEstat() == EstatPartida.PREPARACIO) {

			return passarTornPreparacio(session, partida, jugador, jugadors);
		}

		if (partida.getEstat() == EstatPartida.EN_CURS) {

			return passarTornEnCurs(partida, jugador, jugadors);
		}

		return "L'estat de la partida no permet passar torn";
	}

	// MEOTODE PRIVAT PASSAR TORN EN CURS //

	private String passarTornEnCurs(Partida partida, Jugador jugador, List<Jugador> jugadors) {

		// És obligatori haver tirat els daus

		if (!Boolean.TRUE.equals(partida.getDausTirats())) {

			return "Has de tirar els daus " + "abans de passar torn";
		}

		int indexActual = jugadors.indexOf(jugador);

		if (indexActual < 0) {
			return "El jugador no forma part de la partida";
		}

		int indexSeguent = indexActual + 1;

		// Si era l'últim jugador, tornem al primer

		if (indexSeguent >= jugadors.size()) {

			indexSeguent = 0;

			partida.setRonda(partida.getRonda() + 1);
		}

		Jugador seguentJugador = jugadors.get(indexSeguent);

		partida.setJugadorActiu(seguentJugador);

		partida.setDausTirats(false);

		return "Torn passat correctament a " + seguentJugador.getNom() + ". Ronda actual: " + partida.getRonda();

	}

	// METODE PRIVAT PASSAR TORN EN PREPARACIO //

	private String passarTornPreparacio(Session session, Partida partida, Jugador jugador, List<Jugador> jugadors) {

		Integer rondaPreparacio = partida.getRonda();

		if (rondaPreparacio == null || rondaPreparacio < 1 || rondaPreparacio > 2) {

			return "La ronda de preparació no es vàlida";
		}

		// Comptem els poblats del jugador //

		Long nombrePoblats = session.createNamedQuery("Poblat.comptarPerJugador", Long.class)
				.setParameter("idJugador", jugador.getId()).getSingleResult();

		// Comptem les carreteres del jugador //

		Long nombreCarreteres = session.createQuery("""
				SELECT COUNT(c)
				FROM Carretera c
				WHERE c.propietari.id = :idJugador
				""", Long.class).setParameter("idJugador", jugador.getId()).getSingleResult();

		int poblats = Math.toIntExact(nombrePoblats);

		int carreteres = Math.toIntExact(nombreCarreteres);

		// En la ronda 1 ha de tenir: 1 poblat i 1 carretera

		// En la ronda 2 ha de tenir: 2 poblats i 2 carreteres

		if (poblats != rondaPreparacio) {
			return "Has de col·locar el poblat " + "d'aquesta ronda abans de passar torn";
		}

		if (carreteres != rondaPreparacio) {
			return "Has de col·locar la carretera " + "d'aquesta ronda abans de passar torn";
		}

		// PRIMERA RONDA: ordre ASC

		if (rondaPreparacio == 1) {

			int indexActual = jugadors.indexOf(jugador);

			if (indexActual < jugadors.size() - 1) {

				Jugador seguent = jugadors.get(indexActual + 1);

				partida.setJugadorActiu(seguent);

				return "Torn de preparació passat a " + seguent.getNom();
			}

			// L'últim jugador de la primera ronda comença també la segona ronda.

			partida.setRonda(2);
			partida.setJugadorActiu(jugador);

			return "Comença la segona ronda de preparació. " + "Continua el jugador " + jugador.getNom();
		}

		// SEGONA RONDA: ordre DESC

		int indexActual = jugadors.indexOf(jugador);

		if (indexActual > 0) {

			Jugador anterior = jugadors.get(indexActual - 1);

			partida.setJugadorActiu(anterior);

			return "Torn de preparació passat a " + anterior.getNom();
		}

		// Ha acabat el jugador d'ordre 1. Comprovem que tots tinguin: 2 poblats i 2
		// carreteres.

		if (!preparacioCompletada(session, jugadors)) {

			return "No tots els jugadors han completat " + "la fase de preparació";
		}

		// La partida passa a EN_CURS

		partida.setEstat(EstatPartida.EN_CURS);

		partida.setRonda(1);

		partida.setJugadorActiu(jugadors.get(0));

		partida.setDausTirats(false);

		return "Fase de preparació finalitzada. " + "Comença la partida amb el jugador " + jugadors.get(0).getNom();
	}

	// METODE AUXILIAR PREPARACIO COMPLETADA //

	// Metode auxiliar preparacioCompletada //

	private boolean preparacioCompletada(Session session, List<Jugador> jugadors) {

		if (jugadors == null || jugadors.isEmpty()) {
			return false;
		}

		for (Jugador jugador : jugadors) {

			Long nombrePoblats = session.createNamedQuery("Poblat.comptarPerJugador", Long.class)
					.setParameter("idJugador", jugador.getId()).getSingleResult();

			Long nombreCarreteres = session.createQuery("""
					SELECT COUNT(c)
					FROM Carretera c
					WHERE c.propietari.id = :idJugador
					""", Long.class).setParameter("idJugador", jugador.getId()).getSingleResult();

			if (nombrePoblats.longValue() != 2L || nombreCarreteres.longValue() != 2L) {

				return false;
			}
		}

		return true;
	}

	// METODES AMPLIACIÓN //

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
