# Hibernate_Catan

Projecte desenvolupat per al mòdul **M6 - Accés a Dades** del segon curs del cicle formatiu de **Desenvolupament d'Aplicacions Multiplataforma (DAM)**.

L'objectiu del projecte és desenvolupar la persistència i les principals regles d'una partida del joc **Catan**, utilitzant Java, Hibernate, JPA i MySQL.

Durant el desenvolupament del projecte treballem principalment amb entitats, relacions entre classes, herència, consultes a la base de dades, fitxers CSV i el patró DAO.

## Integrants

- Adam
- Adrian
- Ivan

## Informació acadèmica

- **Cicle formatiu:** Desenvolupament d'Aplicacions Multiplataforma
- **Curs:** 2n DAM
- **Mòdul:** M6 - Accés a Dades
- **Professor:** Marc Arto
- **Curs acadèmic:** 2026-2027


## Objectius del projecte

L'objectiu principal del projecte és desenvolupar la persistència i les funcionalitats principals d'una partida de Catan mitjançant Java, Hibernate i MySQL.

Amb aquest projecte volem posar en pràctica els coneixements apresos durant el mòdul d'Accés a Dades, especialment la creació d'entitats, la definició de relacions entre classes i la realització de consultes a una base de dades.

Els objectius principals són:

- Modelar les entitats necessàries per representar una partida de Catan.
- Relacionar les entitats mitjançant anotacions JPA.
- Aplicar herència entre les diferents construccions del joc.
- Utilitzar Hibernate per guardar, consultar, modificar i eliminar dades.
- Aplicar el patró DAO per separar l'accés a dades de les entitats.
- Crear jugadors amb un inventari inicial de recursos.
- Carregar la topologia del tauler a partir de fitxers CSV.
- Gestionar hexàgons, vèrtexs i arestes del tauler.
- Implementar les principals accions d'una partida.
- Gestionar les rondes i els torns dels jugadors.
- Calcular els punts de victòria de cada jugador.
- Treballar de manera col·laborativa utilitzant Git i GitHub.



## Estructura del projecte

El projecte està dividit en diferents paquets format per classes.

```text
src/main/java
├── dao
├── enumeration
├── herencia
├── relacions
└── taulell



## Patró DAO

Hem utilitzat el patró DAO per separar les classes que representen les dades de les classes que treballen directament amb la base de dades.

### IGenericDAO

`IGenericDAO` és una interfície genèrica que defineix les operacions bàsiques que poden fer els DAO:

- Guardar o actualitzar una entitat.
- Buscar una entitat pel seu identificador.
- Eliminar una entitat.
- Mostrar totes les entitats d'un tipus.

Utilitzem els genèrics `T` i `ID` perquè la mateixa interfície pugui funcionar amb diferents entitats i tipus d'identificador.


### GenericDAO

`GenericDAO` implementa els mètodes definits a `IGenericDAO` utilitzant Hibernate.

Aquesta classe s'encarrega d'obrir les transaccions, executar les operacions amb la base de dades i fer `commit` si tot funciona correctament. Si es produeix un error, executa un `rollback`.

D'aquesta manera, evitem repetir les mateixes operacions a `JugadorDAO` i `PartidaDAO`.

Per exemple:

```java
public class JugadorDAO
        extends GenericDAO<Jugador, Integer>
        
        
        
### IPartidaDAO i PartidaDAO

`IPartidaDAO` defineix les operacions principals relacionades amb una partida de Catan.

Aquesta interfície hereta de `IGenericDAO<Partida, Integer>`, de manera que també disposa de les operacions bàsiques per guardar, buscar, eliminar i llistar partides.

`PartidaDAO` implementa aquesta interfície i conté la lògica necessària per treballar amb les partides i el tauler mitjançant Hibernate.

Els mètodes principals definits són:

- `carregarDades()`: carrega la topologia inicial del tauler des dels fitxers CSV.
- `IniciarPartida()`: crea una partida nova i els seus jugadors.
- `MostrarTauler()`: mostra els hexàgons i les construccions del tauler.
- `MostrarPartida()`: mostra l'estat actual de la partida i dels jugadors.
- `TirarDaus()`: genera una tirada i executa la producció de recursos.
- `ComprovarVictoria()`: comprova si algun jugador ha arribat als punts necessaris.
- `ExercitMesGran()`: busca el jugador que té l'exèrcit més gran.

### Càrrega del tauler

La topologia del tauler es carrega des dels fitxers situats a:

