package dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import relacions.Jugador;
import relacions.Partida;

/**
 * Punt d'entrada i bateria de proves manuals d'integracio del projecte Catan.
 *
 * IMPORTANT:
 * - La configuracio activa es daoHibernate.cfg.xml.
 * - Aquesta execucio crea una partida nova i les dades del tauler associades.
 * - daoHibernate.cfg.xml te hibernate.hbm2ddl.auto=create: Hibernate recrea
 *   l'esquema en iniciar-se. No utilitzeu aquesta configuracio amb dades reals.
 * - Les operacions que gasten recursos o modifiquen construccions no s'executen
 *   automaticament per no deixar la partida de demostracio en un estat parcial.
 */
public class Main {

    private static int provesCorrectes = 0;
    private static int provesFallides = 0;

    public static void main(String[] args) {
        PartidaDAO partidaDAO = null;
        JugadorDAO jugadorDAO = null;

        try {
            imprimirCapcalera("INICI DE LES PROVES D'INTEGRACIO CATAN");

            // La creacio dels DAO inicialitza Hibernate i valida la configuracio.
            partidaDAO = new PartidaDAO();
            jugadorDAO = new JugadorDAO();
            prova("Inicialitzacio de Hibernate i dels DAO", true,
                    "SessionFactory creada correctament");

            // Crear una partida carrega automaticament el tauler des dels CSV.
            imprimirCapcalera("1. CREACIO DE PARTIDA I TAULER");
            List<String> noms = Arrays.asList("Prova_A", "Prova_B", "Prova_C");
            int idPartida = partidaDAO.IniciarPartida(noms);
            prova("Crear partida amb 3 jugadors", idPartida > 0,
                    "ID retornat: " + idPartida);

            if (idPartida <= 0) {
                throw new IllegalStateException(
                        "No s'ha pogut crear la partida. Revisa MySQL, els CSV i daoHibernate.cfg.xml.");
            }

            // Verificar que la partida s'ha persistit.
            Partida partida = partidaDAO.get(idPartida);
            prova("Recuperar la partida per ID", partida != null,
                    "Partida ID " + idPartida);

            // Consultar les quantitats reals a la BD, no les col·leccions lazy
            // d'una entitat que ja pot estar desconnectada de la sessio.
            verificarQuantitatsTauler(idPartida);

            // Consultes que mostren dades recuperades de la BD.
            imprimirCapcalera("2. CONSULTES DE LA PARTIDA I DEL TAULER");
            partidaDAO.MostrarPartida(idPartida);
            partidaDAO.MostrarTauler(idPartida);
            prova("Executar MostrarPartida i MostrarTauler", true,
                    "Els dos metodes han estat invocats; revisa la sortida anterior.");

            // Recuperar jugadors de la partida creada.
            imprimirCapcalera("3. PROVES DELS JUGADORS");
            List<Jugador> totsElsJugadors = jugadorDAO.list();
            List<Jugador> jugadorsPartida = new ArrayList<>();
            if (totsElsJugadors != null) {
                for (Jugador jugador : totsElsJugadors) {
                    if (jugador.getPartida() != null
                            && jugador.getPartida().getId() != null
                            && jugador.getPartida().getId().equals(idPartida)) {
                        jugadorsPartida.add(jugador);
                    }
                }
            }

            prova("Recuperar els 3 jugadors de la partida",
                    jugadorsPartida.size() == 3,
                    "Jugadors trobats: " + jugadorsPartida.size());

            for (Jugador jugador : jugadorsPartida) {
                System.out.println("\nJugador: " + jugador.getNom()
                        + " | ID: " + jugador.getId()
                        + " | Color: " + jugador.getColor()
                        + " | Ordre: " + jugador.getOrdreTorn());

                jugadorDAO.mostrarRecursos(jugador.getId());
                int punts = jugadorDAO.CalcularPunts(jugador.getId());
                prova("Calcular punts de " + jugador.getNom(), punts >= 0,
                        "Punts: " + punts);
            }

            // Consultes informatives de l'estat de la partida.
            imprimirCapcalera("4. CONSULTES D'ESTAT I PUNTUACIO");
            partidaDAO.ComprovarVictoria(idPartida);
            partidaDAO.ExercitMesGran(idPartida);
            prova("Executar comprovacio de victoria", true,
                    "Consulta executada; encara no s'ha forcat cap condicio de victoria.");
            prova("Executar consulta de l'exercit mes gran", true,
                    "Consulta executada amb l'estat actual dels cavallers.");

            // Comprovar les validacions del nombre de jugadors. Aquestes crides
            // han de retornar -1 i no han de crear cap partida.
            imprimirCapcalera("5. PROVES DE VALIDACIO D'ENTRADA");
            int partidaAmbUnJugador = partidaDAO.IniciarPartida(
                    Arrays.asList("Només_un"));
            prova("Rebutjar una partida amb 1 jugador",
                    partidaAmbUnJugador == -1,
                    "Resultat: " + partidaAmbUnJugador);

            int partidaAmbCincJugadors = partidaDAO.IniciarPartida(
                    Arrays.asList("A", "B", "C", "D", "E"));
            prova("Rebutjar una partida amb 5 jugadors",
                    partidaAmbCincJugadors == -1,
                    "Resultat: " + partidaAmbCincJugadors);

            int partidaAmbNomBuit = partidaDAO.IniciarPartida(
                    Arrays.asList("Jugador_valid", " "));
            prova("Rebutjar un nom de jugador buit",
                    partidaAmbNomBuit == -1,
                    "Resultat: " + partidaAmbNomBuit);

            imprimirCapcalera("RESUM DE LES PROVES");
            System.out.println("Correctes: " + provesCorrectes);
            System.out.println("Fallides:  " + provesFallides);
            System.out.println("Partida de demostracio: " + idPartida);
            System.out.println("\nNOTA: les proves de construccio, comerç, torns, daus i cartes "
                    + "requereixen preparar recursos i l'estat de partida. No s'executen "
                    + "automaticament per evitar canvis parcials o resultats aleatoris.");

        } catch (Exception e) {
            System.err.println("\nERROR GENERAL DURANT LES PROVES: " + e.getMessage());
            e.printStackTrace();
            provesFallides++;
        } finally {
            // Tanquem una sola vegada la SessionFactory compartida pels DAO.
            SessionManager.tancar();
            System.out.println("\nSessionFactory tancada.");
        }
    }

