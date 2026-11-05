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

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "vertex_a_id", nullable = false)
	private Vertex vertexA;

	@NotNull
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
		if (codi == null || codi < 0) {
			throw new IllegalArgumentException("El codi de l'aresta no pot ser null ni negatiu");
		}
		this.codi = codi;
	}

	public Partida getPartida() {
		return partida;
	}

	public void setPartida(Partida partida) {
		if (partida == null) {
			throw new IllegalArgumentException("La partida de l'aresta no pot ser null");
		}
		if (this.partida != null && !this.partida.equals(partida)) {
			throw new IllegalArgumentException("No es pot canviar la partida d'una aresta");
		}
		if ((vertexA != null && !partida.equals(vertexA.getPartida()))
				|| (vertexB != null && !partida.equals(vertexB.getPartida()))) {
			throw new IllegalArgumentException("Els vèrtexs de l'aresta han de pertànyer a la mateixa partida");
		}
		this.partida = partida;
	}

	public Vertex getVertexA() {
		return vertexA;
	}

	public void setVertexA(Vertex vertexA) {
		validarVertex(vertexA);
		if (vertexB != null && mateixVertex(vertexA, vertexB)) {
			throw new IllegalArgumentException("Una aresta ha de connectar dos vèrtexs diferents");
		}
		this.vertexA = vertexA;
	}

	public Vertex getVertexB() {
		return vertexB;
	}

	public void setVertexB(Vertex vertexB) {
		validarVertex(vertexB);
		if (vertexA != null && mateixVertex(vertexA, vertexB)) {
			throw new IllegalArgumentException("Una aresta ha de connectar dos vèrtexs diferents");
		}
		this.vertexB = vertexB;
	}

	private void validarVertex(Vertex vertex) {
		if (vertex == null) {
			throw new IllegalArgumentException("Els vèrtexs de l'aresta no poden ser null");
		}
		if (partida != null && !partida.equals(vertex.getPartida())) {
			throw new IllegalArgumentException("El vèrtex ha de pertànyer a la mateixa partida que l'aresta");
		}
	}

	private boolean mateixVertex(Vertex primer, Vertex segon) {
		return primer == segon || partida != null && partida.equals(primer.getPartida())
				&& partida.equals(segon.getPartida()) && primer.getCodi().equals(segon.getCodi());
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