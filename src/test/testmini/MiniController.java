package test.testmini;

import model.ActionType;
import model.Personnage;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * C'est ici qu'on gère toute l'intelligence des clics et des mouvements de souris.
 * Le contrôleur fait le pont entre le panneau d'affichage et la logique de la carte.
 */
public class MiniController extends MouseAdapter {

    private final MiniMap map;
    private final MiniMapPanel panel;

    public MiniController(MiniMap map, MiniMapPanel panel) {
        this.map = map;
        this.panel = panel;
    }

    /* Calcule la colonne (X) de la grille en fonction des pixels de la souris */
    private int toTileX(int mouseX) {
        return (mouseX - panel.getPad()) / panel.getTileSize();
    }

    /* Calcule la ligne (Y) en tenant compte de la marge du haut pour le titre */
    private int toTileY(int mouseY) {
        return (mouseY - panel.getPad() - panel.getTop()) / panel.getTileSize();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = toTileX(e.getX());
        int y = toTileY(e.getY());

        // On vérifie qu'on ne survole pas le vide en dehors de la carte
        if (map.isValidPosition(x, y)) {
            panel.setSelectedPosition(x, y);
            panel.setHoveredPersonnage(map.getPersonnageAt(x, y));
        } else {
            // Nettoyage si la souris quitte la zone de jeu
            panel.clearSelection();
            panel.setHoveredPersonnage(null);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = toTileX(e.getX());
        int y = toTileY(e.getY());

        if (!map.isValidPosition(x, y)) return;

        // Cas 1 : On clique sur l'Autel pour invoquer de nouveaux héros
        if (map.estAutel(x, y)) {
            MiniInvocationFrame frame = new MiniInvocationFrame(map, panel);
            frame.setVisible(true);
            return;
        }

        Personnage p = map.getPersonnageAt(x, y);
        if (p == null) return;

        // Focus visuel sur le personnage cliqué
        panel.setSelectedPersonnage(p);

        // Cas 2 : Le personnage a fini son travail (récolte prête)
        if (p.isPretARecuperer()) {
            ActionType ancienneAction = p.getActionCourante();
            int gain = map.recupererRecompenseEtRappeler(p);

            String msg;
            if (ancienneAction == ActionType.COUPER_BOIS) {
                msg = "Recompense recuperee : +" + gain + " bois. Le personnage retourne a l'autel.";
            } else {
                msg = "Mission terminee. Le personnage retourne a l'autel.";
            }

            JOptionPane.showMessageDialog(panel, msg);

            // On remet l'interface à zéro après la récupération
            panel.setSelectedPersonnage(null);
            panel.setHoveredPersonnage(null);
            panel.clearSelection();
            panel.repaint();
            return;
        }

        // Cas 3 : On veut donner un nouvel ordre au personnage
        ouvrirMenuActions(p);
    }

    /**
     * Ouvre une petite fenêtre contextuelle pour décider du sort de notre unité.
     */
    private void ouvrirMenuActions(Personnage p) {
        Object[] options = {
                ActionType.COUPER_BOIS.getLabel(),
                ActionType.DEFENDRE.getLabel(),
                "Rappeler" // Pour ramener manuellement le perso à l'autel
        };

        int choix = JOptionPane.showOptionDialog(
                panel,
                "Choisis une action :",
                "Actions - " + p.getNom(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        // Si l'utilisateur choisit "Rappeler"
        if (choix == 2) {
            map.rappelerPersonnage(p);
            panel.setSelectedPersonnage(null);
            panel.setHoveredPersonnage(null);
            panel.clearSelection();
            panel.repaint();
            return;
        }

        ActionType nouvelleAction = null;
        if (choix == 0) nouvelleAction = ActionType.COUPER_BOIS;
        else if (choix == 1) nouvelleAction = ActionType.DEFENDRE;

        if (nouvelleAction == null) return; // L'utilisateur a fermé la fenêtre sans choisir

        // Vérification de sécurité : on ne relance pas une action déjà en cours
        if (p.getActionCourante() == nouvelleAction
                && (p.isEnExecution() || !p.estArrive() || p.isPretARecuperer())) {
            JOptionPane.showMessageDialog(panel, "Ce personnage effectue deja cette action.");
            return;
        }

        // Gestion de l'interruption : si le perso fait autre chose, on demande confirmation
        if (p.getActionCourante() != null && p.getActionCourante() != nouvelleAction
                && (p.isEnExecution() || !p.estArrive() || p.isPretARecuperer())) {

            int confirmation = JOptionPane.showConfirmDialog(
                    panel,
                    "Ce personnage est deja occupe.\nInterrompre l'action en cours ?\nAucune ressource ne sera obtenue.",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmation != JOptionPane.YES_OPTION) {
                return; // On annule le changement d'ordre
            }

            // On stoppe tout pour préparer le changement de tâche
            p.interrompreAction();
            p.setPretARecuperer(false);
        }

        // Enfin, on envoie le personnage vers sa nouvelle mission
        map.deployerPersonnage(p, nouvelleAction);
        panel.repaint();
    }
}