```text
src/main/resources/csv



### IJugadorDAO i JugadorDAO

`IJugadorDAO` defineix les accions que pot realitzar un jugador durant una partida.

Aquesta interfície hereta de `IGenericDAO<Jugador, Integer>`, per tant també disposa de les operacions bàsiques per guardar, buscar, eliminar i llistar jugadors.

`JugadorDAO` implementa aquestes accions utilitzant sessions, transaccions i consultes d'Hibernate.

Els mètodes principals són:

- `mostrarRecursos()`: consulta i mostra l'inventari d'un jugador.
- `ConstruirCarretera()`: comprova la posició, la connectivitat, els recursos i crea una carretera.
- `ConstruirPoblat()`: comprova que el vèrtex estigui lliure, la regla de distància, els recursos i crea un poblat.
- `MillorarACiutat()`: substitueix un poblat propi per una ciutat.
- `ComerciarBanca()`: intercanvia quatre recursos iguals per un recurs d'un altre tipus.
- `CalcularPunts()`: calcula els punts obtinguts pels poblats i les ciutats.
- `PassarTorn()`: actualitza el jugador actiu i controla les rondes de preparació i de joc.
- `MoureLladre()`, `ComprarCarta()` i `JugarCarta()`: funcionalitats de la part avançada.
- `VertexsDisponibles()`: retorna les posicions on el jugador pot construir un poblat.

### Comprovacions de les accions

Abans de realitzar una acció, `JugadorDAO` comprova diferents condicions del joc:

- Que el jugador existeixi.
- Que sigui el torn del jugador.
- Que la partida estigui en un estat que permeti l'acció.
- Que la posició seleccionada pertanyi a la mateixa partida.
- Que el vèrtex o l'aresta estiguin lliures.
- Que es compleixin les regles de distància i connectivitat.
- Que el jugador tingui els recursos necessaris.
- Que no s'hagi superat el límit de construccions.
- Que no hi hagi cap moviment del lladre pendent.

Totes les modificacions es fan dins d'una transacció. Si l'operació acaba correctament, es fa `commit`. Si es produeix algun error, es fa `rollback`.

### Inventari de recursos

El mètode `mostrarRecursos()` utilitza una `NamedQuery` definida a `RecursJugador` per obtenir els recursos del jugador des de la base de dades.

```java
RecursJugador.buscarPerJugador



### SessionManager

La classe `SessionManager` s'encarrega de crear i gestionar la `SessionFactory` d'Hibernate.

Hem aplicat el patró **Singleton** perquè la `SessionFactory` és un objecte costós de crear i només necessitem una instància per a tota l'aplicació. Aquesta instància és compartida pels diferents DAO.

La configuració d'Hibernate es carrega des del fitxer:

```text
daoHibernate.cfg.xml




## Paquet enumeration

El paquet `enumeration` conté les enumeracions utilitzades al projecte.

Les enumeracions ens permeten definir un conjunt limitat de valors possibles. D'aquesta manera, evitem introduir dades incorrectes i fem que el codi sigui més fàcil de llegir i mantenir.

A les entitats, les enumeracions es guarden a la base de dades com a text mitjançant:

```java
@Enumerated(EnumType.STRING)
```

Així, la base de dades guarda valors com `VERMELL`, `BOSC` o `FUSTA`, en lloc de guardar números que serien més difícils d'interpretar.

### ColorJugador

L'enumeració `ColorJugador` defineix els quatre colors disponibles per als jugadors:

```text
VERMELL
BLAU
BLANC
TARONJA
```

Cada jugador té assignat un color segons el seu ordre de torn. Dins d'una mateixa partida no es pot repetir el mateix color.

### EstatPartida

L'enumeració `EstatPartida` defineix els diferents estats en què es pot trobar una partida:

```text
PREPARACIO
EN_CURS
FINALITZADA
```

- `PREPARACIO`: els jugadors col·loquen els poblats i les carreteres inicials.
- `EN_CURS`: la preparació ha acabat i comencen els torns normals.
- `FINALITZADA`: un jugador ha arribat als punts necessaris i la partida s'ha acabat.

Aquest enum s'utilitza per comprovar quines accions estan permeses en cada moment de la partida.

### Terreny

L'enumeració `Terreny` defineix els diferents tipus de terreny que poden tenir els hexàgons del tauler:

```text
BOSC
TURONS
PASTURES
CAMPS
MUNTANYES
DESERT
```

Cada terreny està relacionat amb un tipus de recurs:

- `BOSC`: produeix fusta.
- `TURONS`: produeix maó.
- `PASTURES`: produeix llana.
- `CAMPS`: produeix blat.
- `MUNTANYES`: produeix mineral.
- `DESERT`: no produeix cap recurs i és la posició inicial del lladre.

El tipus de terreny es guarda a cada `Hexagon` i s'utilitza per determinar quin recurs produeix quan es tiren els daus.

### TipusRecurs

L'enumeració `TipusRecurs` defineix els cinc recursos disponibles durant la partida:

```text
FUSTA
MAO
LLANA
BLAT
MINERAL
```

Els recursos s'utilitzen principalment per construir carreteres, poblats i ciutats, i també per comerciar amb la banca.

Quan es crea un jugador, es genera un registre `RecursJugador` per a cada tipus de recurs amb quantitat inicial zero.

### TipusCartaDesenvolupament

L'enumeració `TipusCartaDesenvolupament` defineix els diferents tipus de carta de desenvolupament:

```text
CAVALLER
MONOPOLI
PUNT_VICTORIA
```

- `CAVALLER`: permet moure el lladre i augmenta el nombre de cavallers jugats.
- `MONOPOLI`: permet escollir un tipus de recurs i obtenir recursos d'aquest tipus.
- `PUNT_VICTORIA`: afegeix un punt de victòria al jugador propietari.

Aquestes cartes formen part de les funcionalitats avançades del projecte.




## Paquet herencia

El paquet `herencia` conté les classes relacionades amb les construccions del joc.

Hem utilitzat herència perquè les carreteres, els poblats i les ciutats comparteixen alguns atributs, com l'identificador i el jugador propietari.

La jerarquia és la següent:

```text
Construccio
├── Carretera
└── Assentament
    ├── Poblat
    └── Ciutat




