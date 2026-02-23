package test.testmini;

import model.Personnage;

import javax.swing.*;
import java.awt.*;

/**
 * C'est ici que la magie visuelle opère !
 * Ce panneau s'occupe de dessiner la grille, les personnages et les retours visuels (survol, sélection).
 */
public class MiniMapPanel extends JPanel {

    /* On définit la taille d'une case et la marge intérieure du panneau */
    private static final int TILE_SIZE = 48;
    private static final int PAD = 20;

    private final MiniMap map;

    /* Pour mémoriser ce que l'utilisateur survole ou sélectionne avec sa souris */
    private Personnage hovered = null;
    private Personnage selected = null;
    private int hoverX = -1, hoverY = -1;

    public MiniMapPanel(MiniMap map) {
        this.map = map;
        /* On calcule la taille de la fenêtre pour qu'elle s'adapte parfaitement à la grille */
        int w = PAD * 2 + map.getWidth() * TILE_SIZE;
        int h = PAD * 2 + map.getHeight() * TILE_SIZE + 40;
        setPreferredSize(new Dimension(w, h));
        /* Un fond sombre pour faire ressortir les couleurs du jeu */
        setBackground(new Color(45, 35, 25));
    }

    public int getTileSize() { return TILE_SIZE; }
    public int getPad() { return PAD; }

    /**
     * Met à jour la case survolée et demande un rafraîchissement de l'image.
     */
    public void setHovered(Personnage p, int x, int y) {
        this.hovered = p;
        this.hoverX = x;
        this.hoverY = y;
        repaint(); // On redessine tout dès que la souris bouge
    }

    public Personnage getSelected() { return selected; }

    public void setSelected(Personnage p) {
        this.selected = p;
        repaint(); // On redessine pour afficher le cercle de sélection
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        /* On active l'anti-aliasing pour que les ronds soient bien lisses */
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /* On dessine les couches les unes après les autres (le "Z-order") */
        drawTitle(g2);      // Le titre en haut
        drawTiles(g2);      // L'herbe
        drawGrid(g2);       // Les lignes de la grille
        drawPersonnages(g2); // Les petits bonshommes
        drawSelection(g2);  // L'indicateur de sélection (si besoin)
        drawTooltip(g2);    // La barre d'infos en bas
    }

    private void drawTitle(Graphics2D g2) {
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        g2.setColor(new Color(255, 215, 60)); // Un beau jaune doré
        g2.drawString("Mini Test - Personnage (hover / select / move)", PAD, 24);
    }

    private void drawTiles(Graphics2D g2) {
        int x0 = PAD;
        int y0 = PAD + 30; // On descend un peu pour laisser de la place au titre
        Color grass = new Color(144, 190, 109);

        /* On remplit chaque case avec un rectangle vert */
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                int px = x0 + x * TILE_SIZE;
                int py = y0 + y * TILE_SIZE;
                g2.setColor(grass);
                g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawGrid(Graphics2D g2) {
        int x0 = PAD;
        int y0 = PAD + 30;
        int x1 = x0 + map.getWidth() * TILE_SIZE;
        int y1 = y0 + map.getHeight() * TILE_SIZE;

        /* On dessine des lignes noires très fines et transparentes pour la grille */
        g2.setColor(new Color(0, 0, 0, 60));
        g2.setStroke(new BasicStroke(1f));

        for (int x = 0; x <= map.getWidth(); x++) {
            g2.drawLine(x0 + x * TILE_SIZE, y0, x0 + x * TILE_SIZE, y1);
        }
        for (int y = 0; y <= map.getHeight(); y++) {
            g2.drawLine(x0, y0 + y * TILE_SIZE, x1, y0 + y * TILE_SIZE);
        }
    }

    private void drawPersonnages(Graphics2D g2) {
        int x0 = PAD;
        int y0 = PAD + 30;

        for (Personnage p : map.getPersonnages()) {
            int px = x0 + p.getX() * TILE_SIZE;
            int py = y0 + p.getY() * TILE_SIZE;

            /* On laisse une petite marge pour que le perso ne colle pas aux bords de la case */
            int margin = 8;
            int size = TILE_SIZE - margin * 2;
            int cx = px + margin;
            int cy = py + margin;

            // Petit effet d'ombre portée pour donner du relief
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillOval(cx + 2, cy + 3, size, size);

            // Le corps du personnage (un cercle bleu)
            g2.setColor(new Color(60, 120, 200));
            g2.fillOval(cx, cy, size, size);

            // Un petit contour blanc pour la finition
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx, cy, size, size);
            g2.setStroke(new BasicStroke(1f));

            // On affiche un "V" (pour Villageois) au centre du cercle
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            String letter = "V";
            int tx = px + (TILE_SIZE - fm.stringWidth(letter)) / 2;
            int ty = py + (TILE_SIZE + fm.getAscent()) / 2 - 4;
            g2.drawString(letter, tx, ty);
        }
    }

    private void drawSelection(Graphics2D g2) {
        /* Si aucun perso n'est sélectionné, on n'affiche rien */
        if (selected == null) return;

        int x0 = PAD;
        int y0 = PAD + 30;
        int px = x0 + selected.getX() * TILE_SIZE;
        int py = y0 + selected.getY() * TILE_SIZE;

        /* Un cercle doré épais pour bien montrer qui est le "chef" */
        g2.setColor(new Color(255, 215, 0, 200));
        g2.setStroke(new BasicStroke(4f));
        g2.drawOval(px + 4, py + 4, TILE_SIZE - 8, TILE_SIZE - 8);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawTooltip(Graphics2D g2) {
        /* On ne dessine l'infobulle que si la souris est sur la grille */
        if (hoverX < 0 || hoverY < 0) return;

        String text;
        if (hovered != null) {
            /* On affiche le nom, les étoiles, l'action et les coordonnées */
            text = String.format("%s | %d★ | %s | (%d,%d)",
                    hovered.getNom(),
                    hovered.getRareteEtoiles(),
                    hovered.getActionCourante().getLabel(),
                    hovered.getX(),
                    hovered.getY());
        } else {
            /* Case vide : on affiche juste les coordonnées */
            text = String.format("(%d,%d) - Herbe", hoverX, hoverY);
        }

        g2.setFont(new Font("Serif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text) + 16;
        int th = fm.getHeight() + 8;

        /* On place l'infobulle tout en bas du panneau */
        int bx = PAD;
        int by = getHeight() - PAD - th;

        // Rectangle noir arrondi et semi-transparent
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(bx, by, tw, th, 10, 10);

        // Texte en jaune pour la lisibilité
        g2.setColor(new Color(255, 215, 60));
        g2.drawString(text, bx + 8, by + fm.getAscent() + 2);
    }
}