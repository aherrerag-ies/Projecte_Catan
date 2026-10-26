package taulell;


import enumeration.Terreny;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import relacions.Partida;



//Entitat

@Entity
@Table(name = "hexagon", uniqueConstraints = {
		
		//Impideix repetir un codi dins la mateixa partida
		
		@UniqueConstraint(name = "uk_hexagon_partida_codi", columnNames = { "partida_id", "codi" }) })

public class Hexagon {
	
	//Atributs

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotNull
	@Min(0)
	@Column(name = "codi", nullable = false)
	private int codi;

	@NotNull
	@Column(name = "coordenada_q", nullable = false)
	private int q;

	@NotNull
	@Column(name = "coordenada_r", nullable = false)
	private int r;

	@Enumerated(EnumType.STRING)
	@Column(name = "terreny", length = 20)
	private Terreny terreny;		// Guardem BOSC,TURONS,PASTURES,CAMPS, MUNTANYES,DESERT
										

	@Min(2)
	@Max(12)
	@Column(name = "numero", nullable = true)
	private Integer numero;		// Numero entre 2 i 12 

	@ManyToOne(optional = false)
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;
	
	//Falta relacion con HexagonVertex (lista onetomany) & mappear
	
	//Constructor sense parametres
	
	public Hexagon() {
		
    }
	
	
	//Constructor parametres

    public Hexagon(
            Integer codi,
            Integer q,
            Integer r,
            Terreny terreny,
            Integer numero,
            Partida partida
            ) {

        this.codi = codi;
        this.q = q;
        this.r = r;
        this.numero = numero;
        this.partida = partida;
        this.terreny = null;       
    }
    
    
    // Getters / Setters
    
    public Integer getId() {
        return id;
    }

    public int getCodi() {
        return codi;
    }

    public void setCodi(Integer codi) {
        this.codi = codi;
    }

    public int getQ() {
        return q;
    }

    public void setQ(Integer q) {
        this.q = q;
    }

    public int getR() {
        return r;
    }

    public void setR(Integer r) {
        this.r = r;
    }

    public Terreny getTerreny() {
        return terreny;
    }

    public void setTerreny(Terreny terreny) {
        this.terreny = terreny;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

}