    private static void verificarQuantitatsTauler(int idPartida) {
        imprimirCapcalera("COMPROVACIO DE LES QUANTITATS DEL TAULER");
        SessionFactory factory = SessionManager.getSessionFactory();
        try (Session session = factory.openSession()) {
            long vertexs = comptar(session, "Vertex", idPartida);
            long hexagons = comptar(session, "Hexagon", idPartida);
            long relacions = comptar(session, "HexagonVertex", idPartida);
            long arestes = comptar(session, "Aresta", idPartida);

            prova("54 vertexs", vertexs == 54, "Trobats: " + vertexs);
            prova("19 hexagons", hexagons == 19, "Trobats: " + hexagons);
            prova("114 relacions hexagon-vertex", relacions == 114,
                    "Trobades: " + relacions);
            prova("72 arestes", arestes == 72, "Trobades: " + arestes);
        } catch (RuntimeException e) {
            prova("Consultar les quantitats del tauler", false, e.getMessage());
            throw e;
        }
    }

    private static long comptar(Session session, String entitat, int idPartida) {
        String hql;
        if ("HexagonVertex".equals(entitat)) {
            hql = "select count(e) from HexagonVertex e where e.hexagon.partida.id = :idPartida";
        } else {
            hql = "select count(e) from " + entitat
                    + " e where e.partida.id = :idPartida";
        }
        return session.createQuery(hql, Long.class)
                .setParameter("idPartida", idPartida)
                .getSingleResult();
    }

    private static void prova(String nom, boolean correcta, String detall) {
        if (correcta) {
            provesCorrectes++;
            System.out.println("[OK]   " + nom + " -- " + detall);
        } else {
            provesFallides++;
            System.err.println("[ERROR] " + nom + " -- " + detall);
        }
    }

    private static void imprimirCapcalera(String titol) {
        System.out.println("\n==================================================");
        System.out.println(titol);
        System.out.println("==================================================");
    }
}
