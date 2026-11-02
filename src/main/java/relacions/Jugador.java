package relacions;

import java.util.ArrayList;
import java.util.List;

import enumeration.ColorJugador;
import enumeration.TipusRecurs;
import herencia.Ciutat;
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

	// Comptar ciutats de cada jugador //

	public int comptarCiutat() {

		int ciutats = 0;

		for (Construccio c : this.construccions) {
			if (c instanceof Ciutat) {
				ciutats++;
			}
		}
		return ciutats;

	}

	// Comprova si té els recursos necessaris per Ciutat: //
	// 2 de BLAT i 3 de MINERAL //

	public boolean teRecursosPerCiutat() {
		RecursJugador blat = buscarRecurs(TipusRecurs.BLAT);
		RecursJugador mineral = buscarRecurs(TipusRecurs.MINERAL);

		int quantitatBlat = (blat != null) ? blat.getQuantitat() : 0;
		int quantitatMineral = (mineral != null) ? mineral.getQuantitat() : 0;

		return quantitatBlat >= 2 && quantitatMineral >= 3;
	}

	// Comprova si té els recursos necessaris per Poblat //
	// 1 FUSTA + 1 MAÓ + 1 LLANA + 1 BLAT //

	public boolean teRecursosPerPoblat() {
		RecursJugador fusta = buscarRecurs(TipusRecurs.FUSTA);
		RecursJugador mao = buscarRecurs(TipusRecurs.MAO);
		RecursJugador llana = buscarRecurs(TipusRecurs.LLANA);
		RecursJugador blat = buscarRecurs(TipusRecurs.BLAT);

		int quantitatFusta = (fusta != null) ? fusta.getQuantitat() : 0;
		int quantitatMao = (mao != null) ? mao.getQuantitat() : 0;
		int quantitatLlana = (llana != null) ? llana.getQuantitat() : 0;
		int quantitatBlat = (blat != null) ? blat.getQuantitat() : 0;

		return quantitatFusta >= 1 && quantitatMao >= 1 && quantitatLlana >= 1 && quantitatBlat >= 1;
	}

	// Comprova si té els recursos necessaris per Carretera: //
	// 1 FUSTA + 1 MAÓ //

	public boolean teRecursosPerCarretera() {
		RecursJugador fusta = buscarRecurs(TipusRecurs.FUSTA);
		RecursJugador mao = buscarRecurs(TipusRecurs.MAO);

		int quantitatFusta = (fusta != null) ? fusta.getQuantitat() : 0;
		int quantitatMao = (mao != null) ? mao.getQuantitat() : 0;

		return quantitatFusta >= 1 && quantitatMao >= 1;
	}

	// Comprova si té els recursos necessaris per Carta Desenvolupament: //
	// 1 BLAT + 1 LLANA + 1 MINERAL //

	public boolean teRecursosPerCartaDesenvolupament() {
		RecursJugador blat = buscarRecurs(TipusRecurs.BLAT);
		RecursJugador llana = buscarRecurs(TipusRecurs.LLANA);
		RecursJugador mineral = buscarRecurs(TipusRecurs.MINERAL);

		int quantitatBlat = (blat != null) ? blat.getQuantitat() : 0;
		int quantitatLlana = (llana != null) ? llana.getQuantitat() : 0;
		int quantitatMineral = (mineral != null) ? mineral.getQuantitat() : 0;

		return quantitatBlat >= 1 && quantitatLlana >= 1 && quantitatMineral >= 1;
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Jugador other))
			return false;
		return getId() != null && getId().equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Jugador.class.hashCode();
	}

	// toString

	@Override
	public String toString() {
		return "Jugador [id=" + id + ", nom=" + nom + ", color=" + color + ", ordreTorn=" + ordreTorn
				+ ", cavallersJugats=" + cavallersJugats + ", partidaId=" + (partida != null ? partida.getId() : null)
				+ "]";
	}

}
