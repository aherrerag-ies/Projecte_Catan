package herencia;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import model_joc.Jugador;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Construccio {
	

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "jugador_id")
	private Jugador propietari;
	
	protected Construccio(){
		
	}
	
	protected Construccio(int id,Jugador propetari){
		this.id = id;
		this.propietari = propetari;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Jugador getPropietari() {
		return propietari;
	}
	public void setPropietari(Jugador propietari) {
		this.propietari = propietari;
	}
	@Override
	public String toString() {
		return "Construccio [id=" + id + ", propietari=" + propietari + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Construccio other))
			return false;
		return getId() != null && getId().equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Construccio.class.hashCode();
	}
	


}


