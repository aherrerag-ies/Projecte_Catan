package taulell;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import relacions.Partida;

@Entity
@Table(name = "aresta", uniqueConstraints = {
		@UniqueConstraint(name = "uk_aresta_partida_codi", columnNames = { "partida_id", "codi" }) })
public class Aresta {
	
	//Atributs

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotNull
	@Min(0)
	@Column(name = "codi", nullable = false)
	private int codi;

	@ManyToOne(optional = false)
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;

	@ManyToOne(optional = false)
	@JoinColumn(name = "vertex_a_id", nullable = false)
	private Vertex vertexA;

	@ManyToOne(optional = false)
	@JoinColumn(name = "vertex_b_id", nullable = false)
	private Vertex vertexB;
	
	//Constructor sense parametres

	public Aresta() {
		
	}
	
	
	//Constructor amb parametres

	public Aresta(Integer codi, Partida partida, Vertex vertexA, Vertex vertexB) {

		this.codi = codi;
		this.partida = partida;
		this.vertexA = vertexA;
		this.vertexB = vertexB;
	}
	
	
	//Getters / Setters

	public Integer getId() {
		return id;
	}

	public int getCodi() {
		return codi;
	}

	public void setCodi(Integer codi) {
		this.codi = codi;
	}

	public Partida getPartida() {
		return partida;
	}

	public void setPartida(Partida partida) {
		this.partida = partida;
	}

	public Vertex getVertexA() {
		return vertexA;
	}

	public void setVertexA(Vertex vertexA) {
		this.vertexA = vertexA;
	}

	public Vertex getVertexB() {
		return vertexB;
	}

	public void setVertexB(Vertex vertexB) {
		this.vertexB = vertexB;
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Aresta other)) return false;
		return getId() != null && getId().equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Aresta.class.hashCode();
	}
}