package dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import relacions.Partida;
import java.util.Date;
import java.util.HashMap;

import taulell.Aresta;
import taulell.Hexagon;
import taulell.HexagonVertex;
import taulell.Vertex;

public class PartidaDAO extends GenericDAO<Partida, Integer> implements IPartidaDAO{

	private static final String CARPETA_CSV = "/csv/";
	
	@Override
	public int carregarDades() {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			int total = carregarDades(session);
			session.getTransaction().commit();
			return total;
		} catch (RuntimeException e) {
			// Qualsevol línia errònia -> rollback de TOTA la càrrega
			handleException(session, e);
			return -1;
		}
	}
	
	private int carregarDades(Session session, Partida partida) {

	    // 1. Entitats independents del taulell
	    Map<String, Vertex> vertexs = carregarVertexs(session, partida);

	    Map<String, Hexagon> hexagons = carregarHexagons(session, partida);
	    // 2. Relacions entre les entitats
	    int totalRelacions =
	            carregarHexagonVertex(session, hexagons, vertexs);
	    // 3. Arestes que connecten als vèrtexs
	    int totalArestes =
	            carregarArestes(session, vertexs, partida);
	    // Comprovem que el taulell tingui les dimensions esperades.
	    if (vertexs.size() != 54
	            || hexagons.size() != 19
	            || totalRelacions != 114
	            || totalArestes != 72) {
	        throw new IllegalStateException(
	            "Les dades del tauler no tenen les quantitats esperades."
	        );
	    }
	    return vertexs.size()
	            + hexagons.size()
	            + totalRelacions
	            + totalArestes;
	} 

	private Map<String, Vertex> carregarVertexs(Session session, Partida partida) {

	    Map<String, Vertex> vertexs = new HashMap<>();

	    for (String[] c : llegirCsv("vertexs.csv")) {

	        Vertex v = new Vertex(
	            Integer.valueOf(c[0]),
	            Double.valueOf(c[1]),
	            Double.valueOf(c[2]),
	            partida
	        );

	        session.persist(v);
	        vertexs.put(c[0], v);
	    }

	    return vertexs;
	} 
	
	private Map<String, Hexagon> carregarHexagons(Session session, Partida partida) {
	    Map<String, Hexagon> hexagons = new HashMap<>();
	    for (String[] c : llegirCsv("hexagons.csv")) {
	        Hexagon h = new Hexagon(
	            Integer.valueOf(c[0]),
	            Integer.valueOf(c[1]),
	            Integer.valueOf(c[2]),
	            null,
	            null,
	            partida
	        );
	        session.persist(h);
	        hexagons.put(c[0], h);
	    }
	    return hexagons;
	}
	
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
	
	private int carregarArestes(Session session, Map<String, Vertex> vertexs, Partida partida) {
	    int total = 0;
	    for (String[] c : llegirCsv("arestes.csv")) {
	        Vertex vertexA = obtenir(vertexs, c[1], "arestes.csv");
	        Vertex vertexB = obtenir(vertexs, c[2], "arestes.csv");
	        Aresta a = new Aresta(
	            Integer.valueOf(c[0]),
	            partida,
	            vertexA,
	            vertexB
	        );
	        session.persist(a);
	        total++;
	    }
	    return total;
	}
	
	@Override
	public int IniciarPartida(List<String> nomsJugadors) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void MostrarTauler(int idPartida) {
		// TODO Auto-generated method stub
		
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
