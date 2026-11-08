package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import enumeration.EstatPartida;
import enumeration.Terreny;
import enumeration.TipusRecurs;
import herencia.Assentament;
import herencia.Carretera;
import herencia.Ciutat;
import herencia.Poblat;
import relacions.Jugador;
import relacions.Partida;
import relacions.RecursJugador;
import taulell.Aresta;
import taulell.Hexagon;
import taulell.HexagonVertex;
import taulell.Vertex;

class CatanProvesTest {

	private SessionFactory sessionFactory;
	private PartidaDAO partidaDAO;
	private JugadorDAO jugadorDAO;

	@BeforeEach
	void crearBaseDeDadesEnMemoria() {
		Configuration configuration = new Configuration()
				.addAnnotatedClass(Assentament.class)
				.addAnnotatedClass(Carretera.class)
				.addAnnotatedClass(Ciutat.class)
				.addAnnotatedClass(Poblat.class)
				.addAnnotatedClass(Aresta.class)
				.addAnnotatedClass(Hexagon.class)
				.addAnnotatedClass(HexagonVertex.class)
				.addAnnotatedClass(Vertex.class)
				.addAnnotatedClass(relacions.CartaDesenvolupament.class)
				.addAnnotatedClass(Jugador.class)
				.addAnnotatedClass(Partida.class)
				.addAnnotatedClass(RecursJugador.class);
		configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
		configuration.setProperty("hibernate.connection.url",
				"jdbc:h2:mem:catan_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
		configuration.setProperty("hibernate.connection.username", "sa");
		configuration.setProperty("hibernate.connection.password", "");
		configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		configuration.setProperty("hibernate.current_session_context_class", "thread");
		configuration.setProperty("hibernate.show_sql", "false");
		configuration.setProperty("jakarta.persistence.validation.mode", "none");

		sessionFactory = configuration.buildSessionFactory();
		partidaDAO = new PartidaDAO(sessionFactory);
		jugadorDAO = new JugadorDAO(sessionFactory);
	}

	@AfterEach
	void tancarBaseDeDades() {
		if (sessionFactory != null) {
			sessionFactory.close();
		}
	}

	@Test
	void carregaTopologiaAmbLesQuantitatsEsperades() {
		assertEquals(259, partidaDAO.carregarDades());
		Partida partida = obtenirPrimeraPartida();

		assertEquals(54, comptar("Vertex", partida.getId()));
		assertEquals(19, comptar("Hexagon", partida.getId()));
		assertEquals(72, comptar("Aresta", partida.getId()));
		assertEquals(114, comptarRelacionsHexagonVertex(partida.getId()));
	}

	@Test
	void iniciaPartidaAmbJugadorsTaulerICartes() {
		int idPartida = partidaDAO.IniciarPartida(List.of("Anna", "Biel", "Cris"));

		assertTrue(idPartida > 0);
		Partida partida = carregarPartida(idPartida);
		assertEquals(EstatPartida.PREPARACIO, partida.getEstat());
		assertEquals(3, obtenirJugadors(idPartida).size());
		assertEquals(19, comptar("Hexagon", idPartida));
		assertEquals(21, comptarCartes(idPartida));
		assertEquals(1, comptarDeserts(idPartida));
		assertEquals(18, comptarHexagonsAmbNumero(idPartida));
		assertEquals(15, comptarRecursos(idPartida));
	}

	@Test
	void completaPreparacioProduccioAccionsTornsIVictoria() {
		int idPartida = partidaDAO.IniciarPartida(List.of("Anna", "Biel"));
		assertTrue(idPartida > 0);
		List<Jugador> jugadors = obtenirJugadors(idPartida);
		Jugador primer = jugadors.get(0);
		Jugador segon = jugadors.get(1);

		construirPoblatICarreteraPreparacio(idPartida, primer);
		jugadorDAO.PassarTorn(primer.getId());
		assertEquals(segon.getId(), carregarPartida(idPartida).getJugadorActiu().getId());

		construirPoblatICarreteraPreparacio(idPartida, segon);
		jugadorDAO.PassarTorn(segon.getId());
		assertEquals(2, carregarPartida(idPartida).getRonda());
		assertEquals(segon.getId(), carregarPartida(idPartida).getJugadorActiu().getId());

		int segonVertexSegonaRonda = construirPoblatICarreteraPreparacio(idPartida, segon);
		assertRecursosInicialsCorrectes(idPartida, segon, segonVertexSegonaRonda);
		jugadorDAO.PassarTorn(segon.getId());

		int primerVertexSegonaRonda = construirPoblatICarreteraPreparacio(idPartida, primer);
		assertRecursosInicialsCorrectes(idPartida, primer, primerVertexSegonaRonda);
		jugadorDAO.PassarTorn(primer.getId());
		assertEquals(EstatPartida.EN_CURS, carregarPartida(idPartida).getEstat());

		provarProduccio(idPartida, primer);
		provarConstruccionsComercIActualitzacio(idPartida, primer);

		jugadorDAO.PassarTorn(primer.getId());
		Partida despresDelTorn = carregarPartida(idPartida);
		assertEquals(segon.getId(), despresDelTorn.getJugadorActiu().getId());
		assertFalse(despresDelTorn.getDausTirats());

		actualitzarPuntsPerGuanyar(idPartida, 1);
		partidaDAO.ComprovarVictoria(idPartida);
		Partida finalitzada = carregarPartida(idPartida);
		assertEquals(EstatPartida.FINALITZADA, finalitzada.getEstat());
		assertEquals(primer.getId(), finalitzada.getGuanyador().getId());
	}

	@Test
	void rebutjaIniciarPartidaAmbUnNombreInvalidDeJugadors() {
		assertEquals(-1, partidaDAO.IniciarPartida(List.of("Només un")));
		assertEquals(-1, partidaDAO.IniciarPartida(List.of("A", "B", "C", "D", "E")));
		assertEquals(-1, partidaDAO.IniciarPartida(List.of("A", " ")));
		assertEquals(0, comptarTotesLesPartides());
	}

	private int construirPoblatICarreteraPreparacio(int idPartida, Jugador jugador) {
		int idVertex = trobarVertexLegal(idPartida);
		jugadorDAO.ConstruirPoblat(jugador.getId(), idVertex);
		assertEquals(1, comptarPoblatsEnVertex(idVertex));

		int idAresta = trobarArestaLliureAlVertex(idPartida, idVertex);
		jugadorDAO.ConstruirCarretera(jugador.getId(), idAresta);
		assertEquals(1, comptarCarreteresEnAresta(idAresta));
		return idVertex;
	}

	private void provarProduccio(int idPartida, Jugador jugador) {
		List<Integer> vertexsPoblats = obtenirVertexsDelJugador(jugador.getId());
		assertFalse(vertexsPoblats.isEmpty());
		List<Hexagon> hexagons = obtenirHexagonsAdjacents(idPartida, vertexsPoblats.get(0));
		assertFalse(hexagons.isEmpty());

		int idHexagon = hexagons.get(0).getId();
		actualitzarHexagonPerProduccio(idHexagon);
		actualitzarTirada(idPartida, false);

		PartidaDAO dauVuit = new PartidaDAO(sessionFactory) {
			@Override
			protected Random crearAleatori() {
				return new Random() {
					private final int[] tirada = { 3, 3 };
					private int index;

					@Override
					public int nextInt(int bound) {
						return tirada[index++];
					}
				};
			}
		};

		int fustaAbans = obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.FUSTA);
		String resultat = dauVuit.TirarDaus(idPartida);
		assertTrue(resultat.contains("= 8"));
		assertTrue(obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.FUSTA) > fustaAbans);
		assertEquals(Integer.valueOf(8), carregarPartida(idPartida).getUltimaTirada());
		assertTrue(carregarPartida(idPartida).getDausTirats());

