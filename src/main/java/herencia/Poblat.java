package herencia;

import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import relacions.Jugador;
import taulell.Vertex;

@Entity
@Table(name = "poblat")
@NamedQuery(name = "Poblat.comptarPerJugador", query = """
		    SELECT COUNT(p)
		    FROM Poblat p
		    WHERE p.propietari.id = :idJugador
		""")

public class Poblat extends Assentament {
	
	// Constructor sense paràmetres obligatori per Hibernate

	protected Poblat() {
		
	}
	
	// Constructor per crear un nou poblat

	public Poblat(Jugador propietari, Vertex vertex) {
				super(propietari, vertex);
	}

}