## Paquet relacions

El paquet `relacions` conté les entitats principals relacionades amb el funcionament d'una partida.

Les classes d'aquest paquet representen la partida, els jugadors, els seus recursos i les cartes de desenvolupament.

Les entitats principals són:

```text
Partida
Jugador
RecursJugador
CartaDesenvolupament
```

Aquestes classes estan relacionades entre elles mitjançant anotacions JPA com `OneToMany` i `ManyToOne`.





### CartaDesenvolupament

La classe `CartaDesenvolupament` representa una carta de desenvolupament que pertany a una partida.

Cada carta té:

- Un identificador autogenerat.
- Un tipus de carta.
- La partida a la qual pertany.
- Un jugador propietari, si la carta ja ha estat comprada.
- La ronda en què s'ha comprat.
- Un camp que indica si la carta ja s'ha utilitzat.

Els tipus de carta disponibles són:

```text
CAVALLER
MONOPOLI
PUNT_VICTORIA
```

La relació amb `Partida` és `ManyToOne`, perquè una partida pot tenir moltes cartes, però cada carta només pertany a una partida.

La relació amb `Jugador` també és `ManyToOne`, perquè un jugador pot tenir diverses cartes. El propietari pot ser `null` mentre la carta encara està a la pila.

La ronda de compra també pot ser `null` fins que algun jugador compra la carta.

El camp `usada` comença amb valor `false` i permet controlar si una carta ja ha estat jugada.

Aquesta entitat forma part de les funcionalitats avançades del projecte, com comprar cartes, jugar un cavaller, utilitzar un monopoli o calcular els punts de victòria.




### Jugador

La classe `Jugador` representa una persona que participa en una partida de Catan.

Cada jugador guarda les dades principals següents:

- Un identificador autogenerat.
- El nom del jugador.
- El color assignat.
- L'ordre de torn.
- El nombre de cavallers jugats.
- La partida a la qual pertany.

La relació amb `Partida` és `ManyToOne`, perquè una partida pot tenir diversos jugadors, però cada jugador només pertany a una partida.

Cada jugador també té diferents col·leccions relacionades:

```text
Recursos
Construccions
Cartes de desenvolupament
```

La relació amb `RecursJugador` és `OneToMany`. Quan es crea un jugador, s'inicialitzen els cinc tipus de recursos amb quantitat zero:

```text
FUSTA
MAO
LLANA
BLAT
MINERAL
```

La relació amb `Construccio` permet guardar les carreteres, els poblats i les ciutats que pertanyen al jugador.

La relació amb `CartaDesenvolupament` permet guardar les cartes que el jugador ha comprat durant la partida.

També s'han afegit restriccions perquè, dins d'una mateixa partida, no es pugui repetir el color ni l'ordre de torn.

La classe conté mètodes auxiliars per afegir recursos, construccions i cartes, mantenint sincronitzats els dos costats de les relacions.





### Partida

La classe `Partida` representa una partida completa de Catan i controla el seu estat general.

Cada partida guarda les dades principals següents:

