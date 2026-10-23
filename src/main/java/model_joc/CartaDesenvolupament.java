package model_joc;

import enumeration.TipusCartaDesenvolupament;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
public class CartaDesenvolupament {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Enumerated(EnumType.STRING)
	private TipusCartaDesenvolupament tipus;
	
	@ManyToOne
	@JoinColumn(name = "partida_id")
	private Partida partida;
	
	@ManyToOne
	@JoinColumn(name = "jugador_id")
	private Jugador propietari;
	
	@Column(name="ronda_comprar",nullable = false, length = 80)
	private Integer rondaCompra;
	
	@Column(name="usada",nullable = false)
	private Boolean usada;

	
	protected CartaDesenvolupament() {
		
	}
	protected CartaDesenvolupament(int id, TipusCartaDesenvolupament tipus,Partida partida, Jugador propetari,
			Integer rondaCompra, Boolean usada) {
		this.id = id;
		this.tipus = tipus;
		this.partida = partida;
		this.propietari = propetari;
		this.rondaCompra = rondaCompra;
		this.usada = usada;
		
	}
	public Integer getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public TipusCartaDesenvolupament getTipus() {
		return tipus;
	}
	public void setTipus(TipusCartaDesenvolupament tipus) {
		this.tipus = tipus;
	}
	public Partida getPartida() {
		return partida;
	}
	public void setPartida(Partida partida) {
		this.partida = partida;
	}
	public Jugador getPropietari() {
		return propietari;
	}
	public void setPropietari(Jugador propietari) {
		this.propietari = propietari;
	}
	public Integer getRondaCompra() {
		return rondaCompra;
	}
	public void setRondaCompra(Integer rondaCompra) {
		this.rondaCompra = rondaCompra;
	}
	public Boolean getUsada() {
		return usada;
	}
	public void setUsada(Boolean usada) {
		this.usada = usada;
	}
	@Override
	public String toString() {
		return "CartaDesenvolupament [id=" + id + ", tipus=" + tipus + ", partida=" + partida + ", propietari="
				+ propietari + ", rondaCompra=" + rondaCompra + ", usada=" + usada + "]";
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof CartaDesenvolupament other))
			return false;
		return getId() != null && getId().equals(other.getId());
	}

	@Override
	public int hashCode() {
		return CartaDesenvolupament.class.hashCode();
	}
}
