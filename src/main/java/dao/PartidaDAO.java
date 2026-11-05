package dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import herencia.Assentament;
import relacions.Partida;
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
			Partida partida = new Partida(new Date(), 1, null, 10, false, false, null, null);
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

		// 1. Cargar vértices
		Map<Integer, Vertex> vertexs = carregarVertexs(session, partida);

		// 2. Cargar hexágonos
		Map<Integer, Hexagon> hexagons = carregarHexagons(session, partida);

		// 3. Cargar relaciones hexágono-vértice
		int totalRelacions = carregarHexagonVertex(session, hexagons, vertexs);

		// 4. Cargar aristas
		int totalArestes = carregarArestes(session, vertexs, partida);

		// Comprobar cantidades
		if (vertexs.size() != 54 || hexagons.size() != 19 || totalRelacions != 114 || totalArestes != 72) {

			throw new IllegalStateException("Les dades del tauler no tenen les quantitats esperades. " + "Vèrtexs="
					+ vertexs.size() + ", hexàgons=" + hexagons.size() + ", relacions=" + totalRelacions + ", arestes="
					+ totalArestes);
		}

		return vertexs.size() + hexagons.size() + totalRelacions + totalArestes;
	}
	
	// METODE carregarVertexs() //

	private Map<String, Vertex> carregarVertexs(Session session, Partida partida) {

		Map<String, Vertex> vertexs = new HashMap<>();

		for (String[] c : llegirCsv("vertexs.csv")) {

			Vertex v = new Vertex(Integer.valueOf(c[0]), partida);

			session.persist(v);
			vertexs.put(c[0], v);
		}

		return vertexs;
	}
	
	// METODE carregarHexagons() //

	private Map<String, Hexagon> carregarHexagons(Session session, Partida partida) {
		Map<String, Hexagon> hexagons = new HashMap<>();
		for (String[] c : llegirCsv("hexagons.csv")) {
			Hexagon h = new Hexagon(Integer.valueOf(c[0]), Integer.valueOf(c[1]), Integer.valueOf(c[2]), null, null,
					partida);
			session.persist(h);
			hexagons.put(c[0], h);
		}
		return hexagons;
	}

	// METODE carregarHexagonVertex() //
	
	private int carregarHexagonVertex(Session session, Map<String, Hexagon> hexagons, Map<String, Vertex> vertexs) {
		int total = 0;
		for (String[] c : llegirCsv("hexagon_vertex.csv")) {
			Hexagon hexagon = obtenir(hexagons, c[0], "hexagon_vertex.csv");
			Vertex vertex = obtenir(vertexs, c[1], "hexagon_vertex.csv");
			int posicio = Integer.parseInt(c[2]);
			HexagonVertex hv = new HexagonVertex(hexagon, vertex, posicio);
			session.persist(hv);
			total++;
		}
		return total;
	}
	
	// METODE carregarArestes() //

	private int carregarArestes(Session session, Map<String, Vertex> vertexs, Partida partida) {
		int total = 0;
		for (String[] c : llegirCsv("arestes.csv")) {
			Vertex vertexA = obtenir(vertexs, c[1], "arestes.csv");
			Vertex vertexB = obtenir(vertexs, c[2], "arestes.csv");
			Aresta a = new Aresta(Integer.valueOf(c[0]), partida, vertexA, vertexB);
			session.persist(a);
			total++;
		}
		return total;
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
		// TODO Auto-generated method stub

	}

	@Override
	public void TirarDaus(int idPartida) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ComprovarVictoria(int idPartida) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ExercitMesGran(int idPartida) {
		// TODO Auto-generated method stub

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

	private <E> E obtenir(Map<String, E> mapa, String clau, String fitxer) {
		E e = mapa.get(clau);
		if (e == null) {
			throw new IllegalStateException(fitxer + ": no existeix '" + clau + "'");
		}
		return e;
	}

}
