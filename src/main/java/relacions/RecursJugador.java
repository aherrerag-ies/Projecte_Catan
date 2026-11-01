package relacions;

import enumeration.TipusRecurs;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@NamedQuery(name = "RecursJugador.buscarPerJugador", query = """
		    SELECT r
		    FROM RecursJugador r
		    WHERE r.jugador.id = :idJugador
		    ORDER BY r.tipus
		""")
@Table(name = "recurs_jugador", uniqueConstraints = {
		@UniqueConstraint(name = "uk_recurs_jugador_tipus", columnNames = { "jugador_id", "tipus" }) })
public class RecursJugador {

	// Clau primària autogenerada //

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// Jugador propietari del recurs //

	@ManyToOne(optional = false)
	@JoinColumn(name = "jugador_id", nullable = false)
	private Jugador jugador;

	// Tipus de recurs: FUSTA, MAO, LLANA, BLAT o MINERAL // 

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "tipus", nullable = false, length = 20)
	private TipusRecurs tipus;

	// Quantitat disponible d'aquest recurs

	@NotNull
	@Min(0)
	@Column(name = "quantitat", nullable = false)
	private Integer quantitat = 0;
	
	
	
	

	// Constructor sense param //

	protected RecursJugador() {
	}

	// Constructor per crear un recurs inicial del jugador //

	public RecursJugador(Jugador jugador, TipusRecurs tipus) {

		setJugador(jugador);
		setTipus(tipus);
		this.quantitat = 0;
	}
	
	

	// Metodo sumarQuantitat //
	
	/**
	 * Suma una quantitat positiva al recurs.
	 *
	 * S'utilitza quan: - Un hexàgon produeix recursos. - El jugador comercia amb la
	 * banca. - Es reparteixen els recursos inicials.
	 */
	
	public void sumarQuantitat(int quantitat) {

		if (quantitat <= 0) {
			throw new IllegalArgumentException("La quantitat a sumar ha de ser superior a zero");
		}

		this.quantitat += quantitat;
	}
	
	// Metodo restarQuantitat //

	/**
	 * Intenta restar una quantitat al recurs.
	 *
	 * @return true si hi havia prou recursos i s'han restat; false si no hi havia
	 *         prou recursos.
	 */
	public boolean restarQuantitat(int quantitat) {

		if (quantitat <= 0) {
			throw new IllegalArgumentException("La quantitat a restar ha de ser superior a zero");
		}

		if (this.quantitat < quantitat) {
			return false;
		}

		this.quantitat -= quantitat;

		return true;
	}

	// Getters i setters //

	public Integer getId() {
		return id;
	}

	public Jugador getJugador() {
		return jugador;
	}

	public void setJugador(Jugador jugador) {

		if (jugador == null) {
			throw new IllegalArgumentException("El jugador no pot ser null");
		}

		this.jugador = jugador;
	}

	public TipusRecurs getTipus() {
		return tipus;
	}

	public void setTipus(TipusRecurs tipus) {

		if (tipus == null) {
			throw new IllegalArgumentException("El tipus de recurs no pot ser null");
		}

		this.tipus = tipus;
	}

	public Integer getQuantitat() {
		return quantitat;
	}

	public void setQuantitat(Integer quantitat) {

		if (quantitat == null || quantitat < 0) {
			throw new IllegalArgumentException("La quantitat no pot ser null ni negativa");
		}

		this.quantitat = quantitat;
	}

	// equals basat en ID //

	@Override
	public boolean equals(Object objecte) {

		if (this == objecte) {
			return true;
		}

		if (!(objecte instanceof RecursJugador altreRecurs)) {
			return false;
		}

		return id != null && id.equals(altreRecurs.id);
	}

	// hashCode estable per a una entitat amb ID autogenerat //

	@Override
	public int hashCode() {
		return RecursJugador.class.hashCode();
	}

	// toString simplificat per evitar recursió amb Jugador //

	@Override
	public String toString() {
		return "RecursJugador [id=" + id + ", jugadorId=" + (jugador != null ? jugador.getId() : null) + ", tipus="
				+ tipus + ", quantitat=" + quantitat + "]";
	}
}