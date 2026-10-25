package relacions;

import java.util.ArrayList;
import java.util.List;

import enumeration.ColorJugador;
import enumeration.TipusRecurs;
import herencia.Construccio;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "jugador", uniqueConstraints = {
		@UniqueConstraint(name = "uk_jugador_partida_color", columnNames = { "partida_id", "color" }),
		@UniqueConstraint(name = "uk_jugador_partida_ordre", columnNames = { "partida_id", "ordre_torn" }) })
public class Jugador {

	// Atributs //

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotBlank
	@Size(max = 50)
	@Column(name = "nom", nullable = false, length = 50)
	private String nom;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "color", nullable = false)
	private ColorJugador color;

	@NotNull
	@Min(1)
	@Max(4)
	@Column(name = "ordre_torn", nullable = false)
	private Integer ordreTorn;

	@NotNull
	@Min(0)
	@Column(name = "cavallers_jugats", nullable = false)
	private int cavallersJugats = 0;

	// Relació amb partida //

	@ManyToOne(optional = false)
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;

	// Inventari del jugador //

	@OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RecursJugador> recursos = new ArrayList<>();

	// Construccions del jugador //

	@OneToMany(mappedBy = "propietari")
	private List<Construccio> construccions = new ArrayList<>();

	// Cartes del jugador //

	@OneToMany(mappedBy = "propietari")
	private List<CartaDesenvolupament> cartes = new ArrayList<>();

	// Constructor sense parametres

	public Jugador() {

	}

	// Constructor amb parametres

	public Jugador(String nom, ColorJugador color, Integer ordreTorn, Partida partida, int cavallersJugats) {

		this.nom = nom;
		this.color = color;
		this.ordreTorn = ordreTorn;
		this.partida = partida;
		this.cavallersJugats = 0;

		inicialitzarRecursos();
	}
	
	
	

	// Metode inicialitzarRecursos

	public void inicialitzarRecursos() {
		recursos.clear();

		for (TipusRecurs tipus : TipusRecurs.values()) {
			afegirRecurs(new RecursJugador(this, tipus));
		}
	}
	
	
	

	// Metode afegirRecurs

	public void afegirRecurs(RecursJugador recurs) {

		if (recurs == null) {
			throw new IllegalArgumentException("El recurs no pot ser null");
		}

		recurs.setJugador(this);
		recursos.add(recurs);
	}
	
	
	

	// Metode afegirConstruccio //

	public void afegirConstruccio(Construccio construccio) {

		if (construccio == null) {
			throw new IllegalArgumentException("La construcció no pot ser null");
		}

		construccio.setPropietari(this);
		construccions.add(construccio);
	}
	
	
	

	// Metode buscarRecurs //

	public RecursJugador buscarRecurs(TipusRecurs tipusBuscat) {

		if (tipusBuscat == null) {
			throw new IllegalArgumentException("El tipus de recurs no pot ser null");
		}

		for (RecursJugador recurs : recursos) {
			if (recurs.getTipus() == tipusBuscat) {
				return recurs;
			}
		}

		return null;
	}
	
	
	

	// Metode afegirCarta //

	public void afegirCarta(CartaDesenvolupament carta, Integer rondaCompra) {

		if (carta == null) {
			throw new IllegalArgumentException("La carta no pot ser null");
		}

		if (rondaCompra == null || rondaCompra < 1) {
			throw new IllegalArgumentException("La ronda de compra ha de ser superior a zero");
		}

		carta.setPropietari(this);
		carta.setRondaCompra(rondaCompra);
		cartes.add(carta);
	}
	
	
	

	// Getters / Setters

	public Integer getId() {
		return id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public ColorJugador getColor() {
		return color;
	}

	public void setColor(ColorJugador color) {
		this.color = color;
	}

	public Integer getOrdreTorn() {
		return ordreTorn;
	}

	public void setOrdreTorn(Integer ordreTorn) {
		this.ordreTorn = ordreTorn;
	}

	public int getCavallersJugats() {
		return cavallersJugats;
	}

	public void setCavallersJugats(int cavallersJugats) {
		this.cavallersJugats = cavallersJugats;
	}

	public Partida getPartida() {
		return partida;
	}

	public void setPartida(Partida partida) {
		this.partida = partida;
	}

	public List<RecursJugador> getRecursos() {
		return recursos;
	}

	public List<Construccio> getConstruccions() {
		return construccions;
	}

	public List<CartaDesenvolupament> getCartes() {
		return cartes;
	}
	
	
	

	// HASHCODE //

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
	
	
	

	// EQUALS //

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Jugador other)) {
			return false;
		}

		return id != null && id.equals(other.id);
	}
	
	

	// toString

	@Override
	public String toString() {
		return "Jugador [id=" + id + ", nom=" + nom + ", color=" + color + ", ordreTorn=" + ordreTorn
				+ ", cavallersJugats=" + cavallersJugats + ", partidaId=" + (partida != null ? partida.getId() : null)
				+ "]";
	}

}
