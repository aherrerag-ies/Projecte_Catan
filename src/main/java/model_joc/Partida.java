package model_joc;

import java.util.ArrayList;
import java.util.List;

import enumeration.EstatPartida;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



@Entity
@Table(name = "partida")
public class Partida {
	
	
	@Id
	@Column(name = "id", unique = true,nullable = false, length = 80)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(nullable = false, length = 80)
	private EstatPartida estat;
	
	@Column(name = "ronda", nullable = false, length = 80)
	private Integer ronda;
	
	@Column(name = "ultima_tirada" ,nullable = false, length = 80)
	private Integer ultimaTirada;
	
	
	@Column(name = "punts_per_guanyar", nullable = false, length = 80)
	private Integer puntsPerGuanyar;
	
	@Column(name = "daus_tirats" ,nullable = false, length = 10)
	private Boolean dausTirats;
	
	@Column(name = "lladre_pendent", nullable = false, length = 10)
	private Boolean lladrePendent;

	@Column(name = "jugador_actiu" ,nullable = false, length = 10)
	private Jugador jugadorActiu;
	
	@Column(name = "guanyador", nullable = false, length = 10)
	private Jugador guanyador;


	

}
