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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import relacions.Partida;

@Entity
@Table(name = "vertex", uniqueConstraints = {
		@UniqueConstraint(name = "uk_vertex_partida_codi", columnNames = { "partida_id", "codi" }) })
public class Vertex {
	
	
	//Atributs

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@Min(0)
	@Column(name = "codi", nullable = false)
	private int codi;

	

	@ManyToOne(optional = false,fetch = FetchType.EAGER)
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;
	
	
	
	//Constructor sense parametres

	public Vertex() {
	}
	
	//Constructor parametres

	public Vertex(Integer codi, Partida partida) {

		this.codi = codi;
		this.partida = partida;
	}
	
	//Getters / Setters

	public int getId() {
		return id;
	}

	public Integer getCodi() {
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
}