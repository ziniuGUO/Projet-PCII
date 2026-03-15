package test.testmini;

import model.ActionType;
import model.Personnage;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * C'est le moteur graphique de notre module.
 * Ce panneau gère l'affichage des tuiles, des personnages et de l'interface (HUD).
 */
public class MiniMapPanel extends JPanel {

    // On garde tes constantes de mise en page d'origine
    private static final int TILE = 60;
    private static final int PAD = 30;
    private static final int TOP = 40;

    private final MiniMap map;

    private int selectedX = -1;
    private int selectedY = -1;
    private Personnage hoveredPersonnage = null;
    private Personnage selectedPersonnage = null;

    public MiniMapPanel(MiniMap map) {
        this.map = map;
        // Calcul de la taille de la fenêtre pour qu'elle s'adapte à la grille
        int w = PAD * 2 + map.getWidth() * TILE + 220;
        int h = PAD * 2 + TOP + map.getHeight() * TILE;
        setPreferredSize(new Dimension(w, h));
        setBackground(new Color(46, 30, 16)); // Fond sombre pour faire ressortir la carte
    }

    /* Getters pour le contrôleur */
    public int getTileSize() { return TILE; }
    public int getPad() { return PAD; }
    public int getTop() { return TOP; }

    /* Méthodes de mise à jour d'état (appelées par le contrôleur) */
    public void setSelectedPosition(int x, int y) {
        selectedX = x;
        selectedY = y;
        repaint();
    }

    public void clearSelection() {
        selectedX = -1;
        selectedY = -1;
        repaint();
    }

    public void setHoveredPersonnage(Personnage p) {
        hoveredPersonnage = p;
        repaint();
    }

    public void setSelectedPersonnage(Personnage p) {
        selectedPersonnage = p;
        repaint();
    }

    public Personnage getSelectedPersonnage() {
        return selectedPersonnage;
    }

    /**
     * La méthode centrale de dessin (Pipeline de rendu).
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Anti-aliasing pour que les ronds des persos ne soient pas "pixelisés"
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawTitle(g2);
        drawTiles(g2);       // Le sol et les ressources
        drawGrid(g2);        // Le quadrillage
        drawPersonnages(g2); // Les unités mobiles
        drawSelection(g2);   // Le curseur de sélection
        drawTooltip(g2);     // L'info-bulle contextuelle
        drawNotification(g2);// Les messages système
        drawInfos(g2);       // Le panneau de ressources à droite
    }

    private void drawTitle(Graphics2D g2) {
        g2.setFont(new Font("Serif", Font.BOLD, 24));
        g2.setColor(new Color(255, 220, 80));
        g2.drawString("Mini Test - Autel d'invocation", PAD, 26);
    }

    private void drawTiles(Graphics2D g2) {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                int px = PAD + x * TILE;
                int py = PAD + TOP + y * TILE;
                int type = map.getTerrainAt(x, y);

                Color color;
                String label = null;

                // Logique de couleur selon le type de terrain
                if (type == MiniMap.HERBE) {
                    color = new Color(146, 190, 112);
                } else if (type == MiniMap.BOIS) {
                    // La couleur du bois change selon la quantité restante
                    float taux = (float) map.getStockBoisForet() / map.getStockBoisForetMax();
                    color = new Color(20, 90 + (int) (120 * taux), 20);
                    label = "BOIS " + map.getStockBoisForet();
                } else {
                    color = new Color(160, 160, 160); // Gris pour l'Autel
                    label = "AUTEL";
                }

                // Surbrillance pour aider le joueur à voir où envoyer ses persos
                ActionType action = null;
                if (selectedPersonnage != null && selectedPersonnage.isDeploye()) {
                    action = selectedPersonnage.getActionCourante();
                } else if (map.getPersonnageMiseEnValeur() != null) {
                    action = map.getActionMiseEnValeur();
                }

                if (map.caseCorrespondAAction(type, action)) {
                    color = new Color(40, 170, 40); // Vert plus vif
                }

                g2.setColor(color);
                g2.fillRect(px, py, TILE, TILE);
                g2.setColor(new Color(0, 0, 0, 50));
                g2.drawRect(px, py, TILE, TILE);

                if (label != null) {
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    g2.setColor(type == MiniMap.AUTEL ? new Color(120, 20, 120) : Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = px + (TILE - fm.stringWidth(label)) / 2;
                    int ty = py + TILE / 2;
                    g2.drawString(label, tx, ty);
                }
            }
        }
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 35));
        int x0 = PAD;
        int y0 = PAD + TOP;
        int x1 = x0 + map.getWidth() * TILE;
        int y1 = y0 + map.getHeight() * TILE;

        for (int x = 0; x <= map.getWidth(); x++) {
            g2.drawLine(x0 + x * TILE, y0, x0 + x * TILE, y1);
        }
        for (int y = 0; y <= map.getHeight(); y++) {
            g2.drawLine(x0, y0 + y * TILE, x1, y0 + y * TILE);
        }
    }

    private void drawPersonnages(Graphics2D g2) {
        for (Personnage p : map.getPersonnages()) {
            if (!p.isDeploye()) continue;

            int px = PAD + p.getX() * TILE;
            int py = PAD + TOP + p.getY() * TILE;

            int margin = 10;
            int size = TILE - margin * 2;
            int cx = px + margin;
            int cy = py + margin;

            // Feedback couleur selon l'état de l'unité
            if (p.isPretARecuperer()) {
                g2.setColor(new Color(255, 170, 60)); // Orange : Travail fini
            } else if (p.isEnExecution()) {
                g2.setColor(new Color(220, 120, 120)); // Rouge : En cours
            } else {
                g2.setColor(new Color(244, 180, 60)); // Jaune : En mouvement
            }

            g2.fillOval(cx, cy, size, size);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx, cy, size, size);

            // On affiche le nom abrégé
            String affichage = getNomAffiche(p);
            g2.setFont(new Font("SansSerif", Font.BOLD, affichage.length() == 1 ? 18 : 14));
            FontMetrics fm = g2.getFontMetrics();
            int tx = px + (TILE - fm.stringWidth(affichage)) / 2;
            int ty = py + (TILE + fm.getAscent()) / 2 - 4;
            g2.drawString(affichage, tx, ty);
        }
    }

    /**
     * Détermine si on affiche une ou deux lettres pour identifier le perso.
     */
    private String getNomAffiche(Personnage personnage) {
        String nom = personnage.getNom();
        if (nom == null || nom.isBlank()) return "?";

        String premiere = nom.substring(0, 1).toUpperCase();
        boolean conflit = false;
        List<Personnage> persos = map.getPersonnages();

        for (Personnage autre : persos) {
            if (autre == personnage) continue;
            String autreNom = autre.getNom();
            if (autreNom != null && !autreNom.isBlank() && autreNom.substring(0, 1).equalsIgnoreCase(premiere)) {
                conflit = true;
                break;
            }
        }

        if (!conflit) return premiere;
        return (nom.length() >= 2) ? nom.substring(0, 1).toUpperCase() + nom.substring(1, 2).toLowerCase() : premiere;
    }

