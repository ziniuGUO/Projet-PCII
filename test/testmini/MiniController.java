package test.testmini;

import model.ActionType;
import model.Personnage;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Le "cerveau" de notre petite carte : c'est lui qui fait le lien entre
 * les mouvements de la souris et ce qui se passe réellement dans le modèle.
 */
public class MiniController extends MouseAdapter {

    private final MiniMap map;
    private final MiniMapPanel panel;

    public MiniController(MiniMap map, MiniMapPanel panel) {
        this.map = map;
        this.panel = panel;
    }

    /* Petite moulinette pour transformer les pixels X de la souris en index de case */
    private int toTileX(int mouseX) {
        int x0 = panel.getPad();
        return (mouseX - x0) / panel.getTileSize();
    }

    /* Même chose pour l'axe Y, avec un petit décalage pour compenser le titre */
    private int toTileY(int mouseY) {
        int y0 = panel.getPad() + 30;
        return (mouseY - y0) / panel.getTileSize();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int tx = toTileX(e.getX());
        int ty = toTileY(e.getY());

        /* Si on sort de la grille, on nettoie le survol pour pas avoir de restes */
        if (!map.isValid(tx, ty)) {
            panel.setHovered(null, -1, -1);
            return;
        }

        /* On regarde s'il y a quelqu'un sous le curseur pour mettre la case en surbrillance */
        Personnage p = map.getPersonnageAt(tx, ty);
        panel.setHovered(p, tx, ty);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int tx = toTileX(e.getX());
        int ty = toTileY(e.getY());

        /* Clic en dehors ? On ne fait rien. */
        if (!map.isValid(tx, ty)) return;

        Personnage clicked = map.getPersonnageAt(tx, ty);

        // 1) Cas où on clique sur un perso : on le sélectionne et on demande ce qu'il doit faire
        if (clicked != null) {
            // Un deuxième clic sur le même perso le désélectionne
            if (panel.getSelected() == clicked) panel.setSelected(null);
            else panel.setSelected(clicked);

            // On ouvre la petite fenêtre pour choisir l'ordre à donner
            choisirAction(clicked);

            panel.repaint();
            return;
        }

        // 2) Cas où on clique sur une case vide : si on a un perso sélectionné, on le déplace
        Personnage selected = panel.getSelected();
        if (selected != null) {
            selected.setPosition(tx, ty);

            // Optionnel : on pourrait désélectionner ici, mais pour l'instant on garde le focus
            panel.repaint();
        }
    }

    /**
     * Affiche une boîte de dialogue pour donner des ordres au personnage.
     * C'est ici qu'on définit les métiers : bûcheron, mineur ou défenseur.
     */
    private void choisirAction(Personnage p) {
        /* On récupère les textes à afficher sur les boutons depuis notre énumération */
        Object[] options = {
                ActionType.COUPER_BOIS.getLabel(),
                ActionType.MINER_FER.getLabel(),
                ActionType.DEFENDRE.getLabel()
        };

        /* Une petite fenêtre "OptionDialog" pour bloquer le jeu le temps du choix */
        int choix = JOptionPane.showOptionDialog(
                panel,
                "Choisis une action :\n\nAction actuelle : " + p.getActionCourante().getLabel(),
                "Actions - " + p.getNom() + " (" + p.getRareteEtoiles() + "★)",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        /* On met à jour l'état du personnage selon le bouton cliqué */
        if (choix == 0) p.setActionCourante(ActionType.COUPER_BOIS);
        else if (choix == 1) p.setActionCourante(ActionType.MINER_FER);
        else if (choix == 2) p.setActionCourante(ActionType.DEFENDRE);

        // Note : si choix == -1, c'est que l'utilisateur a fermé la croix, donc on ne change rien.
    }
}