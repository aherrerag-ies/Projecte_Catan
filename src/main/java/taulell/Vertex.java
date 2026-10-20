package taulell;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import model_joc.Partida;

public class Vertex {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	
	private Integer codi;
	private Double x;
	private Double y;
	private Partida partida;

}
