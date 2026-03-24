package control;

import java.awt.event.*;
import javax.swing.*;
import model.ActionType;
import model.Map;
import model.Personnage;
import view.FenetreInvocation;
import view.MapPanel;
/**
 * Gestion des interactions souris sur la map.
 * Gere la selection de tuiles au survol et au clic.
 */
public class ReactionClic implements MouseMotionListener, MouseListener {

    private final Map gameMap;
    private final MapPanel mapPanel;
    private final int tileSize;
    private final int borderPad;
    private final int titleHeight;

    public ReactionClic(Map gameMap, MapPanel mapPanel, int tileSize, int borderPad, int titleHeight) {
        this.gameMap = gameMap;
        this.mapPanel = mapPanel;
        this.tileSize = tileSize;
        this.borderPad = borderPad;
        this.titleHeight = titleHeight;
    }

    // ── MouseMotionListener ────────────────────────────────────────────────────

    @Override
    public void mouseMoved(MouseEvent e) {
        // Convertir position pixel en coordonnees tuile
        int tileX = (e.getX() - borderPad) / tileSize;
        int tileY = (e.getY() - borderPad - titleHeight) / tileSize;

        // Verifier si la position est valide sur la map
        if (gameMap.isValidPosition(tileX, tileY)) {
            mapPanel.setSelectedPosition(tileX, tileY);
            // Survol personnage ?
            Personnage p = gameMap.getPersonnageAt(tileX, tileY);
            mapPanel.setHoveredPersonnage(p);
        } else {
            mapPanel.clearSelection();
            mapPanel.setHoveredPersonnage(null);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Pas d'action pour le drag pour l'instant
    }

    // ── MouseListener ──────────────────────────────────────────────────────────

    @Override
    public void mouseClicked(MouseEvent e) {
        int tileX = (e.getX() - borderPad) / tileSize;
        int tileY = (e.getY() - borderPad - titleHeight) / tileSize;

        if (!gameMap.isValidPosition(tileX, tileY)) return;

        if (gameMap.estAutelInvocation(tileX, tileY) && SwingUtilities.isLeftMouseButton(e)) {
            FenetreInvocation fenetreInvocation = new FenetreInvocation(gameMap, mapPanel);
            fenetreInvocation.setVisible(true);
            return;
        }

        // Clic sur l'Hotel de Ville → dialogue d'amelioration
        if (gameMap.estHotelDeVille(tileX, tileY) && SwingUtilities.isLeftMouseButton(e)) {
            ouvrirDialogueHotelDeVille();
            return;
        }

        // Clic sur un bâtiment non construit → dialogue de construction
        if (gameMap.getTerrainAt(tileX, tileY) == model.Map.BATIMENT
                && !gameMap.estConstruit(tileX, tileY)
                && SwingUtilities.isLeftMouseButton(e)) {
            ouvrirDialogueConstruction(tileX, tileY);
            return;
        }

        Personnage clicked = gameMap.getPersonnageAt(tileX, tileY);
        if (clicked != null) {
            mapPanel.setSelectedPersonnage(clicked);

            if (clicked.isPretARecuperer()) {
                int gain = gameMap.recupererRecompenseEtRappeler(clicked);
                String msg;
                if (clicked.getActionCourante() == ActionType.DEFENDRE || gain == 0) {
                    msg = "Mission terminée. Le personnage est retourné à l'autel.";
                } else {
                    msg = "Récompense récupérée : +" + gain + ". Le personnage retourne à l'autel.";
                }
                JOptionPane.showMessageDialog(mapPanel, msg);
                mapPanel.setSelectedPersonnage(null);
                mapPanel.repaint();
                return;
            }

            if (SwingUtilities.isLeftMouseButton(e)) {
                ouvrirDialogueActions(clicked);
            }
            mapPanel.repaint();
            return;
        }
    }

    private void ouvrirDialogueConstruction(int tileX, int tileY) {
        String type = gameMap.getTypeBatiment(tileX, tileY);
        if (type == null) return;

        int[] cout = gameMap.getCoutConstruction(tileX, tileY);
        if (cout == null) return;

        model.Inventaire inv = gameMap.getInventaire();

        // Nom lisible du bâtiment
        String nom = switch (type) {
            case model.Map.TYPE_MAISON         -> "Maison";
            case model.Map.TYPE_ENTREPOT_BOIS  -> "Entrepôt de Bois";
            case model.Map.TYPE_ENTREPOT_FER   -> "Entrepôt de Fer";
            case model.Map.TYPE_ENTREPOT_OR    -> "Trésorerie (Or)";
            case model.Map.TYPE_ENTREPOT_NOURR -> "Grenier (Nourriture)";
            default -> type;
        };

        // Vérifier si le joueur a assez
        boolean assezBois = inv == null || inv.getBois() >= cout[0];
        boolean assezFer  = inv == null || inv.getFer()  >= cout[1];
        boolean assezOr   = inv == null || inv.getOr()   >= cout[2];
        boolean peutConstruire = assezBois && assezFer && assezOr;

        // Panneau principal
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new java.awt.GridLayout(0, 1, 4, 4));

        panel.add(new javax.swing.JLabel("<html><b>Construire : " + nom + "</b></html>"));
        panel.add(new javax.swing.JLabel(" "));

        // Ligne ressource : couleur rouge si manque
        String colorBois = assezBois ? "green" : "red";
        String colorFer  = assezFer  ? "green" : "red";
        String colorOr   = assezOr   ? "green" : "red";

        int stockBois = inv != null ? inv.getBois() : 0;
        int stockFer  = inv != null ? inv.getFer()  : 0;
        int stockOr   = inv != null ? inv.getOr()   : 0;

        // N'afficher que les ressources dont le coût est > 0
        if (cout[0] > 0)
            panel.add(new javax.swing.JLabel("<html>🪵 Bois : <font color='" + colorBois + "'><b>" + cout[0] + "</b></font> &nbsp; (vous avez : " + stockBois + ")</html>"));
        if (cout[1] > 0)
            panel.add(new javax.swing.JLabel("<html>⚙ Fer  : <font color='" + colorFer  + "'><b>" + cout[1] + "</b></font> &nbsp; (vous avez : " + stockFer  + ")</html>"));
        if (cout[2] > 0)
            panel.add(new javax.swing.JLabel("<html>🪙 Or   : <font color='" + colorOr   + "'><b>" + cout[2] + "</b></font> &nbsp; (vous avez : " + stockOr   + ")</html>"));

        if (!peutConstruire)
            panel.add(new javax.swing.JLabel("<html><font color='red'><i>Ressources insuffisantes.</i></font></html>"));

        // Bouton construire activé seulement si assez de ressources
        javax.swing.JButton btnConstruire = new javax.swing.JButton("Construire");
        btnConstruire.setEnabled(peutConstruire);

        Object[] options = { btnConstruire, "Annuler" };

        javax.swing.JOptionPane pane = new javax.swing.JOptionPane(
            panel,
            javax.swing.JOptionPane.PLAIN_MESSAGE,
            javax.swing.JOptionPane.OK_CANCEL_OPTION,
            null,
            options,
            peutConstruire ? btnConstruire : options[1]
        );

        javax.swing.JDialog dialog = pane.createDialog(mapPanel, "Construction");

        btnConstruire.addActionListener(ev -> {
            String erreur = gameMap.tenterConstruction(tileX, tileY);
            dialog.dispose();
            if (erreur != null) {
                javax.swing.JOptionPane.showMessageDialog(mapPanel, erreur, "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
            } else {
                javax.swing.JOptionPane.showMessageDialog(mapPanel, nom + " construit !", "Construction", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
            mapPanel.repaint();
        });

        dialog.setVisible(true);
    }

    private void ouvrirDialogueHotelDeVille() {
        model.HotelDeVille hdv = gameMap.getHotelDeVille();
        int niveau = hdv.getNiveau();
        int cap = hdv.getCapaciteBase();
        String capStr = (cap < 0) ? "Illimitée" : (cap + " par ressource");

        if (hdv.estAuNiveauMax()) {
            JOptionPane.showMessageDialog(mapPanel,
                "Hôtel de Ville — Niveau MAX (" + niveau + "/" + model.HotelDeVille.NIVEAU_MAX + ")\n"
                + "Capacité de stockage : " + capStr,
                "Hôtel de Ville",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int coutFer = hdv.getCoutAmelioration();
        int choix = JOptionPane.showConfirmDialog(mapPanel,
            "Hôtel de Ville — Niveau " + niveau + "/" + model.HotelDeVille.NIVEAU_MAX + "\n"
            + "Capacité actuelle : " + capStr + "\n\n"
            + "Améliorer au niveau " + (niveau + 1) + " ?\n"
            + "Coût : " + coutFer + " fer",
            "Améliorer l'Hôtel de Ville",
            JOptionPane.YES_NO_OPTION);

        if (choix == JOptionPane.YES_OPTION) {
            String resultat = gameMap.ameliorerHotelDeVille();
            JOptionPane.showMessageDialog(mapPanel, resultat, "Hôtel de Ville", JOptionPane.INFORMATION_MESSAGE);
            mapPanel.repaint();
        }
    }

    private void ouvrirDialogueActions(Personnage p) {
        String titre = "Actions - " + p.getNom() + " (" + p.getRareteEtoiles() + "★)";

        // 3 boutons = 3 actions (version minimale)
        Object[] options = {
                ActionType.COUPER_BOIS.getLabel(),
                ActionType.MINER_FER.getLabel(),
                ActionType.DEFENDRE.getLabel(),
                "Rappeler"
        };

        int choix = JOptionPane.showOptionDialog(
                mapPanel,
                "Choisis une action :\n\nAction actuelle : " + p.getActionCourante().getLabel(),
                titre,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choix == 3) {
            gameMap.rappelerPersonnage(p);
            mapPanel.setSelectedPersonnage(null);
            mapPanel.repaint();
            return;
        }

        ActionType nouvelleAction = null;
        if (choix == 0) p.setActionCourante(ActionType.COUPER_BOIS);
        else if (choix == 1) p.setActionCourante(ActionType.MINER_FER);
        else if (choix == 2) p.setActionCourante(ActionType.DEFENDRE);

        if (nouvelleAction == null) return;

        if (p.getActionCourante() == nouvelleAction && (p.isEnExecution() || !p.estArrive() || p.isPretARecuperer())) {
            JOptionPane.showMessageDialog(mapPanel, "Ce personnage effectue déjà cette action.");
            return;
        }

        if (p.getActionCourante() != null && p.getActionCourante() != nouvelleAction
                && (p.isEnExecution() || !p.estArrive() || p.isPretARecuperer())) {

            int confirmation = JOptionPane.showConfirmDialog(
                    mapPanel,
                    "Ce personnage est déjà occupé.\nInterrompre l'action en cours ?\nSi tu interromps, aucune ressource ne sera obtenue.",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }

            p.interrompreAction();
            p.setPretARecuperer(false);
        }

        gameMap.deployerPersonnage(p, nouvelleAction);
        mapPanel.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Pas d'action pour l'instant
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Pas d'action pour l'instant
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Pas d'action pour l'instant
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Efface la selection quand la souris sort de la map
        mapPanel.clearSelection();
        mapPanel.setHoveredPersonnage(null);
    }
}