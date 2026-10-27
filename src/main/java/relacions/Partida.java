package relacions;

import java.util.ArrayList;
import java.util.Date;
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

	@Column(name="estat",nullable = false)
	@Enumerated(EnumType.STRING)
	private EstatPartida estat;
	
	@Column(name="data_inici",nullable = false)
	//falta algo mas en date @
	private Date dataInici;

	@Column(name = "ronda", nullable = false)
	private int ronda;

	@Column(name = "ultima_tirada", nullable = true)
	private Integer ultimaTirada;

	@Column(name = "punts_per_guanyar", nullable = false)
	private Integer puntsPerGuanyar;

	@Column(name = "daus_tirats", nullable = false)
	private Boolean dausTirats;

	@Column(name = "lladre_pendent", nullable = false)
	private Boolean lladrePendent;

	
	// Relacions amb jugador //
	
	@OneToOne
	@JoinColumn(name = "jugador_actiu_id", nullable = true)
	//Falta relacion 1:1
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
	
	@OneToMany(mappedBy = "partida", cascade = CascadeType.ALL,orphanRemoval = true)
	private List<Jugador> jugadors = new ArrayList<>();
	
	
	
	// Relacions amb cartes desenvolupament //
	@OneToMany(mappedBy = "partida", cascade = CascadeType.ALL,orphanRemoval = true)
	private List<CartaDesenvolupament> cartes = new ArrayList<>();

	
	
	
	
	// Constructor sense parametres //

	public Partida() {
		
	}
	
	
	

	// Constructor amb parametres //

	public Partida(Date dataInici, int ronda, Integer ultimaTirada, Integer puntsPerGuanyar, Boolean dausTirats,
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

	// Getters / Setters

	public Integer getId() {
		return id;
	}

	public EstatPartida getEstat() {
		return estat;
	}

	public void setEstat(EstatPartida estat) {
		this.estat = estat;
	}

	public int getRonda() {
		return ronda;
	}

	public void setRonda(int ronda) {
		this.ronda = ronda;
	}

	public Integer getUltimaTirada() {
		return ultimaTirada;
	}

	public void setUltimaTirada(Integer ultimaTirada) {
		this.ultimaTirada = ultimaTirada;
	}

	public Integer getPuntsPerGuanyar() {
		return puntsPerGuanyar;
	}
	
	
	//Metodo para añadir los puntos que nosotros queramos para ganar
	
	 public void setPuntsPerGuanyar(Integer puntsPerGuanyar) {

	        if (puntsPerGuanyar == null || 
	        		puntsPerGuanyar < 1) {
	            throw new IllegalArgumentException(
	                "Els punts per guanyar han de ser superiors a zero"
	            );
	        }

	        this.puntsPerGuanyar = puntsPerGuanyar;
	    }

	public Boolean getDausTirats() {
		return dausTirats;
	}

	public void setDausTirats(Boolean dausTirats) {
		this.dausTirats = dausTirats;
	}

	public Boolean getLladrePendent() {
		return lladrePendent;
	}

	public void setLladrePendent(Boolean lladrePendent) {
		this.lladrePendent = lladrePendent;
	}

	public Jugador getJugadorActiu() {
		return jugadorActiu;
	}

	public void setJugadorActiu(Jugador jugadorActiu) {
		this.jugadorActiu = jugadorActiu;
	}

	public Jugador getGuanyador() {
		return guanyador;
	}

	public void setGuanyador(Jugador guanyador) {
		this.guanyador = guanyador;
	}
	
	
	// EQUALS //
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Partida other)) return false;
		return getId() != null && getId().equals(other.getId());
	}
	
	
	// HASHCODE /

	@Override
	public int hashCode() {
		return Partida.class.hashCode();
	}

}