		actualitzarTirada(idPartida, false);
		PartidaDAO dauSet = new PartidaDAO(sessionFactory) {
			@Override
			protected Random crearAleatori() {
				return new Random() {
					private final int[] tirada = { 2, 3 };
					private int index;

					@Override
					public int nextInt(int bound) {
						return tirada[index++];
					}
				};
			}
		};
		int fustaDespresVuit = obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.FUSTA);
		String resultatSet = dauSet.TirarDaus(idPartida);
		assertTrue(resultatSet.contains("= 7"));
		assertEquals(fustaDespresVuit, obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.FUSTA));
		assertFalse(carregarPartida(idPartida).getLladrePendent());
	}

	private void provarConstruccionsComercIActualitzacio(int idPartida, Jugador jugador) {
		actualitzarTirada(idPartida, true);
		omplirRecursos(jugador.getId(), 10);

		int[] cami = trobarCamiPerConstruirPoblat(idPartida, jugador.getId());
		jugadorDAO.ConstruirCarretera(jugador.getId(), cami[0]);
		assertEquals(1, comptarCarreteresEnAresta(cami[0]));
		jugadorDAO.ConstruirCarretera(jugador.getId(), cami[1]);
		assertEquals(1, comptarCarreteresEnAresta(cami[1]));
		assertEquals(8, obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.FUSTA));
		assertEquals(8, obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.MAO));

		int vertexNou = cami[2];
		jugadorDAO.ConstruirPoblat(jugador.getId(), vertexNou);
		assertEquals(1, comptarPoblatsEnVertex(vertexNou));
		assertEquals(7, obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.FUSTA));
		assertEquals(7, obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.MAO));
		assertEquals(9, obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.LLANA));
		assertEquals(9, obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.BLAT));

		actualitzarRecurs(jugador.getId(), TipusRecurs.FUSTA, 4);
		actualitzarRecurs(jugador.getId(), TipusRecurs.LLANA, 0);
		jugadorDAO.ComerciarBanca(jugador.getId(), TipusRecurs.FUSTA, TipusRecurs.LLANA);
		assertEquals(0, obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.FUSTA));
		assertEquals(1, obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.LLANA));

		int vertexPerMillorar = obtenirVertexsDelJugador(jugador.getId()).get(0);
		actualitzarRecurs(jugador.getId(), TipusRecurs.BLAT, 2);
		actualitzarRecurs(jugador.getId(), TipusRecurs.MINERAL, 3);
		jugadorDAO.MillorarACiutat(jugador.getId(), vertexPerMillorar);
		assertEquals(1, comptarCiutats(jugador.getId()));
		assertEquals(0, obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.BLAT));
		assertEquals(0, obtenirQuantitatRecurs(jugador.getId(), TipusRecurs.MINERAL));
	}

	private int trobarVertexLegal(int idPartida) {
		List<Vertex> vertexs = obtenirVertexs(idPartida);
		List<Aresta> arestes = obtenirArestes(idPartida);
		Set<Integer> ocupats = obtenirVertexsOcupats(idPartida);

		for (Vertex vertex : vertexs) {
			if (ocupats.contains(vertex.getId())) {
				continue;
			}
			boolean distanciesCorrectes = true;
			for (Aresta aresta : arestes) {
				Integer veí = null;
				if (aresta.getVertexA().getId() == vertex.getId()) {
					veí = aresta.getVertexB().getId();
				} else if (aresta.getVertexB().getId() == vertex.getId()) {
					veí = aresta.getVertexA().getId();
				}
				if (veí != null && ocupats.contains(veí)) {
					distanciesCorrectes = false;
					break;
				}
			}
			if (distanciesCorrectes) {
				return vertex.getId();
			}
		}
		throw new AssertionError("No s'ha trobat cap vèrtex legal");
	}

	private int[] trobarCamiPerConstruirPoblat(int idPartida, int idJugador) {
		Set<Integer> idsPoblats = obtenirVertexsOcupats(idPartida);
		List<Integer> idsConstruitsDelJugador = obtenirVertexsDelJugador(idJugador);
		List<Aresta> arestes = obtenirArestes(idPartida);

		for (Integer idVertexPoblat : idsConstruitsDelJugador) {
			for (Aresta primeraAresta : arestes) {
				if (comptarCarreteresEnAresta(primeraAresta.getId()) > 0) {
					continue;
				}
				Integer segonVertex = altreExtrem(primeraAresta, idVertexPoblat);
				if (segonVertex == null || idsPoblats.contains(segonVertex)) {
					continue;
				}
				for (Aresta segonaAresta : arestes) {
					if (segonaAresta.getId().equals(primeraAresta.getId())
							|| comptarCarreteresEnAresta(segonaAresta.getId()) > 0) {
						continue;
					}
					Integer vertexFinal = altreExtrem(segonaAresta, segonVertex);
					if (vertexFinal == null || idsPoblats.contains(vertexFinal)) {
						continue;
					}
					boolean distanciesCorrectes = true;
					for (Aresta adjacent : arestes) {
						Integer veí = altreExtrem(adjacent, vertexFinal);
						if (veí != null && idsPoblats.contains(veí)) {
							distanciesCorrectes = false;
							break;
						}
					}
					if (distanciesCorrectes) {
						return new int[] { primeraAresta.getId(), segonaAresta.getId(), vertexFinal };
					}
				}
			}
		}
		throw new AssertionError("No s'ha trobat un camí de dues arestes per construir un poblat legal");
	}

	private Integer altreExtrem(Aresta aresta, int idVertex) {
		if (aresta.getVertexA().getId() == idVertex) {
			return aresta.getVertexB().getId();
		}
		if (aresta.getVertexB().getId() == idVertex) {
			return aresta.getVertexA().getId();
		}
		return null;
	}

	private int trobarArestaLliureAlVertex(int idPartida, int idVertex) {
		for (Aresta aresta : obtenirArestes(idPartida)) {
			if ((aresta.getVertexA().getId() == idVertex || aresta.getVertexB().getId() == idVertex)
					&& comptarCarreteresEnAresta(aresta.getId()) == 0) {
				return aresta.getId();
			}
		}
		throw new AssertionError("No s'ha trobat cap aresta lliure al vèrtex " + idVertex);
	}

	private void assertRecursosInicialsCorrectes(int idPartida, Jugador jugador, int idVertex) {
		List<Hexagon> hexagons = obtenirHexagonsAdjacents(idPartida, idVertex);
		int[] esperats = new int[TipusRecurs.values().length];
		for (Hexagon hexagon : hexagons) {
			TipusRecurs tipus = PartidaDAO.obtenirRecurs(hexagon.getTerreny());
			if (tipus != null) {
				esperats[tipus.ordinal()]++;
			}
		}
		for (TipusRecurs tipus : TipusRecurs.values()) {
			assertEquals(esperats[tipus.ordinal()], obtenirQuantitatRecurs(jugador.getId(), tipus),
					"Recurs inicial incorrecte: " + tipus);
		}
	}

	private void actualitzarHexagonPerProduccio(int idHexagon) {
		try (Session session = sessionFactory.openSession()) {
			session.beginTransaction();
			Hexagon hexagon = session.find(Hexagon.class, idHexagon);
			hexagon.setTerreny(Terreny.BOSC);
			hexagon.setNumero(8);
			session.getTransaction().commit();
		}
	}

	private void actualitzarTirada(int idPartida, boolean tiradaFeta) {
		try (Session session = sessionFactory.openSession()) {
			session.beginTransaction();
			Partida partida = session.find(Partida.class, idPartida);
			partida.setDausTirats(tiradaFeta);
			session.getTransaction().commit();
		}
	}

	private void actualitzarPuntsPerGuanyar(int idPartida, int punts) {
		try (Session session = sessionFactory.openSession()) {
			session.beginTransaction();
			session.find(Partida.class, idPartida).setPuntsPerGuanyar(punts);
			session.getTransaction().commit();
		}
	}

	private void omplirRecursos(int idJugador, int quantitat) {
		try (Session session = sessionFactory.openSession()) {
			session.beginTransaction();
			for (TipusRecurs tipus : TipusRecurs.values()) {
				RecursJugador recurs = session.createQuery("""
						SELECT r FROM RecursJugador r
						WHERE r.jugador.id = :idJugador AND r.tipus = :tipus
						""", RecursJugador.class)
						.setParameter("idJugador", idJugador)
						.setParameter("tipus", tipus)
						.getSingleResult();
				recurs.setQuantitat(quantitat);
			}
			session.getTransaction().commit();
		}
	}

	private void actualitzarRecurs(int idJugador, TipusRecurs tipus, int quantitat) {
		try (Session session = sessionFactory.openSession()) {
			session.beginTransaction();
			RecursJugador recurs = session.createQuery("""
					SELECT r FROM RecursJugador r
					WHERE r.jugador.id = :idJugador AND r.tipus = :tipus
					""", RecursJugador.class)
					.setParameter("idJugador", idJugador)
					.setParameter("tipus", tipus)
					.getSingleResult();
			recurs.setQuantitat(quantitat);
			session.getTransaction().commit();
		}
	}

	private int obtenirQuantitatRecurs(int idJugador, TipusRecurs tipus) {
		try (Session session = sessionFactory.openSession()) {
			return session.createQuery("""
					SELECT r.quantitat FROM RecursJugador r
					WHERE r.jugador.id = :idJugador AND r.tipus = :tipus
					""", Integer.class)
					.setParameter("idJugador", idJugador)
					.setParameter("tipus", tipus)
					.getSingleResult();
		}
	}

	private List<Integer> obtenirVertexsDelJugador(int idJugador) {
		try (Session session = sessionFactory.openSession()) {
			return session.createQuery("""
					SELECT a.vertex.id FROM Assentament a
					WHERE a.propietari.id = :idJugador ORDER BY a.vertex.codi
					""", Integer.class).setParameter("idJugador", idJugador).getResultList();
		}
	}

	private List<Hexagon> obtenirHexagonsAdjacents(int idPartida, int idVertex) {
		try (Session session = sessionFactory.openSession()) {
			return session.createQuery("""
					SELECT hv.hexagon FROM HexagonVertex hv
					WHERE hv.vertex.id = :idVertex AND hv.hexagon.partida.id = :idPartida
					ORDER BY hv.hexagon.codi
					""", Hexagon.class)
					.setParameter("idVertex", idVertex)
					.setParameter("idPartida", idPartida)
					.getResultList();
		}
	}

	private List<Vertex> obtenirVertexs(int idPartida) {
		try (Session session = sessionFactory.openSession()) {
			return session.createQuery("""
					SELECT v FROM Vertex v WHERE v.partida.id = :idPartida ORDER BY v.codi
					""", Vertex.class).setParameter("idPartida", idPartida).getResultList();
		}
	}

	private List<Aresta> obtenirArestes(int idPartida) {
		try (Session session = sessionFactory.openSession()) {
			return session.createQuery("""
					SELECT a FROM Aresta a WHERE a.partida.id = :idPartida ORDER BY a.codi
					""", Aresta.class).setParameter("idPartida", idPartida).getResultList();
		}
	}

	private Set<Integer> obtenirVertexsOcupats(int idPartida) {
		try (Session session = sessionFactory.openSession()) {
			List<Integer> ids = session.createQuery("""
					SELECT a.vertex.id FROM Assentament a
					WHERE a.propietari.partida.id = :idPartida
					""", Integer.class).setParameter("idPartida", idPartida).getResultList();
			return new HashSet<>(ids);
		}
	}

	private List<Jugador> obtenirJugadors(int idPartida) {
		try (Session session = sessionFactory.openSession()) {
			return session.createQuery("""
					SELECT j FROM Jugador j WHERE j.partida.id = :idPartida ORDER BY j.ordreTorn
					""", Jugador.class).setParameter("idPartida", idPartida).getResultList();
		}
	}

	private Partida carregarPartida(int idPartida) {
		try (Session session = sessionFactory.openSession()) {
			Partida partida = session.find(Partida.class, idPartida);
			assertNotNull(partida);
			return partida;
		}
	}

	private Partida obtenirPrimeraPartida() {
		try (Session session = sessionFactory.openSession()) {
			return session.createQuery("SELECT p FROM Partida p", Partida.class).getSingleResult();
		}
	}

	private int comptar(String entitat, int idPartida) {
		try (Session session = sessionFactory.openSession()) {
			return Math.toIntExact(session.createQuery(
					"SELECT COUNT(e) FROM " + entitat + " e WHERE e.partida.id = :idPartida", Long.class)
					.setParameter("idPartida", idPartida).getSingleResult());
		}
	}

	private int comptarRelacionsHexagonVertex(int idPartida) {
		try (Session session = sessionFactory.openSession()) {
			return Math.toIntExact(session.createQuery("""
					SELECT COUNT(hv) FROM HexagonVertex hv
					WHERE hv.hexagon.partida.id = :idPartida
					""", Long.class).setParameter("idPartida", idPartida).getSingleResult());
		}
	}

	private int comptarCartes(int idPartida) {
		try (Session session = sessionFactory.openSession()) {
			return Math.toIntExact(session.createQuery("""
					SELECT COUNT(c) FROM CartaDesenvolupament c WHERE c.partida.id = :idPartida
					""", Long.class).setParameter("idPartida", idPartida).getSingleResult());
		}
	}

	private int comptarDeserts(int idPartida) {
		try (Session session = sessionFactory.openSession()) {
			return Math.toIntExact(session.createQuery("""
					SELECT COUNT(h) FROM Hexagon h
					WHERE h.partida.id = :idPartida AND h.terreny = :terreny
					""", Long.class)
					.setParameter("idPartida", idPartida)
					.setParameter("terreny", Terreny.DESERT)
					.getSingleResult());
		}
	}

	private int comptarHexagonsAmbNumero(int idPartida) {
		try (Session session = sessionFactory.openSession()) {
			return Math.toIntExact(session.createQuery("""
					SELECT COUNT(h) FROM Hexagon h
					WHERE h.partida.id = :idPartida AND h.numero IS NOT NULL
					""", Long.class).setParameter("idPartida", idPartida).getSingleResult());
		}
	}

	private int comptarRecursos(int idPartida) {
		try (Session session = sessionFactory.openSession()) {
			return Math.toIntExact(session.createQuery("""
					SELECT COUNT(r) FROM RecursJugador r
					WHERE r.jugador.partida.id = :idPartida
					""", Long.class).setParameter("idPartida", idPartida).getSingleResult());
		}
	}

	private int comptarTotesLesPartides() {
		try (Session session = sessionFactory.openSession()) {
			return Math.toIntExact(session.createQuery("SELECT COUNT(p) FROM Partida p", Long.class).getSingleResult());
		}
	}

	private int comptarPoblatsEnVertex(int idVertex) {
		try (Session session = sessionFactory.openSession()) {
			return Math.toIntExact(session.createQuery("""
					SELECT COUNT(p) FROM Poblat p WHERE p.vertex.id = :idVertex
					""", Long.class).setParameter("idVertex", idVertex).getSingleResult());
		}
	}

	private int comptarCarreteresEnAresta(int idAresta) {
		try (Session session = sessionFactory.openSession()) {
			return Math.toIntExact(session.createQuery("""
					SELECT COUNT(c) FROM Carretera c WHERE c.aresta.id = :idAresta
					""", Long.class).setParameter("idAresta", idAresta).getSingleResult());
		}
	}

	private int comptarCiutats(int idJugador) {
		try (Session session = sessionFactory.openSession()) {
			return Math.toIntExact(session.createQuery("""
					SELECT COUNT(c) FROM Ciutat c WHERE c.propietari.id = :idJugador
					""", Long.class).setParameter("idJugador", idJugador).getSingleResult());
		}
	}
}
