package taulell;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "hexagon_vertex", uniqueConstraints = {
		@UniqueConstraint(name = "uk_hexagon_posicio", columnNames = { "hexagon_id", "posicio" }),
		@UniqueConstraint(name = "uk_hexagon_vertex", columnNames = { "hexagon_id", "vertex_id" }) })


public class HexagonVertex {
	
	
	//Atributs

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "hexagon_id", nullable = false)
	private Hexagon hexagon;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "vertex_id", nullable = false)
	private Vertex vertex;

	@NotNull
	@Min(0)
	@Max(5)
	@Column(name = "posicio", nullable = false)
	private int posicio;
	
	
	//Constructor sense parametres

	public HexagonVertex() {
		
	}
	
	
	//Constructor amb parametres

	public HexagonVertex(Hexagon hexagon, Vertex vertex, Integer posicio) {

		this.hexagon = hexagon;
		this.vertex = vertex;
		this.posicio = posicio;
	}
	
	
	//Getters / Setters

	public int getId() {
		return id;
	}

	public Hexagon getHexagon() {
		return hexagon;
	}

	public void setHexagon(Hexagon hexagon) {
		this.hexagon = hexagon;
	}

	public Vertex getVertex() {
		return vertex;
	}

	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}

	public int getPosicio() {
		return posicio;
	}

	public void setPosicio(Integer posicio) {
		this.posicio = posicio;
	}
}