    private void drawSelection(Graphics2D g2) {
        if (selectedX < 0 || selectedY < 0) return;
        int px = PAD + selectedX * TILE;
        int py = PAD + TOP + selectedY * TILE;

        g2.setColor(new Color(255, 120, 40, 220));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(px + 2, py + 2, TILE - 4, TILE - 4);
    }

    private void drawTooltip(Graphics2D g2) {
        if (selectedX < 0) return;

        String text;
        if (hoveredPersonnage != null) {
            String etat = hoveredPersonnage.isPretARecuperer() ? "Mission terminee" :
                    (hoveredPersonnage.isEnExecution() ? "Execution" : "Deplacement");
            text = hoveredPersonnage.getNom() + " | " + hoveredPersonnage.getRareteEtoiles() + "★ | " + etat;
        } else {
            int type = map.getTerrainAt(selectedX, selectedY);
            text = (type == MiniMap.BOIS) ? "Zone bois" : (type == MiniMap.AUTEL ? "Autel d'invocation" : "Herbe");
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(text) + 16;
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(PAD, getHeight() - 46, w, 26, 8, 8);
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(text, PAD + 8, getHeight() - 28);
    }

    private void drawNotification(Graphics2D g2) {
        String message = map.getNotificationMessage();
        if (message == null || message.isBlank()) return;

        int boxW = 320, boxH = 55;
        int bx = PAD + map.getWidth() * TILE - boxW;
        int by = 8;

        g2.setColor(new Color(20, 20, 20, 210));
        g2.fillRoundRect(bx, by, boxW, boxH, 12, 12);
        g2.setColor(new Color(255, 215, 0));
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("Notification", bx + 12, by + 18);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String txt = message.length() > 42 ? message.substring(0, 42) + "..." : message;
        g2.drawString(txt, bx + 12, by + 36);
        g2.drawString("Clique sur le personnage pour recuperer.", bx + 12, by + 50);
    }

    private void drawInfos(Graphics2D g2) {
        int bx = PAD + map.getWidth() * TILE + 20;
        int by = PAD + TOP + 20;

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(bx - 10, by - 20, 160, 110, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Infos", bx, by);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.drawString("Or : " + map.getStockOr(), bx, by + 24);
        g2.drawString("Bois foret : " + map.getStockBoisForet(), bx, by + 46);
        g2.drawString("Bois joueur : " + map.getStockBoisJoueur(), bx, by + 68);
    }
}