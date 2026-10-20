package model_joc;


import enumeration.TipusRecurs;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(
    name = "recurs_jugador",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_recurs_jugador_tipus",
            columnNames = {"jugador_id", "tipus"}
        )
    }
)



public class RecursJugador {
	
	  	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne(optional = false)
	    @JoinColumn(name = "jugador_id", nullable = false)
	    private Jugador jugador;

	    @NotNull
	    @Enumerated(EnumType.STRING)
	    @Column(name = "tipus", nullable = false, length = 20)
	    private TipusRecurs tipus;

	    @NotNull
	    @Min(0)
	    @Column(name = "quantitat", nullable = false)
	    private Integer quantitat = 0;

	    public RecursJugador() {
	    }

	    public RecursJugador(Jugador jugador, TipusRecurs tipus) {
	        this.jugador = jugador;
	        this.tipus = tipus;
	        this.quantitat = 0;
	    }

	    public Long getId() {
	        return id;
	    }

	    public Jugador getJugador() {
	        return jugador;
	    }

	    public void setJugador(Jugador jugador) {
	        this.jugador = jugador;
	    }

	    public TipusRecurs getTipus() {
	        return tipus;
	    }

	    public void setTipus(TipusRecurs tipus) {
	        this.tipus = tipus;
	    }

	    public Integer getQuantitat() {
	        return quantitat;
	    }

	    public void setQuantitat(Integer quantitat) {
	        this.quantitat = quantitat;
	    }

}
