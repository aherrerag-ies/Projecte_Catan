package dao;
import java.util.List;

import model_joc.Partida;

public interface IPartidaDAO extends IGenericDAO<Partida, Integer>{

	/*
	 * Carrega les dades inicials dels CSV de resources/.
	 */
	int carregarDades();
	
	/*
	 *  Inicialitza una nova partida i torna el seu id: 
	 *  Hi ha d'haver entre 2 i 4 jugadors; el color s'assigna segons l'ordre de torn.
	 */
	int IniciarPartida(List<String> nomsJugadors);
	
	/*
	 * Mostra tots els hexàgons de la partida (terreny, número, si hi ha el lladre) i, 
	 * per cada un, les construccions dels seus vèrtexs amb el propietari, 
	 * mitjançant una consulta a la BD. Les carreteres no cal mostrar-les.
	 */
	void MostrarTauler(int idPartida);
	
	/*
	 * MostrarPartida(int idPartida) → Mostra la situació de la partida 
	 * en un moment donat extraient les dades de la BD.
	 */
	void MostrarPartida(int idPartida);
	
	/*
	 *  Tira els daus i executa la producció:
	 */
	void TirarDaus(int idPartida);
	
	/*
	 * Comprova les condicions de victòria.
	 */
	void ComprovarVictoria(int idPartida);
	
	/*
	 *  Torna el jugador amb l'exèrcit més gran.
	 */
	void ExercitMesGran(int idPartida);
	
	
	
	
}
