package model_joc;

import java.util.ArrayList;

import java.util.List;

import enumeration.ColorJugador;
import enumeration.TipusRecurs;
import herencia.Construccio;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


	@Entity
	@Table(name = "jugador")
	public class Jugador {

		
		// Atributs //
		
		
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @NotBlank
	    @Size(max = 50)
	    @Column(name = "nom", nullable = false, length = 50)
	    private String nom;

	    @NotNull
	    @Enumerated(EnumType.STRING)
	    @Column(name = "color", nullable = false, length = 20)
	    private ColorJugador color;

	    @NotNull
	    @Min(1)
	    @Max(4)
	    @Column(name = "ordre_torn", nullable = false)
	    private Integer ordreTorn;

	    @NotNull
	    @Min(0)
	    @Column(name = "cavallers_jugats", nullable = false)
	    private Integer cavallersJugats = 0;

	    @ManyToOne(optional = false)
	    @JoinColumn(name = "partida_id", nullable = false)
	    private Partida partida;

	    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL,orphanRemoval = true)
	    
	    
	    // Listas //
	    
	    private List<RecursJugador> recursos = new ArrayList<>();
	    
	    

	    /*
	     * Estas colecciones todavía no llevan anotaciones JPA.
	     * Se completarán cuando Construccio y CartaDesenvolupament
	     * sean entidades. 
	     */
	    
	    
	    private List<Construccio> construccions = new ArrayList<>();

	    private List<CartaDesenvolupament> cartes = new ArrayList<>();
	    
	    
	    //Constructor sense parametres

	    public Jugador() {
	    	
	    }
	    
	    
	    //Constructor amb parametres

	    public Jugador(String nom,ColorJugador color,Integer ordreTorn, Partida partida) {

	    	this.nom = nom;
	        this.color = color;
	        this.ordreTorn = ordreTorn;
	        this.partida = partida;
	        this.cavallersJugats = 0;

	        inicialitzarRecursos();
	    }
	    
	    
	    
	    // Metode inicialitzarRecursos

	    public void inicialitzarRecursos() {
	        recursos.clear();

	        for (TipusRecurs tipus : TipusRecurs.values()) {
	            afegirRecurs(new RecursJugador(this, tipus));
	        }
	    }
	    
	    //Metode afegirRecurs

	    public void afegirRecurs(RecursJugador recurs) {
	        recurs.setJugador(this);
	        recursos.add(recurs);
	    }
	    
	    
	    // Getters /  Setters

	    public Long getId() {
	        return id;
	    }

	    public String getNom() {
	        return nom;
	    }

	    public void setNom(String nom) {
	        this.nom = nom;
	    }

	    public ColorJugador getColor() {
	        return color;
	    }

	    public void setColor(ColorJugador color) {
	        this.color = color;
	    }

	    public Integer getOrdreTorn() {
	        return ordreTorn;
	    }

	    public void setOrdreTorn(Integer ordreTorn) {
	        this.ordreTorn = ordreTorn;
	    }

	    public Integer getCavallersJugats() {
	        return cavallersJugats;
	    }

	    public void setCavallersJugats(Integer cavallersJugats) {
	        this.cavallersJugats = cavallersJugats;
	    }

	    public Partida getPartida() {
	        return partida;
	    }

	    public void setPartida(Partida partida) {
	        this.partida = partida;
	    }

	    public List<RecursJugador> getRecursos() {
	        return recursos;
	    }

	    public List<Construccio> getConstruccions() {
	        return construccions;
	    }

	    public List<CartaDesenvolupament> getCartes() {
	        return cartes;
	    }

	}


