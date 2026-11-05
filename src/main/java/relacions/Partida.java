package relacions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import enumeration.EstatPartida;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import taulell.Aresta;
import taulell.Hexagon;
import taulell.Vertex;

@Entity
@Table(name = "partida")

public class Partida {

	// Atributs //

	@Id
	@Column(name = "id", unique = true, nullable = false, length = 80)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotNull
	@Column(name = "estat", nullable = false)
	@Enumerated(EnumType.STRING)
	private EstatPartida estat;

	@NotNull
	@Column(name = "data_inici", nullable = false)
	private LocalDateTime dataInici;

	@Min(1)
	@Column(name = "ronda", nullable = false)
	private int ronda;

	@Min(2)
	@Max(12)
	@Column(name = "ultima_tirada", nullable = true)
	private Integer ultimaTirada;

	@NotNull
	@Min(1)
	@Column(name = "punts_per_guanyar", nullable = false)
	private int puntsPerGuanyar;

	@NotNull
	@Column(name = "daus_tirats", nullable = false)
	private Boolean dausTirats;

	@NotNull
	@Column(name = "lladre_pendent", nullable = false)
	private Boolean lladrePendent;

	// Relacions amb jugador //

	@OneToOne
	@JoinColumn(name = "jugador_actiu_id", nullable = true)
	private Jugador jugadorActiu;

	@ManyToOne
	@JoinColumn(name = "guanyador_id", nullable = true)
	private Jugador guanyador;

	// Relacions amb lladre //

	@ManyToOne
	@JoinColumn(name = "hexagon_lladre_id", nullable = true)
	private Hexagon hexagonLladre;

	// Relacions amb taulell //

	@OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Hexagon> hexagons = new ArrayList<>();

	@OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Vertex> vertexs = new ArrayList<>();

	@OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Aresta> arestes = new ArrayList<>();

	// Relacions amb els jugadors //

	@OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Jugador> jugadors = new ArrayList<>();

	// Relacions amb cartes desenvolupament //
	@OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CartaDesenvolupament> cartes = new ArrayList<>();

	// Constructor sense parametres //

	public Partida() {

	}

	// Constructor amb parametres //

	public Partida(LocalDateTime dataInici, int ronda, Integer ultimaTirada, Integer puntsPerGuanyar, Boolean dausTirats,
			Boolean lladrePendent, Jugador jugadorActiu, Jugador guanyador) {
		super();
		this.estat = EstatPartida.PREPARACIO;
		this.dataInici = dataInici;
		this.ronda = ronda;
		this.ultimaTirada = ultimaTirada;
		this.puntsPerGuanyar = puntsPerGuanyar;
		this.dausTirats = dausTirats;
		this.lladrePendent = lladrePendent;
		this.jugadorActiu = jugadorActiu;
		this.guanyador = guanyador;

	}

	// Getters / Setters //

	public Integer getId() {
		return id;
	}

	public EstatPartida getEstat() {
		return estat;
	}

	public void setEstat(EstatPartida estat) {
		if (estat == null) {
			throw new IllegalArgumentException("L'estat de la partida no pot ser null");
		}
		this.estat = estat;
	}

	public int getRonda() {
		return ronda;
	}

	public void setRonda(int ronda) {
		if (ronda < 1) {
			throw new IllegalArgumentException("La ronda ha de ser superior a zero");
		}
		this.ronda = ronda;
	}

	public LocalDateTime getDataInici() {
		return dataInici;
	}

	public void setDataInici(LocalDateTime dataInici) {
		if (dataInici == null) {
			throw new IllegalArgumentException("La data d'inici no pot ser null");
		}
		this.dataInici = dataInici;
	}

	public Integer getUltimaTirada() {
		return ultimaTirada;
	}

	public void setUltimaTirada(Integer ultimaTirada) {
		if (ultimaTirada != null && (ultimaTirada < 2 || ultimaTirada > 12)) {
			throw new IllegalArgumentException("La tirada ha d'estar entre 2 i 12");
		}
		this.ultimaTirada = ultimaTirada;
	}

	public Integer getPuntsPerGuanyar() {
		return puntsPerGuanyar;
	}

	// Metodo para añadir los puntos que nosotros queramos para ganar

	public void setPuntsPerGuanyar(Integer puntsPerGuanyar) {

		if (puntsPerGuanyar == null || puntsPerGuanyar < 1) {
			throw new IllegalArgumentException("Els punts per guanyar han de ser superiors a zero");
		}

		this.puntsPerGuanyar = puntsPerGuanyar;
	}

	public Boolean getDausTirats() {
		return dausTirats;
	}

	public void setDausTirats(Boolean dausTirats) {
		if (dausTirats == null) {
			throw new IllegalArgumentException("El valor de dausTirats no pot ser null");
		}
		this.dausTirats = dausTirats;
	}

	public Boolean getLladrePendent() {
		return lladrePendent;
	}

	public void setLladrePendent(Boolean lladrePendent) {
		if (lladrePendent == null) {
			throw new IllegalArgumentException("El valor de lladrePendent no pot ser null");
		}
		this.lladrePendent = lladrePendent;
	}

