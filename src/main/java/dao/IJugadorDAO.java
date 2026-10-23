package dao;

import java.util.List;

import enumeration.TipusRecurs;
import relacions.Jugador;
import taulell.Vertex;

public interface IJugadorDAO extends IGenericDAO<Jugador, Integer>{

	/*
	 * Mostra l'inventari de recursos d'un jugador mitjançant una consulta a la BD.
	 */
	void MostrarRecursos(int idJugador);
	
	/*
	 * Construeix una carretera:
	 */
	void ConstruirCarretera(int idJugador, int idAresta);
	
	/*
	 *  Construeix un poblat
	 */
	void ConstruirPoblat(int idJugador, int idVertex);
	
	/*
	 *  Substitueix un poblat propi per una ciutat
	 */
	void MillorarACiutat(int idJugador, int idVertex);
	
	/*
	 * Canvia 4 recursos del tipus dona per 1 del tipus rep.
	 */
	void ComerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep);
	
	/*
	 *  Calcula els punts de victòria del jugador amb una consulta a la BD.
	 */
	int CalcularPunts(int idJugador); 
	
	/*
	 *  Gestiona el final del torn d'un jugador.
	 */
	void PassarTorn(int idJugador);
	
	/*
	 * 	 Mou el lladre quan està pendent (després d'un 7 o d'un CAVALLER)
	 */	
	void MoureLladre(int idJugador, int idHexagon, Integer idJugadorVictima);
	
	/*
	 * Compra una carta de desenvolupament
	 */
	void ComprarCarta(int idJugador);
	
	/*
	 *  Juga una carta de la mà (no es pot jugar a la mateixa ronda en què s'ha comprat, és a dir, 
	 *  rondaCompra ha de ser menor que la ronda actual). La carta queda marcada com a usada
	 */
	void JugarCarta(int idJugador, int idCarta, TipusRecurs recurs);
	
	/*
	 *  Torna els vèrtexs on el jugador pot construir un poblat ara mateix
	 */
	List<Vertex> VertexsDisponibles(int idJugador);
	
	
	
	
	
}
