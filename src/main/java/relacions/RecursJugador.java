package relacions;

import java.util.Objects;

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
// Consulta definida amb NamedQuery //
@NamedQuery(name = "RecursJugador.buscarPerJugador", query = """
		    SELECT r
		    FROM RecursJugador r
		    WHERE r.jugador.id = :idJugador
		    ORDER BY r.tipus
		""")
@Table(name = "recurs_jugador", uniqueConstraints = {
		@UniqueConstraint(name = "uk_recurs_jugador_tipus", columnNames = { "jugador_id", "tipus" }) })

public class RecursJugador {
	
	// Clau id autogenerada //

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	
	// Relació amb el jugador propietari del recurs // 
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "jugador_id", nullable = false)
	private Jugador jugador;

	
	
	// Tipus de recurs //
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "tipus", nullable = false)
	private TipusRecurs tipus;

	

    // Quantitat disponible //
	
	@NotNull
	@Min(0)
	@Column(name = "quantitat", nullable = false)
	private Integer quantitat = 0;
	
	

	// Constructor sense parametres //

	public RecursJugador() {

	}
	
	

	// Constructor amb parametres //

	public RecursJugador(Jugador jugador, TipusRecurs tipus) {
		this.jugador = jugador;
		this.tipus = tipus;
		this.quantitat = 0;
	}

	// Getter - Setter //

	public Integer getId() {
		return id;
	}

	public Jugador getJugador() {
		return jugador;
	}

	public void setJugador(Jugador jugador) {
		this.jugador = jugador;
	}

	public TipusRecurs getTipus() {
		return tipus;
	}

	public void setTipus(TipusRecurs tipus) {
		this.tipus = tipus;
	}

	public Integer getQuantitat() {
		return quantitat;
	}

	public void setQuantitat(Integer quantitat) {
		this.quantitat = quantitat;
	}

	// HASHCODE //

	@Override
	public int hashCode() {
		return Objects.hash(id, jugador, quantitat, tipus);
	}


	// EQUALS //

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecursJugador other = (RecursJugador) obj;
		return Objects.equals(id, other.id) && Objects.equals(jugador, other.jugador)
				&& Objects.equals(quantitat, other.quantitat) && tipus == other.tipus;
	}


	
	// toString //

	@Override
	public String toString() {
		return "RecursJugador [id=" + id + ", jugador=" + jugador + ", tipus=" + tipus + ", quantitat=" + quantitat
				+ "]";
	}
	
	
	
	
	

}