- Un identificador autogenerat.
- L'estat actual de la partida.
- La data d'inici.
- La ronda actual.
- L'última tirada de daus.
- Els punts necessaris per guanyar.
- Si els daus ja s'han tirat durant el torn.
- Si hi ha un moviment del lladre pendent.
- El jugador actiu.
- El guanyador de la partida.

Els estats possibles de la partida són:

```text
PREPARACIO
EN_CURS
FINALITZADA
```

La classe `Partida` també conté les col·leccions necessàries per guardar els elements relacionats:

```text
Jugadors
Cartes de desenvolupament
Hexàgons
Vèrtexs
Arestes
```

La relació amb `Jugador` és `OneToMany`, perquè una partida pot tenir entre dos i quatre jugadors.

La relació amb els elements del tauler també és `OneToMany`, ja que cada partida crea i guarda la seva pròpia topologia formada per hexàgons, vèrtexs i arestes.

La partida també guarda el jugador actiu, el possible guanyador i l'hexàgon on es troba el lladre.

S'han afegit mètodes auxiliars per afegir jugadors i elements del tauler, mantenint sincronitzades les relacions entre les entitats.

Quan es crea una partida nova, comença amb l'estat `PREPARACIO`, a la ronda inicial i sense guanyador.




### RecursJugador

La classe `RecursJugador` representa un tipus concret de recurs dins de l'inventari d'un jugador.

Cada registre guarda:

- Un identificador autogenerat.
- El jugador propietari del recurs.
- El tipus de recurs.
- La quantitat disponible.

Els tipus de recurs possibles són:

```text
FUSTA
MAO
LLANA
BLAT
MINERAL



## Paquet taulell

El paquet `taulell` conté les entitats que representen la topologia del tauler de Catan.

El tauler de cada partida està format per:

```text
19 hexàgons
54 vèrtexs
72 arestes
114 relacions entre hexàgons i vèrtexs
```

La topologia es carrega a partir dels fitxers CSV situats a `src/main/resources/csv`.

### Vertex

La classe `Vertex` representa un punt del tauler on es pot construir un poblat o una ciutat.

Cada vèrtex guarda:

- Un identificador autogenerat.
- El codi original del fitxer CSV.
- La partida a la qual pertany.

La relació amb `Partida` és `ManyToOne`, perquè una partida conté molts vèrtexs, però cada vèrtex només pertany a una partida.

La combinació entre la partida i el codi del vèrtex és única. Això permet reutilitzar els mateixos codis dels CSV en partides diferents, però evita repetir-los dins del mateix tauler.

### Aresta

La classe `Aresta` representa una connexió entre dos vèrtexs del tauler.

Cada aresta guarda:

- Un identificador autogenerat.
- El codi original del fitxer CSV.
- Els dos vèrtexs que connecta.
- La partida a la qual pertany.

Les relacions amb els dos vèrtexs són `ManyToOne`, perquè un mateix vèrtex pot formar part de diverses arestes.

Les arestes s'utilitzen per comprovar la connectivitat entre posicions i per determinar on es poden construir les carreteres.

La combinació entre la partida i el codi de l'aresta és única.

### Hexagon

La classe `Hexagon` representa una casella del tauler de Catan.

Cada hexàgon guarda:

- Un identificador autogenerat.
- El codi original del fitxer CSV.
- Les coordenades del tauler.
- El tipus de terreny.
- El número de producció.
- La partida a la qual pertany.

Els terrenys disponibles són:

```text
BOSC
TURONS
PASTURES
CAMPS
MUNTANYES
DESERT
```

La relació amb `Partida` és `ManyToOne`, perquè una partida conté diversos hexàgons, però cada hexàgon només pertany a una partida.

El desert no produeix recursos, no té número de producció i és la posició inicial del lladre.

La combinació entre la partida i el codi de l'hexàgon és única.

### HexagonVertex

La classe `HexagonVertex` representa la relació entre un hexàgon i un dels seus vèrtexs.

Cada relació guarda:

- Un identificador autogenerat.
- L'hexàgon relacionat.
- El vèrtex relacionat.
- La posició del vèrtex dins de l'hexàgon.

Les posicions possibles són:

```text
0
1
2
3
4
5
```

Hem creat aquesta entitat intermèdia perquè la relació entre `Hexagon` i `Vertex` necessita guardar informació addicional: la posició del vèrtex.

Aquesta classe permet saber quins sis vèrtexs envolten cada hexàgon. Aquesta informació s'utilitza per determinar quins poblats i ciutats reben recursos quan un hexàgon produeix.

S'han afegit restriccions per evitar repetir una mateixa posició o un mateix vèrtex dins del mateix hexàgon.


