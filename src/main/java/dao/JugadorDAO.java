
package dao;

import java.util.List;


import org.hibernate.Session;

import enumeration.EstatPartida;
import enumeration.TipusRecurs;
import relacions.Jugador;
import relacions.Partida;
import relacions.RecursJugador;
import taulell.Vertex;

public class JugadorDAO extends GenericDAO<Jugador, Integer> implements IJugadorDAO {
	

	// 3. MÈTODE PÚBLIC (Interfície) --> MostrarRecursos()

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

	// 3. MÈTODE PRIVAT AUXILIAR --> --> MostrarRecursos()

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

	@Override
	public void ConstruirCarretera(int idJugador, int idAresta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ConstruirPoblat(int idJugador, int idVertex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void MillorarACiutat(int idJugador, int idVertex) {
		
		
		

	}

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

	private String comerciarBanca(Session session, int idJugador, TipusRecurs dona, TipusRecurs rep) {

		// 1. Buscar el jugador

		Jugador jugador = session.find(Jugador.class, idJugador);

		if (jugador == null) {
			return "No s'ha trobat cap jugador amb l'ID: " + idJugador;
		}

		// 2. Comprobar que el jugador pertenece a una partida

		if (jugador.getPartida() == null) {
			return "El jugador no pertany a cap partida";
		}

		// 3. Comprobar que la partida está EN_CURS

		if (jugador.getPartida().getEstat() != EstatPartida.EN_CURS) {

			return "La partida no està en curs";
		}

		// 4. Comprobar que es el jugador activo

		Jugador jugadorActiu = jugador.getPartida().getJugadorActiu();

		if (jugadorActiu == null || !jugadorActiu.getId().equals(jugador.getId())) {

			return "No és el teu torn";
		}

		// 5. Comprobar que ya se han tirado los dados

		if (!Boolean.TRUE.equals(jugador.getPartida().getDausTirats())) {

			return "Has de tirar els daus abans de comerciar";
		}

		// 6. Comprobar que no hay ladrón pendiente

		if (Boolean.TRUE.equals(jugador.getPartida().getLladrePendent())) {

			return "No es pot comerciar mentre el lladre està pendent";
		}

		// 7. Buscar los dos registros del inventario

		RecursJugador recursDona = buscarRecursJugador(session, idJugador, dona);

		RecursJugador recursRep = buscarRecursJugador(session, idJugador, rep);

		if (recursDona == null) {
			return "No s'ha trobat el recurs que vols donar";
		}

		if (recursRep == null) {
			return "No s'ha trobat el recurs que vols rebre";
		}

		// 8. Comprobar que tiene al menos 4 unidades

		if (recursDona.getQuantitat() < 4) {
			return "No tens 4 recursos de tipus " + dona;
		}

		// 9. Realizar el intercambio 4 por 1

		boolean restat = recursDona.restarQuantitat(4);

		if (!restat) {
			return "No s'han pogut restar els recursos";
		}

		recursRep.sumarQuantitat(1);

		// No hace falta session.merge() porque ambos objetos
		// están gestionados por la sesión actual.

		return "Comerç realitzat correctament: " + "has donat 4 " + dona + " i has rebut 1 " + rep;
	}

	// Metodo auxiliar para buscar recurso //

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

	// Metode calcular punts //

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

	// Metode calcular punt privat //

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

	// Metode public PassarTorn //

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

	// METODE PRIVAT passarTorn //

	private String passarTorn(Session session, int idJugador) {

		// 1. Busquem el jugador

		Jugador jugador = session.find(Jugador.class, idJugador);

		if (jugador == null) {
			return "No existeix cap jugador amb l'ID: " + idJugador;
		}

		// 2. Comprovem que pertanyi a una partida

		Partida partida = jugador.getPartida();

		if (partida == null) {
			return "El jugador no pertany a cap partida";
		}

		// 3. Una partida finalitzada no admet accions

		if (partida.getEstat() == EstatPartida.FINALITZADA) {

			return "La partida ja està finalitzada";
		}

		// 4. Comprovem que sigui el jugador actiu

		Jugador jugadorActiu = partida.getJugadorActiu();

		if (jugadorActiu == null || !jugadorActiu.getId().equals(jugador.getId())) {

			return "No és el teu torn";
		}

		// 5. No es pot passar torn amb el lladre pendent

		if (Boolean.TRUE.equals(partida.getLladrePendent())) {

			return "No es pot passar torn mentre " + "el moviment del lladre està pendent";
		}

		// 6. Obtenim els jugadors ordenats pel torn

		List<Jugador> jugadors = session.createQuery("""
				SELECT j
				FROM Jugador j
				WHERE j.partida.id = :idPartida
				ORDER BY j.ordreTorn
				""", Jugador.class).setParameter("idPartida", partida.getId()).getResultList();

		if (jugadors.isEmpty()) {
			return "La partida no té jugadors";
		}

		// 7. Comportament segons l'estat de la partida

		if (partida.getEstat() == EstatPartida.PREPARACIO) {

			return passarTornPreparacio(session, partida, jugador, jugadors);
		}

		if (partida.getEstat() == EstatPartida.EN_CURS) {

			return passarTornEnCurs(partida, jugador, jugadors);
		}

		return "L'estat de la partida no permet passar torn";
	}

	// Metode passarTonrEnCurs //

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

	// Metodo passarTornPreparacio //

	private String passarTornPreparacio(Session session, Partida partida, Jugador jugador, List<Jugador> jugadors) {

		Integer rondaPreparacio = partida.getRonda();

		if (rondaPreparacio == null || rondaPreparacio < 1 || rondaPreparacio > 2) {

			return "La ronda de preparació no és vàlida";
		}

		// Comptem els poblats del jugador

		Long nombrePoblats = session.createNamedQuery("Poblat.comptarPerJugador", Long.class)
				.setParameter("idJugador", jugador.getId()).getSingleResult();

		// Comptem les carreteres del jugador

		Long nombreCarreteres = session.createQuery("""
				SELECT COUNT(c)
				FROM Carretera c
				WHERE c.propietari.id = :idJugador
				""", Long.class).setParameter("idJugador", jugador.getId()).getSingleResult();

		int poblats = Math.toIntExact(nombrePoblats);

		int carreteres = Math.toIntExact(nombreCarreteres);

		/*
		 * En la ronda 1 ha de tenir: 1 poblat i 1 carretera.
		 *
		 * En la ronda 2 ha de tenir: 2 poblats i 2 carreteres.
		 */

		if (poblats != rondaPreparacio) {
			return "Has de col·locar el poblat " + "d'aquesta ronda abans de passar torn";
		}

		if (carreteres != rondaPreparacio) {
			return "Has de col·locar la carretera " + "d'aquesta ronda abans de passar torn";
		}

		// Primera ronda: ordre ascendent

		if (rondaPreparacio == 1) {

			int indexActual = jugadors.indexOf(jugador);

			if (indexActual < jugadors.size() - 1) {

				Jugador seguent = jugadors.get(indexActual + 1);

				partida.setJugadorActiu(seguent);

				return "Torn de preparació passat a " + seguent.getNom();
			}

			/*
			 * L'últim jugador de la primera ronda comença també la segona ronda.
			 */

			partida.setRonda(2);
			partida.setJugadorActiu(jugador);

			return "Comença la segona ronda de preparació. " + "Continua el jugador " + jugador.getNom();
		}

		// Segona ronda: ordre descendent

		int indexActual = jugadors.indexOf(jugador);

		if (indexActual > 0) {

			Jugador anterior = jugadors.get(indexActual - 1);

			partida.setJugadorActiu(anterior);

			return "Torn de preparació passat a " + anterior.getNom();
		}

		/*
		 * Ha acabat el jugador d'ordre 1. Comprovem que tots tinguin: 2 poblats i 2
		 * carreteres.
		 */

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
