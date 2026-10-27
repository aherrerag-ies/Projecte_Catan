package herencia;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import relacions.Jugador;
import taulell.Aresta;

@Entity
@Table(name = "carretera")
public class Carretera extends Construccio {

	@OneToOne(optional = false)
	@JoinColumn(name = "aresta_id", nullable = false, unique = true)
	private Aresta aresta;

	// Construcotr sense parametres //

	protected Carretera() {

	}

	// Construcotr amb parametres //

	public Carretera(Jugador propietari, Aresta aresta) {
		super(propietari);

		if (aresta == null) {
			throw new IllegalArgumentException("L'aresta no pot ser null");
		}

		this.aresta = aresta;
	}

	// Getters / Setters //

	public Aresta getAresta() {
		return aresta;
	}

	public void setAresta(Aresta aresta) {

		if (aresta == null) {
			throw new IllegalArgumentException("L'aresta no pot ser null");
		}

		this.aresta = aresta;
	}

	@Override
	public String toString() {
		return "Carretera [id=" + getId() + ", propietariId="
				+ (getPropietari() != null ? getPropietari().getId() : null) + ", arestaId="
				+ (aresta != null ? aresta.getId() : null) + "]";
		
		// Resultado --> Carretera [id=12, propietariId=3, arestaId=45]
	}

}
