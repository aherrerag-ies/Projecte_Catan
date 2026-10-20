package model_joc;

import java.util.ArrayList;
import java.util.List;

import enumeration.EstatPartida;
import taulell.Aresta;
import taulell.Hexagon;
import taulell.Vertex;

public class Partida {
	
	private Long id;
	private EstatPartida estat;
	private Integer ronda;
	private Integer ultimaTirada;
	private Integer puntsPerGuanyar;
	private Boolean dausTirats;
	private Boolean lladrePendent;

	private Jugador jugadorActiu;
	private Jugador guanyador;
	private Hexagon hexagonLladre;

	private List<Jugador> jugadors = new ArrayList<>();
	private List<Hexagon> hexagons = new ArrayList<>();
	private List<Vertex> vertexs = new ArrayList<>();
	private List<Aresta> arestes = new ArrayList<>();
	private List<CartaDesenvolupament> cartes = new ArrayList<>();

}
