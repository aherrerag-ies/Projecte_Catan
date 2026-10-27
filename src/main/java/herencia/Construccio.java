package herencia;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import relacions.Jugador;

@Entity
@Table(name = "construccio")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Construccio {

	// Atributs //

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	
	// Jugador propietari de la construcció //
	@ManyToOne(optional = false)
	@JoinColumn(name = "propietari_id", nullable = false)
	private Jugador propietari;

	// Constructor sense parametres //

	protected Construccio() {

	}

	// Constructor amb parametres //

	protected Construccio(Jugador propetari) {
		this.propietari = propetari;
	}

	// Getters / Setters //

	public Integer getId() {
		return id;
	}

	public Jugador getPropietari() {
		return propietari;
	}

	public void setPropietari(Jugador propietari) {

		if (propietari == null) {
			throw new IllegalArgumentException("El propietari no pot ser null");
		}

		this.propietari = propietari;
	}

	// toString //

	@Override
	public String toString() {
		return "Construccio [id=" + id + ", propietariId=" + (propietari != null ? propietari.getId() : null) + "]";
	}

	// Equals //

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Construccio other)) return false;
		return getId() != null && getId().equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Construccio.class.hashCode();
	}
	
}
