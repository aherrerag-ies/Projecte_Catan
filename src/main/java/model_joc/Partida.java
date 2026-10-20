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
	
	
	//Constructor sense parametres
	
	public Partida() {
	}

	
	//Constructor amb parametres
	
	public Partida(EstatPartida estat, Integer ronda, Integer ultimaTirada, Integer puntsPerGuanyar, Boolean dausTirats,
			Boolean lladrePendent, Jugador jugadorActiu, Jugador guanyador) {
		super();
		this.estat = estat.PREPARACIO;
		this.ronda = ronda;
		this.ultimaTirada = ultimaTirada;
		this.puntsPerGuanyar = puntsPerGuanyar;
		this.dausTirats = dausTirats;
		this.lladrePendent = lladrePendent;
		this.jugadorActiu = jugadorActiu;
		this.guanyador = guanyador;
	}


	
	//Getters - Setters
	
	
	public int getId() {
		return id;
	}


	public EstatPartida getEstat() {
		return estat;
	}


	public void setEstat(EstatPartida estat) {
		this.estat = estat;
	}


	public Integer getRonda() {
		return ronda;
	}


	public void setRonda(Integer ronda) {
		this.ronda = ronda;
	}


	public Integer getUltimaTirada() {
		return ultimaTirada;
	}


	public void setUltimaTirada(Integer ultimaTirada) {
		this.ultimaTirada = ultimaTirada;
	}


	public Integer getPuntsPerGuanyar() {
		return puntsPerGuanyar;
	}


	public void setPuntsPerGuanyar(Integer puntsPerGuanyar) {
		this.puntsPerGuanyar = puntsPerGuanyar;
	}


	public Boolean getDausTirats() {
		return dausTirats;
	}


	public void setDausTirats(Boolean dausTirats) {
		this.dausTirats = dausTirats;
	}


	public Boolean getLladrePendent() {
		return lladrePendent;
	}


	public void setLladrePendent(Boolean lladrePendent) {
		this.lladrePendent = lladrePendent;
	}


	public Jugador getJugadorActiu() {
		return jugadorActiu;
	}


	public void setJugadorActiu(Jugador jugadorActiu) {
		this.jugadorActiu = jugadorActiu;
	}


	public Jugador getGuanyador() {
		return guanyador;
	}


	public void setGuanyador(Jugador guanyador) {
		this.guanyador = guanyador;
	}
	
	
	
	
	
	
	
	
	
	


	

}
