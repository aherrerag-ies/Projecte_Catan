package relacions;

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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "carta_desenvolupament")
public class CartaDesenvolupament {
	
	// Atributs //
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id; 
	
	// Tipus carta //
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "tipus", nullable = false, length = 30)
	private TipusCartaDesenvolupament tipus; // CAVALLER , MONOPOLI O PUNT_VICTORIA
	
	
	// Partida a la qual pertany la carta //
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;
	
	
	// Ronda en què es compra //
	
	@Min(1)
	@Column(name = "ronda_compra", nullable = true)
	private Integer rondaCompra;
	
	
	
	//  Indica si la carta ja s'ha utilitzat //
	@NotNull
	@Column(name = "usada", nullable = false)
	private Boolean usada = false;
	
	
	// Jugador propietari de la carta //
	
	@ManyToOne
	@JoinColumn(name = "propietari_id", nullable = true)
	private Jugador propietari;
	
	
	
	
	
	// Constructor sense parametres //

	
	protected CartaDesenvolupament() {
		
	}
	
	
	
	
	// Constructor amb parametres //
	
	public CartaDesenvolupament(TipusCartaDesenvolupament tipus,Partida partida, Jugador propietari,
			Integer rondaCompra, Boolean usada) {
		this.tipus = tipus;
		this.partida = partida;
		this.propietari = propietari;
		this.rondaCompra = rondaCompra;
		this.usada = usada;
		
	}
	
	
	
	
	
	// Getters / Setters //
	
	public Integer getId() {
		return id;
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
	
	
	// To string //
	
	@Override
	public String toString() {
		return "CartaDesenvolupament [id=" + id + ", tipus=" + tipus + ", partida=" + partida + ", propietari="
				+ propietari + ", rondaCompra=" + rondaCompra + ", usada=" + usada + "]";
	}
	
	
	// Equals //
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof CartaDesenvolupament other))
			return false;
		return getId() != null && getId().equals(other.getId());
	}
	
	// HashCode //

	@Override
	public int hashCode() {
		return CartaDesenvolupament.class.hashCode();
	}
}
