package taulell;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import model_joc.Partida;


@Entity
@Table(name = "aresta")
public class Aresta {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Integer codi;
	
	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;
	
	
	private Vertex vertexA;
	private Vertex vertexB;

}