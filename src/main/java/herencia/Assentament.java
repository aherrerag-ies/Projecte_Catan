package herencia;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import relacions.Partida;
import taulell.Vertex;

public abstract class Assentament extends Construccio {
	
	
	
	// Atributs //
	
	
	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "vertex_id", nullable = false,  unique = true)
	private Vertex vertex;
	

	
	
	
	// Constructor sense parametres //
	
	
	public Assentament() {
		
	}
	
	
	// Constructor sense parametres //

	public Assentament(Vertex vertex) {
		super();
		this.vertex = vertex;
	}
	
	
	// Getter / Setter //

	public Vertex getVertex() {
		return vertex;
	}

	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}
	
	// EQUALS //
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Assentament other))
			return false;
		return getId() != null && getId().equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Assentament.class.hashCode();
	}

		
	
	// TOSTRING //
	
	
	
	
	

}
