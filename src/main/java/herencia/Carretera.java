package herencia;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import taulell.Aresta;

@Entity
@Table(name = "carretera")
public class Carretera extends Construccio {
	
	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "aresta_id", nullable = false,  unique = true)
	private Aresta aresta;
	
	
	protected Carretera() {
		
	}
	public Carretera(Aresta aresta) {
		this.aresta = aresta;
	}
	
	public Aresta getAresta() {
		return aresta;
	}

	public void setAresta(Aresta estudi) {
		this.aresta = estudi;
	}

}
