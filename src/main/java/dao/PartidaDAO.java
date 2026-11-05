package dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hibernate.Session;

import enumeration.ColorJugador;
import enumeration.EstatPartida;
import enumeration.Terreny;
import enumeration.TipusCartaDesenvolupament;
import enumeration.TipusRecurs;
import herencia.Assentament;
import herencia.Ciutat;
import relacions.Jugador;
import relacions.Partida;
import relacions.CartaDesenvolupament;
import relacions.RecursJugador;
import taulell.Aresta;
import taulell.Hexagon;
import taulell.HexagonVertex;
import taulell.Vertex;

public class PartidaDAO extends GenericDAO<Partida, Integer> implements IPartidaDAO {

	private static final String CARPETA_CSV = "/csv/";

	@Override
	public int carregarDades() {

		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			// Crear una partida para asociar el tablero.
			Partida partida = new Partida(LocalDateTime.now(), 1, null, 10, false, false, null, null);
			session.persist(partida);
			// Cargar el tablero para esta partida.
			int total = carregarDades(session, partida);
			session.getTransaction().commit();
			return total;
		} catch (RuntimeException e) {
			handleException(session, e);
			return -1;
		}
	}

	private int carregarDades(Session session, Partida partida) {

		Map<Integer, Vertex> vertexs = carregarVertexs(session, partida);
		Map<Integer, Hexagon> hexagons = carregarHexagons(session, partida);
		int totalRelacions = carregarHexagonVertex(session, hexagons, vertexs);
		int totalArestes = carregarArestes(session, vertexs, partida);

		if (vertexs.size() != 54 || hexagons.size() != 19 || totalRelacions != 114 || totalArestes != 72) {
			throw new IllegalStateException("Les dades del tauler no tenen les quantitats esperades. " + "Vèrtexs="
					+ vertexs.size() + ", hexàgons=" + hexagons.size() + ", relacions=" + totalRelacions + ", arestes="
					+ totalArestes);
		}

		return vertexs.size() + hexagons.size() + totalRelacions + totalArestes;
	}
	
	// METODE carregarVertexs() //

	private Map<Integer, Vertex> carregarVertexs(Session session, Partida partida) {

		Map<Integer, Vertex> vertexs = new HashMap<>();

		for (String[] c : llegirCsv("vertexs.csv")) {
			Integer codi = Integer.valueOf(c[0]);
			Vertex vertex = new Vertex(codi, partida);
			partida.afegirVertex(vertex);
			session.persist(vertex);
			vertexs.put(codi, vertex);
		}

		return vertexs;
	}
	
	// METODE carregarHexagons() //

	private Map<Integer, Hexagon> carregarHexagons(Session session, Partida partida) {
		Map<Integer, Hexagon> hexagons = new HashMap<>();
		for (String[] c : llegirCsv("hexagons.csv")) {
			Integer codi = Integer.valueOf(c[0]);
			Hexagon hexagon = new Hexagon(codi, Integer.valueOf(c[1]), Integer.valueOf(c[2]), null, null, partida);
			partida.afegirHexagon(hexagon);
			session.persist(hexagon);
			hexagons.put(codi, hexagon);
		}
		return hexagons;
	}

	// METODE carregarHexagonVertex() //
	
	private int carregarHexagonVertex(Session session, Map<Integer, Hexagon> hexagons,
			Map<Integer, Vertex> vertexs) {
		int total = 0;
		for (String[] c : llegirCsv("hexagon_vertex.csv")) {
			Hexagon hexagon = obtenir(hexagons, Integer.valueOf(c[0]), "hexagon_vertex.csv");
			Vertex vertex = obtenir(vertexs, Integer.valueOf(c[1]), "hexagon_vertex.csv");
			int posicio = Integer.parseInt(c[2]);
			HexagonVertex hv = new HexagonVertex(hexagon, vertex, posicio);
			session.persist(hv);
			total++;
		}
		return total;
	}
	
	// METODE carregarArestes() //

	private int carregarArestes(Session session, Map<Integer, Vertex> vertexs, Partida partida) {
		int total = 0;
		for (String[] c : llegirCsv("arestes.csv")) {
			Vertex vertexA = obtenir(vertexs, Integer.valueOf(c[1]), "arestes.csv");
			Vertex vertexB = obtenir(vertexs, Integer.valueOf(c[2]), "arestes.csv");
			Aresta aresta = new Aresta(Integer.valueOf(c[0]), partida, vertexA, vertexB);
			partida.afegirAresta(aresta);
			session.persist(aresta);
			total++;
		}
		return total;
	}

	private int iniciarPartida(Session session, List<String> nomsJugadors) {
		if (nomsJugadors == null || nomsJugadors.size() < 2 || nomsJugadors.size() > 4) {
			throw new IllegalArgumentException("La partida ha de tenir entre 2 i 4 jugadors");
		}
		for (String nom : nomsJugadors) {
			if (nom == null || nom.trim().isEmpty() || nom.length() > 50) {
				throw new IllegalArgumentException("Cada jugador ha de tenir un nom d'entre 1 i 50 caràcters");
			}
		}

		Partida partida = new Partida(LocalDateTime.now(), 1, null, 10, false, false, null, null);
		session.persist(partida);
		carregarDades(session, partida);

		List<Hexagon> hexagonsPartida = new ArrayList<>(partida.getHexagons());
		Collections.shuffle(hexagonsPartida);
		List<Terreny> terrenys = new ArrayList<>();
		afegirTerrenys(terrenys, Terreny.BOSC, 4);
		afegirTerrenys(terrenys, Terreny.PASTURES, 4);
		afegirTerrenys(terrenys, Terreny.CAMPS, 4);
		afegirTerrenys(terrenys, Terreny.TURONS, 3);
		afegirTerrenys(terrenys, Terreny.MUNTANYES, 3);
		afegirTerrenys(terrenys, Terreny.DESERT, 1);
		Collections.shuffle(terrenys);

		List<Integer> numeros = new ArrayList<>(
				java.util.Arrays.asList(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12));
		Collections.shuffle(numeros);

		int indexNumero = 0;
		for (int i = 0; i < hexagonsPartida.size(); i++) {
			Hexagon hexagon = hexagonsPartida.get(i);
			Terreny terreny = terrenys.get(i);
			hexagon.setTerreny(terreny);
			if (terreny == Terreny.DESERT) {
				hexagon.setNumero(null);
				partida.setHexagonLladre(hexagon);
			} else {
				hexagon.setNumero(numeros.get(indexNumero++));
			}
		}

		List<String> nomsBarrejats = new ArrayList<>(nomsJugadors);
		Collections.shuffle(nomsBarrejats);
		List<Jugador> jugadors = new ArrayList<>();
		ColorJugador[] colors = ColorJugador.values();
		for (int i = 0; i < nomsBarrejats.size(); i++) {
			Jugador jugador = new Jugador(nomsBarrejats.get(i), colors[i], i + 1, partida, 0);
			partida.afegirJugador(jugador);
			session.persist(jugador);
			jugadors.add(jugador);
		}

		partida.setJugadorActiu(jugadors.get(0));

		afegirCartesDesenvolupament(session, partida);
		return partida.getId();
	}

	private void afegirCartesDesenvolupament(Session session, Partida partida) {
		afegirCartes(session, partida, TipusCartaDesenvolupament.CAVALLER, 14);
		afegirCartes(session, partida, TipusCartaDesenvolupament.PUNT_VICTORIA, 5);
		afegirCartes(session, partida, TipusCartaDesenvolupament.MONOPOLI, 2);
	}

	private void afegirCartes(Session session, Partida partida, TipusCartaDesenvolupament tipus, int quantitat) {
		for (int i = 0; i < quantitat; i++) {
			session.persist(new CartaDesenvolupament(tipus, partida, null, null, false));
		}
	}

	private void afegirTerrenys(List<Terreny> terrenys, Terreny terreny, int quantitat) {
		for (int i = 0; i < quantitat; i++) {
			terrenys.add(terreny);
		}
	}

	// METODE INICIAR PARTIDA() //

	@Override
	public int IniciarPartida(List<String> nomsJugadors) {
		Session session = sessionFactory.getCurrentSession();

		try {
			session.beginTransaction();

			int idPartida = iniciarPartida(session, nomsJugadors);

			session.getTransaction().commit();
			return idPartida;

		} catch (RuntimeException e) {
			handleException(session, e);
			return -1;
		}
	}

	
	
	// METODE MOSTRAR TAULER() //

	@Override
	public void MostrarTauler(int idPartida) {

		Session session = sessionFactory.getCurrentSession();

		try {
			session.beginTransaction();

			String resultat = mostrarTauler(session, idPartida);

			session.getTransaction().commit();

			System.out.println(resultat);

		} catch (RuntimeException e) {
			handleException(session, e);

			System.out.println("Error en mostrar el tauler");
		}
	}

	// METODE AUXILIAR PRIVATE MOSTRAR TAULER() //

	private String mostrarTauler(Session session, int idPartida) {

		Partida partida = session.find(Partida.class, idPartida);

		if (partida == null) {
			return "No existeix cap partida amb l'ID: " + idPartida;
		}

		List<Hexagon> hexagons = session.createQuery("""
				SELECT h
				FROM Hexagon h
				WHERE h.partida.id = :idPartida
				ORDER BY h.codi
				""", Hexagon.class).setParameter("idPartida", idPartida).getResultList();

		StringBuilder text = new StringBuilder();

		text.append("=== TAULER DE LA PARTIDA ").append(idPartida).append(" ===").append(System.lineSeparator());

		for (Hexagon hexagon : hexagons) {

			boolean teLladre = partida.getHexagonLladre() != null
					&& partida.getHexagonLladre().getId().equals(hexagon.getId());

			text.append("Hexàgon ").append(hexagon.getCodi()).append(" | Terreny: ").append(hexagon.getTerreny())
					.append(" | Número: ").append(hexagon.getNumero()).append(" | Lladre: ")
					.append(teLladre ? "SÍ" : "NO").append(System.lineSeparator());

			List<Assentament> assentaments = session.createQuery("""
					SELECT a
					FROM Assentament a
					WHERE a.vertex.id IN (
					    SELECT hv.vertex.id
					    FROM HexagonVertex hv
					    WHERE hv.hexagon.id = :idHexagon
					)
					""", Assentament.class).setParameter("idHexagon", hexagon.getId()).getResultList();

			if (assentaments.isEmpty()) {
				text.append("  Sense construccions").append(System.lineSeparator());
			} else {
				for (Assentament assentament : assentaments) {

					text.append("  - ").append(assentament.getClass().getSimpleName()).append(" al vèrtex ")
							.append(assentament.getVertex().getId()).append(", propietari: ")
							.append(assentament.getPropietari().getNom()).append(System.lineSeparator());
				}
			}
		}

		return text.toString();
	}

	@Override
	public void MostrarPartida(int idPartida) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			Partida partida = session.find(Partida.class, idPartida);
			if (partida == null) {
				throw new IllegalArgumentException("No existeix cap partida amb l'ID: " + idPartida);
			}

			List<Jugador> jugadors = session.createQuery("""
					SELECT j
					FROM Jugador j
					WHERE j.partida.id = :idPartida
					ORDER BY j.ordreTorn
					""", Jugador.class).setParameter("idPartida", idPartida).getResultList();

			StringBuilder text = new StringBuilder();
			text.append("=== PARTIDA ").append(idPartida).append(" ===").append(System.lineSeparator())
					.append("Estat: ").append(partida.getEstat()).append(System.lineSeparator())
					.append("Ronda: ").append(partida.getRonda()).append(System.lineSeparator())
					.append("Jugador actiu: ")
					.append(partida.getJugadorActiu() == null ? "cap" : partida.getJugadorActiu().getNom())
					.append(System.lineSeparator())
					.append("Última tirada: ")
					.append(partida.getUltimaTirada() == null ? "cap" : partida.getUltimaTirada())
					.append(System.lineSeparator());

			for (Jugador jugador : jugadors) {
				Long poblats = session.createNamedQuery("Poblat.comptarPerJugador", Long.class)
						.setParameter("idJugador", jugador.getId()).getSingleResult();
				Long ciutats = session.createNamedQuery("Ciutat.comptarPerJugador", Long.class)
						.setParameter("idJugador", jugador.getId()).getSingleResult();
				Long carreteres = session.createQuery("""
						SELECT COUNT(c)
						FROM Carretera c
						WHERE c.propietari.id = :idJugador
						""", Long.class).setParameter("idJugador", jugador.getId()).getSingleResult();
				Long totalRecursos = session.createQuery("""
						SELECT COALESCE(SUM(r.quantitat), 0)
						FROM RecursJugador r
						WHERE r.jugador.id = :idJugador
						""", Long.class).setParameter("idJugador", jugador.getId()).getSingleResult();

				text.append("- ").append(jugador.getNom()).append(" (").append(jugador.getColor()).append(")")
						.append(": ").append(calcularPunts(session, jugador)).append(" punts, ")
						.append(poblats).append(" poblats, ").append(ciutats).append(" ciutats, ")
						.append(carreteres).append(" carreteres, ").append(totalRecursos)
						.append(" recursos totals").append(System.lineSeparator());
			}

			session.getTransaction().commit();
			System.out.println(text);
		} catch (RuntimeException e) {
			handleException(session, e);
			System.out.println("Error en mostrar la partida: " + e.getMessage());
		}
	}

	@Override
	public String TirarDaus(int idPartida) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			Partida partida = session.find(Partida.class, idPartida);
			if (partida == null) {
				throw new IllegalArgumentException("No existeix cap partida amb l'ID: " + idPartida);
			}
			if (partida.getEstat() != EstatPartida.EN_CURS) {
				throw new IllegalStateException("Només es poden tirar els daus en una partida EN_CURS");
			}
			if (partida.getJugadorActiu() == null) {
				throw new IllegalStateException("La partida no té cap jugador actiu");
			}
			if (Boolean.TRUE.equals(partida.getDausTirats())) {
				throw new IllegalStateException("El jugador actiu ja ha tirat els daus aquest torn");
			}
			Random aleatori = new Random();
			int dau1 = aleatori.nextInt(6) + 1;
			int dau2 = aleatori.nextInt(6) + 1;
			int tirada = dau1 + dau2;
			partida.setUltimaTirada(tirada);
			partida.setDausTirats(true);
			partida.setLladrePendent(false);

			StringBuilder resum = new StringBuilder("Tirada: ").append(dau1).append(" + ").append(dau2)
					.append(" = ").append(tirada).append(System.lineSeparator());
			if (tirada == 7) {
				resum.append("No hi ha producció en una tirada de 7. El moviment del lladre no forma part de l'abast.");
			} else {
				List<Jugador> jugadors = obtenirJugadors(session, idPartida);
				int[][] recursosRebuts = new int[jugadors.size()][TipusRecurs.values().length];
				List<Hexagon> hexagons = session.createQuery("""
						SELECT h
						FROM Hexagon h
						WHERE h.partida.id = :idPartida
						  AND h.numero = :numero
						ORDER BY h.codi
						""", Hexagon.class)
						.setParameter("idPartida", idPartida)
						.setParameter("numero", tirada)
						.getResultList();
				for (Hexagon hexagon : hexagons) {
					TipusRecurs tipusRecurs = obtenirRecurs(hexagon.getTerreny());
					if (tipusRecurs == null) {
						continue;
					}

					List<Assentament> assentaments = session.createQuery("""
							SELECT a
							FROM Assentament a
							WHERE a.vertex.id IN (
							    SELECT hv.vertex.id
							    FROM HexagonVertex hv
							    WHERE hv.hexagon.id = :idHexagon
							)
							""", Assentament.class).setParameter("idHexagon", hexagon.getId()).getResultList();
					for (Assentament assentament : assentaments) {
						int quantitat = assentament instanceof Ciutat ? 2 : 1;
						RecursJugador recurs = session.createQuery("""
								SELECT r
								FROM RecursJugador r
								WHERE r.jugador.id = :idJugador
								  AND r.tipus = :tipus
								""", RecursJugador.class)
								.setParameter("idJugador", assentament.getPropietari().getId())
								.setParameter("tipus", tipusRecurs).getSingleResult();
						recurs.sumarQuantitat(quantitat);
						int indexJugador = -1;
						for (int i = 0; i < jugadors.size(); i++) {
							if (jugadors.get(i).getId().equals(assentament.getPropietari().getId())) {
								indexJugador = i;
								break;
							}
						}
						if (indexJugador == -1) {
							throw new IllegalStateException("El propietari d'un assentament no pertany a la partida");
						}
						int indexRecurs = obtenirIndexRecurs(tipusRecurs);
						recursosRebuts[indexJugador][indexRecurs] += quantitat;
					}
				}

				for (int i = 0; i < jugadors.size(); i++) {
					resum.append(jugadors.get(i).getNom()).append(": ");
					boolean haRebutRecursos = false;
					for (int j = 0; j < TipusRecurs.values().length; j++) {
						if (recursosRebuts[i][j] > 0) {
							if (haRebutRecursos) {
								resum.append(", ");
							}
							resum.append(recursosRebuts[i][j]).append(" ").append(TipusRecurs.values()[j]);
							haRebutRecursos = true;
						}
					}
					if (!haRebutRecursos) {
						resum.append("cap recurs");
					}
					resum.append(System.lineSeparator());
				}
			}

			session.getTransaction().commit();
			System.out.println(resum);
			return resum.toString();
		} catch (RuntimeException e) {
			handleException(session, e);
			throw e;
		}
	}

	@Override
	public void ComprovarVictoria(int idPartida) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			Partida partida = session.find(Partida.class, idPartida);
			if (partida == null) {
				throw new IllegalArgumentException("No existeix cap partida amb l'ID: " + idPartida);
			}
			if (partida.getEstat() == EstatPartida.FINALITZADA) {
				session.getTransaction().commit();
				System.out.println("La partida ja està finalitzada.");
				return;
			}

			for (Jugador jugador : obtenirJugadors(session, idPartida)) {
				int punts = calcularPunts(session, jugador);
				if (punts >= partida.getPuntsPerGuanyar()) {
					partida.setGuanyador(jugador);
					partida.setEstat(EstatPartida.FINALITZADA);
					session.getTransaction().commit();
					System.out.println("Partida finalitzada. Ha guanyat " + jugador.getNom() + " amb " + punts
							+ " punts.");
					return;
				}
			}

			session.getTransaction().commit();
			System.out.println("Cap jugador ha arribat als " + partida.getPuntsPerGuanyar() + " punts necessaris.");
		} catch (RuntimeException e) {
			handleException(session, e);
			System.out.println("Error en comprovar la victòria: " + e.getMessage());
		}
	}

	@Override
	public void ExercitMesGran(int idPartida) {
		// Pendent d'implementar.
	}

	private List<Jugador> obtenirJugadors(Session session, int idPartida) {
		return session.createQuery("""
				SELECT j
				FROM Jugador j
				WHERE j.partida.id = :idPartida
				ORDER BY j.ordreTorn
				""", Jugador.class).setParameter("idPartida", idPartida).getResultList();
	}

	private int calcularPunts(Session session, Jugador jugador) {
		Long poblats = session.createNamedQuery("Poblat.comptarPerJugador", Long.class)
				.setParameter("idJugador", jugador.getId()).getSingleResult();
		Long ciutats = session.createNamedQuery("Ciutat.comptarPerJugador", Long.class)
				.setParameter("idJugador", jugador.getId()).getSingleResult();
		Long cartesPuntVictoria = session.createQuery("""
				SELECT COUNT(c)
				FROM CartaDesenvolupament c
				WHERE c.propietari.id = :idJugador
				  AND c.tipus = :tipus
				  AND c.usada = false
				""", Long.class).setParameter("idJugador", jugador.getId())
				.setParameter("tipus", TipusCartaDesenvolupament.PUNT_VICTORIA).getSingleResult();
		return Math.toIntExact(poblats + 2 * ciutats + cartesPuntVictoria);
	}

	private int obtenirIndexRecurs(TipusRecurs tipusRecurs) {
		switch (tipusRecurs) {
		case FUSTA:
			return 0;
		case MAO:
			return 1;
		case LLANA:
			return 2;
		case BLAT:
			return 3;
		case MINERAL:
			return 4;
		default:
			throw new IllegalArgumentException("Tipus de recurs no vàlid");
		}
	}

	static TipusRecurs obtenirRecurs(Terreny terreny) {
		if (terreny == null) {
			throw new IllegalStateException("Un hexàgon no té terreny assignat");
		}
		switch (terreny) {
		case BOSC:
			return TipusRecurs.FUSTA;
		case TURONS:
			return TipusRecurs.MAO;
		case PASTURES:
			return TipusRecurs.LLANA;
		case CAMPS:
			return TipusRecurs.BLAT;
		case MUNTANYES:
			return TipusRecurs.MINERAL;
		case DESERT:
			return null;
		default:
			throw new IllegalArgumentException("Terreny no vàlid");
		}
	}

	private List<String[]> llegirCsv(String fitxer) {
		InputStream is = PartidaDAO.class.getResourceAsStream(CARPETA_CSV + fitxer);
		if (is == null) {
			throw new IllegalStateException("No es troba el fitxer " + CARPETA_CSV + fitxer);
		}
		List<String[]> files = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			br.readLine(); // capçalera
			String linia;
			while ((linia = br.readLine()) != null) {
				if (linia.isBlank()) {
					continue;
				}
				String[] camps = linia.split(",", -1);
				for (int i = 0; i < camps.length; i++) {
					camps[i] = camps[i].trim();
				}
				files.add(camps);
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Error llegint " + fitxer, e);
		}
		return files;
	}

	private <E> E obtenir(Map<Integer, E> mapa, Integer clau, String fitxer) {
		E e = mapa.get(clau);
		if (e == null) {
			throw new IllegalStateException(fitxer + ": no existeix '" + clau + "'");
		}
		return e;
	}

}