	public Jugador getJugadorActiu() {
		return jugadorActiu;
	}

	public void setJugadorActiu(Jugador jugadorActiu) {
		if (jugadorActiu != null && !pertanyAquestaPartida(jugadorActiu.getPartida())) {
			throw new IllegalArgumentException("El jugador actiu ha de pertànyer a aquesta partida");
		}
		this.jugadorActiu = jugadorActiu;
	}

	public Jugador getGuanyador() {
		return guanyador;
	}

	public void setGuanyador(Jugador guanyador) {
		if (guanyador != null && !pertanyAquestaPartida(guanyador.getPartida())) {
			throw new IllegalArgumentException("El guanyador ha de pertànyer a aquesta partida");
		}
		this.guanyador = guanyador;
	}


	public List<Hexagon> getHexagons() {
		return hexagons;
	}

	public void setHexagons(List<Hexagon> hexagons) {
		if (hexagons == null) {
			throw new IllegalArgumentException("La llista d'hexàgons no pot ser null");
		}
		for (Hexagon hexagon : hexagons) {
			if (hexagon == null || !pertanyAquestaPartida(hexagon.getPartida())) {
				throw new IllegalArgumentException("Tots els hexàgons han de pertànyer a aquesta partida");
			}
		}
		this.hexagons = new ArrayList<>(hexagons);
	}

	public List<Vertex> getVertexs() {
		return vertexs;
	}

	public void setVertexs(List<Vertex> vertexs) {
		if (vertexs == null) {
			throw new IllegalArgumentException("La llista de vèrtexs no pot ser null");
		}
		for (Vertex vertex : vertexs) {
			if (vertex == null || !pertanyAquestaPartida(vertex.getPartida())) {
				throw new IllegalArgumentException("Tots els vèrtexs han de pertànyer a aquesta partida");
			}
		}
		this.vertexs = new ArrayList<>(vertexs);
	}

	public List<Aresta> getArestes() {
		return arestes;
	}

	public Hexagon getHexagonLladre() {
		return hexagonLladre;
	}

	public void setHexagonLladre(Hexagon hexagonLladre) {
		if (hexagonLladre != null && !pertanyAquestaPartida(hexagonLladre.getPartida())) {
			throw new IllegalArgumentException("L'hexàgon del lladre ha de pertànyer a aquesta partida");
		}
		this.hexagonLladre = hexagonLladre;
	}

	public void setArestes(List<Aresta> arestes) {
		if (arestes == null) {
			throw new IllegalArgumentException("La llista d'arestes no pot ser null");
		}
		for (Aresta aresta : arestes) {
			if (aresta == null || !pertanyAquestaPartida(aresta.getPartida())) {
				throw new IllegalArgumentException("Totes les arestes han de pertànyer a aquesta partida");
			}
		}
		this.arestes = new ArrayList<>(arestes);
	}

	// Metodo afegir vertex i saber en todo momento a que partida pertenece

	public void afegirVertex(Vertex vertex) {
		if (vertex == null) {
			throw new IllegalArgumentException("El vèrtex no pot ser null");
		}
		if (!pertanyAquestaPartida(vertex.getPartida())) {
			throw new IllegalArgumentException("El vèrtex ha de pertànyer a aquesta partida");
		}
		// Establim la relació en els dos sentits:
		if (!this.vertexs.contains(vertex)) {
			this.vertexs.add(vertex);
		}
	}

	public void afegirJugador(Jugador jugador) {
		if (jugador == null) {
			throw new IllegalArgumentException("El jugador no pot ser null");
		}
		if (jugador.getPartida() != null && !pertanyAquestaPartida(jugador.getPartida())) {
			throw new IllegalArgumentException("El jugador ja pertany a una altra partida");
		}
		jugador.setPartida(this);
		if (!this.jugadors.contains(jugador)) {
			this.jugadors.add(jugador);
		}
	}

	public void afegirHexagon(Hexagon hexagon) {
		if (hexagon == null || !pertanyAquestaPartida(hexagon.getPartida())) {
			throw new IllegalArgumentException("L'hexàgon ha de pertànyer a aquesta partida");
		}
		if (!this.hexagons.contains(hexagon)) {
			this.hexagons.add(hexagon);
		}
	}

	// Metodo afegir aresta i saber en todo momento a que partida pertenece

	public void afegirAresta(Aresta aresta) {
		if (aresta == null) {
			throw new IllegalArgumentException("L'aresta no pot ser null");
		}
		if (!pertanyAquestaPartida(aresta.getPartida())) {
			throw new IllegalArgumentException("L'aresta ha de pertànyer a aquesta partida");
		}
		if (!this.arestes.contains(aresta)) {
			this.arestes.add(aresta);
		}
	}

	private boolean pertanyAquestaPartida(Partida altraPartida) {
		return altraPartida == this || (id != null && id.equals(altraPartida == null ? null : altraPartida.getId()));
	}
	

	// EQUALS //

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Partida other))
			return false;
		return getId() != null && getId().equals(other.getId());
	}

	// HASHCODE /

	@Override
	public int hashCode() {
		return Partida.class.hashCode();
	}

}
