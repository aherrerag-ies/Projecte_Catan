package herencia;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import relacions.Jugador;
import taulell.Vertex;

@Entity
@Table(name = "assentament")
public abstract class Assentament extends Construccio {

	// Atributs //

	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "vertex_id", nullable = false, unique = true)
	private Vertex vertex;

	// Constructor sense parametres //

	public Assentament() {

	}

	// Constructor utilitzat per Poblat i Ciutat

	protected Assentament(Jugador propietari, Vertex vertex) {

		super(propietari);

		if (vertex == null) {
			throw new IllegalArgumentException("El vèrtex no pot ser null");
		}

		this.vertex = vertex;
	}

	// Getter / Setter //

	public Vertex getVertex() {
		return vertex;
	}

	public void setVertex(Vertex vertex) {

		if (vertex == null) {
			throw new IllegalArgumentException("El vèrtex no pot ser null");
		}
		this.vertex = vertex;
	}

	// Los Getters / Setters ya los hereda de Construccio

	// TOSTRING //

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + getId() + ", propietariId="
				+ (getPropietari() != null ? getPropietari().getId() : null) + ", vertexId="
				+ (vertex != null ? vertex.getId() : null) + "]";
		
		
		// Resultado: Poblat [id=10, propietariId=3, vertexId=25] o Ciutat [id=14, propietariId=2, vertexId=19] //
	}
	
	

}
