package herencia;

import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import relacions.Jugador;
import taulell.Vertex;

@Entity
@Table(name = "ciutat")
@NamedQuery(name = "Ciutat.comptarPerJugador", query = """
		    SELECT COUNT(c)
		    FROM Ciutat c
		    WHERE c.propietari.id = :idJugador
		""")

public class Ciutat extends Assentament {

	// Constructor sense paràmetres obligatori per Hibernate

	protected Ciutat() {

	}

	// Constructor per crear una nova ciutat //

	public Ciutat(Jugador propietari, Vertex vertex) {

		super(propietari, vertex);
	}

}
