package view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import model.ActionType;
import model.Map;
import model.Personnage;

public class MapPanel extends JPanel {

    private static final int TILE_SIZE  = 48;
    private static final int BORDER_PAD = 40;
    private static final int TITLE_H    = 50;

    private static final Color COL_HERBE_JOUR = new Color(144, 190, 109);
    private static final Color COL_HERBE_NUIT = new Color( 82,   0, 171);
    private static final Color COL_BATIMENT   = new Color(160, 160, 160);
    private static final Color COL_BOIS       = new Color( 34, 139,  34);
    private static final Color COL_FER        = new Color(105, 105, 105);
    private static final Color COL_OR         = new Color(255, 215,   0);
    private static final Color COL_NOURRITURE = new Color(160, 120,  60);

    // couleurs des batiments construits
    private static final Color COL_HOTEL_VILLE = new Color(180,  60,  60);
    private static final Color COL_MAISON      = new Color(200, 180, 140);
    private static final Color COL_ENTREPOT    = new Color(100, 140, 180);
    private static final Color COL_TOUR_DEFENSE = new Color(120, 120, 210);
    private static final Color COL_AUTEL_INVOC   = new Color(140,  60, 180);
    private static final Color COL_STATUE_DRAGON = new Color(220, 130,   0); // doré dragon

    private static final Color COL_BG       = new Color( 40,  30,  20);
    private static final Color COL_GRID     = new Color(  0,   0,   0,  40);
    private static final Color COL_SELECTED = new Color(255, 100,  50, 200);

    private static final Font FONT_TITLE     = new Font("Serif",     Font.BOLD,  22);
    private static final Font FONT_RESOURCE  = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_TOOLTIP   = new Font("Serif",     Font.BOLD,  13);
    private static final Font FONT_LEGEND    = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_INV_TITLE = new Font("Serif",     Font.BOLD,  12);
    private static final Font FONT_INV_VAL   = new Font("SansSerif", Font.BOLD,  12);

    // inventaire du joueur
    private int stockBois       = 0;
    private int stockFer        = 0;
    private int stockOr         = 0;
    private int stockNourriture = 0;

    private final Map gameMap;
    private int selectedX = -1;
    private int selectedY = -1;

    // Images des terrains (null = fallback couleur)
    private BufferedImage imgForet;
    private BufferedImage imgFer;
    private BufferedImage imgOr;
    private BufferedImage imgMaison;
    private BufferedImage imgMaisonNonConstruite;
    private BufferedImage imgHotelDeVille;
    private BufferedImage imgHotelDeVilleNiveau5;
    private BufferedImage imgHerbre;
    private BufferedImage imgHerbreNuit;
    private BufferedImage imgNouriture;
    private BufferedImage imgInvocation;
    private BufferedImage imgTowerDef;
    private BufferedImage imgEntrBois;
    private BufferedImage imgEntrFer;
    private BufferedImage imgEntrOr;
    private BufferedImage imgEntrNouriture;
    private BufferedImage imgVoleur;
    private BufferedImage img1Etoile;
    private BufferedImage img2Etoiles;
    private BufferedImage img3Etoiles;
    private BufferedImage img4Etoiles;
    private BufferedImage img5Etoiles;
    private BufferedImage imgStatueDragon;
    private Personnage hoveredPersonnage  = null;
    private Personnage selectedPersonnage = null;

    public void setSelectedPersonnage(Personnage p) { this.selectedPersonnage = p; repaint(); }
    public Personnage getSelectedPersonnage()        { return selectedPersonnage; }

    // setters inventaire
    public void setStockBois(int v)       { this.stockBois       = v; repaint(); }
    public void setStockFer(int v)        { this.stockFer        = v; repaint(); }
    public void setStockOr(int v)         { this.stockOr         = v; repaint(); }
    public void setStockNourriture(int v) { this.stockNourriture = v; repaint(); }

    public MapPanel(Map gameMap) {
        this.gameMap = gameMap;
        int panelW = BORDER_PAD * 2 + gameMap.getWidth()  * TILE_SIZE + 150;
        int panelH = BORDER_PAD * 2 + gameMap.getHeight() * TILE_SIZE + TITLE_H + 30;
        setPreferredSize(new Dimension(panelW, panelH));
        setBackground(COL_BG);

        // Chargement des images (fallback couleur si fichier absent)
        imgForet               = chargerImage("/images/arbre.png");
        imgFer                 = chargerImage("/images/fer.png");
        imgOr                  = chargerImage("/images/or.png");
        imgMaison              = chargerImage("/images/maison.png");
        imgMaisonNonConstruite = chargerImage("/images/maisonnonconstruite.png");
        imgHotelDeVille        = chargerImage("/images/hoteldeville.jpg");
        imgHotelDeVilleNiveau5 = chargerImage("/images/hoteldevilleniveau5.jpg");
        imgHerbre              = chargerImage("/images/herbre.jpg");
        imgHerbreNuit          = chargerImage("/images/herbrenuit.jpg");
        imgNouriture           = chargerImage("/images/nouriture.jpg");
        imgInvocation          = chargerImage("/images/invocation.jpg");
        imgTowerDef            = chargerImage("/images/towerdef.jpg");
        imgEntrBois            = chargerImage("/images/entrbois.jpg");
        imgEntrFer             = chargerImage("/images/entrfer.jpg");
        imgEntrOr              = chargerImage("/images/entror.jpg");
        imgEntrNouriture       = chargerImage("/images/entrnouriture.jpg");
        imgVoleur              = chargerImage("/images/voleur.jpg");
        img1Etoile             = chargerImage("/images/1etoile.png");
        img2Etoiles            = chargerImage("/images/2etoiles.png");
        img3Etoiles            = chargerImage("/images/3etoiles.png");
        img4Etoiles            = chargerImage("/images/4etoiles.png");
        img5Etoiles            = chargerImage("/images/5etoiles.png");
        imgStatueDragon        = chargerImage("/images/statutdudragon.png");
    }

    private BufferedImage chargerImage(String chemin) {
        try {
            var stream = getClass().getResourceAsStream(chemin);
            if (stream == null) return null;
            return ImageIO.read(stream);
        } catch (IOException e) {
            return null;
        }
    }

    public void setSelectedPosition(int x, int y) { this.selectedX = x; this.selectedY = y; repaint(); }
    public void clearSelection()                   { this.selectedX = -1; this.selectedY = -1; repaint(); }
    public void setHoveredPersonnage(Personnage p) { this.hoveredPersonnage = p; repaint(); }
    public int getTileSize()    { return TILE_SIZE;  }
    public int getBorderPad()   { return BORDER_PAD; }
    public int getTitleHeight() { return TITLE_H;    }

    private String getInitiales(String nom) {
        if (nom == null || nom.isEmpty()) return "?";
        if (nom.length() >= 2)
            return String.valueOf(nom.charAt(0)).toUpperCase()
                 + String.valueOf(nom.charAt(1)).toLowerCase();
        return String.valueOf(nom.charAt(0)).toUpperCase();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawTitle(g2);
        drawCycleJourNuit(g2);
        drawTiles(g2);
        drawVoleurPaths(g2);
        drawPersonnages(g2);
        drawVoleurs(g2);
        drawGrid(g2);
        drawSelectedPersonnage(g2);
        drawSelection(g2);
        drawTooltip(g2);
        drawLegend(g2);
        drawNotification(g2);
    }
    private void drawVoleurs(Graphics2D g2) {
        for (model.Voleur v : gameMap.getVoleurs()) {
            if (v == null) continue;

            int px = BORDER_PAD + v.getX() * TILE_SIZE;
            int py = BORDER_PAD + TITLE_H + v.getY() * TILE_SIZE;
            int margin = 4;

            if (imgVoleur != null) {
                g2.drawImage(imgVoleur, px + margin, py + margin, TILE_SIZE - margin * 2, TILE_SIZE - margin * 2, null);
            } else {
                // fallback cercle rouge avec "V"
                int size = TILE_SIZE - margin * 2 - 6;
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillOval(px + margin + 2, py + margin + 3, size, size);
                g2.setColor(new Color(190, 40, 40));
                g2.fillOval(px + margin, py + margin, size, size);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(px + margin, py + margin, size, size);
                g2.setStroke(new BasicStroke(1f));
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String txt = "V";
                int tx = px + (TILE_SIZE - fm.stringWidth(txt)) / 2;
                int ty = py + (TILE_SIZE + fm.getAscent()) / 2 - 4;
                g2.drawString(txt, tx, ty);
            }
        }
    }

    private void drawVoleurPaths(Graphics2D g2) {
        for (model.Voleur v : gameMap.getVoleurs()) {
            if (v == null || v.getCible() == null) continue;

            int x = v.getX();
            int y = v.getY();
            int tx = v.getCible().getX();
            int ty = v.getCible().getY();

            Graphics2D gPath = (Graphics2D) g2.create();
            gPath.setColor(new Color(220, 30, 30, 120));
            gPath.setStroke(new BasicStroke(3f));

            int curX = x;
            int curY = y;

            while (curX != tx || curY != ty) {
                int nextX = curX;
                int nextY = curY;

                if (curX < tx) nextX++;
                else if (curX > tx) nextX--;
                else if (curY < ty) nextY++;
                else if (curY > ty) nextY--;

                int x1 = BORDER_PAD + curX * TILE_SIZE + TILE_SIZE / 2;
                int y1 = BORDER_PAD + TITLE_H + curY * TILE_SIZE + TILE_SIZE / 2;
                int x2 = BORDER_PAD + nextX * TILE_SIZE + TILE_SIZE / 2;
                int y2 = BORDER_PAD + TITLE_H + nextY * TILE_SIZE + TILE_SIZE / 2;

                gPath.drawLine(x1, y1, x2, y2);

                curX = nextX;
                curY = nextY;
            }

            gPath.dispose();
        }
    }

    private void drawPersonnages(Graphics2D g2) {
        for (Personnage p : gameMap.getPersonnages()) {
            if (!p.isDeploye()) continue;
            int px = BORDER_PAD + p.getX() * TILE_SIZE;
            int py = BORDER_PAD + TITLE_H  + p.getY() * TILE_SIZE;
            int margin = 4;

            // Choisir l'image selon les étoiles
            BufferedImage imgPerso = switch (p.getRareteEtoiles()) {
                case 1 -> img1Etoile;
                case 2 -> img2Etoiles;
                case 3 -> img3Etoiles;
                case 4 -> img4Etoiles;
                case 5 -> img5Etoiles;
                default -> null;
            };

            if (imgPerso != null) {
                g2.drawImage(imgPerso, px + margin, py + margin, TILE_SIZE - margin * 2, TILE_SIZE - margin * 2, null);
            } else {
                // fallback cercle coloré
                int size = TILE_SIZE - margin * 2;
                int cx = px + margin, cy = py + margin;
                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillOval(cx + 2, cy + 3, size, size);
                if (p.isPretARecuperer())       g2.setColor(new Color(255, 180,  60));
                else if (p.isEnExecution())     g2.setColor(new Color(220, 100, 100));
                else                            g2.setColor(new Color( 60, 120, 200));
                g2.fillOval(cx, cy, size, size);
                g2.setColor(new Color(255, 255, 255, 180));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(cx, cy, size, size);
                g2.setStroke(new BasicStroke(1f));
                String initiales = getInitiales(p.getNom());
                g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int tx = px + (TILE_SIZE - fm.stringWidth(initiales)) / 2;
                int ty = py + (TILE_SIZE + fm.getAscent()) / 2 - 4;
                g2.drawString(initiales, tx, ty);
            }

            // Indicateur d'état (petit cercle en bas à droite)
            if (p.isPretARecuperer() || p.isEnExecution()) {
                Color couleurEtat = p.isPretARecuperer() ? new Color(255, 180, 60) : new Color(220, 100, 100);
                g2.setColor(couleurEtat);
                g2.fillOval(px + TILE_SIZE - 12, py + TILE_SIZE - 12, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(px + TILE_SIZE - 12, py + TILE_SIZE - 12, 10, 10);
                g2.setStroke(new BasicStroke(1f));
            }
        }
    }

    private void drawSelectedPersonnage(Graphics2D g2) {
        if (selectedPersonnage == null) return;
        int px = BORDER_PAD + selectedPersonnage.getX() * TILE_SIZE;
        int py = BORDER_PAD + TITLE_H + selectedPersonnage.getY() * TILE_SIZE;
        g2.setColor(new Color(255, 215, 0, 180));
        g2.setStroke(new BasicStroke(4f));
        g2.drawOval(px + 4, py + 4, TILE_SIZE - 8, TILE_SIZE - 8);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawCycleJourNuit(Graphics2D g2) {
        boolean isDay = gameMap.getIsDay();
        long restantMs = gameMap.jourNuit.getTempsRestantMs();
        long sec = restantMs / 1000;
        long min = sec / 60;
        long secRestante = sec % 60;

        String icone  = isDay ? "☀" : "🌙";
        String phase  = isDay ? "Jour" : "Nuit";
        String chrono = String.format("%d:%02d", min, secRestante);
        String texte  = icone + " " + phase + " — " + chrono;

        // Couleurs selon phase
        Color couleurFond  = isDay ? new Color(255, 200, 50, 200) : new Color(30, 30, 100, 220);
        Color couleurTexte = isDay ? new Color(80, 50, 0)          : new Color(180, 200, 255);

        // Position : colonne de droite, au-dessus de la légende
        int lx = BORDER_PAD + gameMap.getWidth() * TILE_SIZE + 16;
        int boxW = 122; // même largeur que les blocs légende
        int boxH = 28;
        int by = BORDER_PAD + TITLE_H - boxH - 8; // juste au-dessus de "TERRAINS"

        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();

        // Fond arrondi
        g2.setColor(couleurFond);
        g2.fillRoundRect(lx - 6, by, boxW, boxH, 10, 10);

        // Bordure
        g2.setColor(isDay ? new Color(200, 150, 0) : new Color(80, 80, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(lx - 6, by, boxW, boxH, 10, 10);
        g2.setStroke(new BasicStroke(1f));

        // Texte centré
        int tw = fm.stringWidth(texte);
        int tx = lx - 6 + (boxW - tw) / 2;
        g2.setColor(couleurTexte);
        g2.drawString(texte, tx, by + fm.getAscent() + 5);
    }

    private void drawTitle(Graphics2D g2) {
        String title = "Carte du Royaume";
        g2.setFont(FONT_TITLE);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth() - 170 - fm.stringWidth(title)) / 2;
        g2.setColor(new Color(0, 0, 0, 130));
        g2.drawString(title, tx + 2, 34);
        GradientPaint gold = new GradientPaint(0, 12, new Color(255, 220, 60), 0, 34, new Color(190, 140, 0));
        g2.setPaint(gold);
        g2.drawString(title, tx, 34);
    }

    private void drawTiles(Graphics2D g2) {
        for (int y = 0; y < gameMap.getHeight(); y++)
            for (int x = 0; x < gameMap.getWidth(); x++)
                drawTile(g2, BORDER_PAD + x * TILE_SIZE, BORDER_PAD + TITLE_H + y * TILE_SIZE, gameMap.getTerrainAt(x, y), x, y);
    }

    private void drawTile(Graphics2D g2, int px, int py, int type, int tileX, int tileY) {
        Color color;
        String label = null;
        Color textColor = Color.WHITE;

        model.Batiment batiment = gameMap.getBatimentAt(tileX, tileY);

        switch (type) {
            case 1 -> color = gameMap.getIsDay() ? COL_HERBE_JOUR : COL_HERBE_NUIT;

            case 2 -> {
                String typeBat = gameMap.getTypeBatiment(tileX, tileY);

                if (typeBat != null && gameMap.estConstruit(tileX, tileY)) {
                    switch (typeBat) {
                        case Map.TYPE_HOTEL_VILLE -> {
                            color = COL_HOTEL_VILLE;
                            label = "HOTEL Niv." + gameMap.getHotelDeVille().getNiveau();
                        }
                        case Map.TYPE_MAISON -> {
                            color = COL_MAISON;
                            label = "MAISON";
                            textColor = new Color(60, 40, 20);
                        }
                        case Map.TYPE_TOUR_DEFENSE -> {
                            color = COL_TOUR_DEFENSE;
                            label = "TOUR DEF.";
                            textColor = new Color(60, 40, 20);
                        }
                        case Map.TYPE_ENTREPOT_BOIS -> {
                            color = COL_ENTREPOT;
                            label = "ENT.BOIS";
                        }
                        case Map.TYPE_ENTREPOT_FER -> {
                            color = COL_ENTREPOT;
                            label = "ENT.FER";
                        }
                        case Map.TYPE_ENTREPOT_OR -> {
                            color = COL_ENTREPOT;
                            label = "ENT.OR";
                        }
                        case Map.TYPE_ENTREPOT_NOURR -> {
                            color = COL_ENTREPOT;
                            label = "ENT.NOURR";
                        }
                        case Map.TYPE_AUTEL_INVOC -> {
                            color = COL_AUTEL_INVOC;
                            label = "AUTEL";
                        }
                        case Map.TYPE_STATUE_DRAGON -> {
                            color = COL_STATUE_DRAGON;
                            label = " STATUE";
                        }
                        default -> {
                            color = COL_BATIMENT;
                            label = "BATIMENT";
                        }
                    }
                } else {
                    // bâtiment non construit : version grisée
                    textColor = new Color(190, 190, 190);

                    if (typeBat != null) {
                        switch (typeBat) {
                            case Map.TYPE_HOTEL_VILLE -> {
                                color = new Color(110, 65, 65);
                                label = "HOTEL";
                            }
                            case Map.TYPE_MAISON -> {
                                color = new Color(115, 100, 85);
                                label = "MAISON";
                            }
                            case Map.TYPE_TOUR_DEFENSE -> {
                                color = new Color(90, 90, 150);
                                label = "TOUR DEF.";
                            }
                            case Map.TYPE_ENTREPOT_BOIS -> {
                                color = new Color(55, 85, 65);
                                label = "ENT.BOIS";
                            }
                            case Map.TYPE_ENTREPOT_FER -> {
                                color = new Color(65, 75, 95);
                                label = "ENT.FER";
                            }
                            case Map.TYPE_ENTREPOT_OR -> {
                                color = new Color(105, 95, 45);
                                label = "ENT.OR";
                            }
                            case Map.TYPE_ENTREPOT_NOURR -> {
                                color = new Color(85, 70, 50);
                                label = "ENT.NOURR";
                            }
                            case Map.TYPE_AUTEL_INVOC -> {
                                color = new Color(80, 50, 100);
                                label = "AUTEL";
                            }
                            case Map.TYPE_STATUE_DRAGON -> {
                                color = new Color(90, 55, 0);
                                label = "STATUE";
                            }
                            default -> {
                                color = COL_BATIMENT;
                                label = "CONSTRUIRE";
                            }
                        }
                    } else {
                        color = COL_BATIMENT;
                        label = "CONSTRUIRE";
                    }
                }
            }

            case 3 -> {
                float tauxBois = (float) gameMap.getStockBoisForet() / gameMap.getStockBoisForetMax();
                color = new Color(20, 90 + (int) (120 * tauxBois), 20);
                label = "BOIS " + gameMap.getStockBoisForet();
            }

            case 4 -> {
                color = COL_FER;
                label = "FER";
            }

            case 5 -> {
                color = COL_OR;
                label = "OR";
                textColor = new Color(100, 70, 0);
            }

            case 6 -> {
                color = COL_NOURRITURE;
                label = "NOURR.";
            }

            default -> color = gameMap.getIsDay() ? COL_HERBE_JOUR : COL_HERBE_NUIT;
        }

        // surbrillance de la zone d'action du personnage sélectionné
        ActionType actionAffichee = null;
        if (selectedPersonnage != null && selectedPersonnage.isDeploye()) {
            actionAffichee = selectedPersonnage.getActionCourante();
        } else if (gameMap.getPersonnageMiseEnValeur() != null) {
            actionAffichee = gameMap.getActionMiseEnValeur();
        }

        if (gameMap.caseCorrespondAAction(type, actionAffichee)) {
            if (actionAffichee == ActionType.COUPER_BOIS) {
                color = new Color(40, 170, 40);
            } else if (actionAffichee == ActionType.MINER_FER) {
                color = new Color(120, 120, 210);
            }
        }

        g2.setColor(color);
        g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);

        // Image terrain (par-dessus le fond couleur)
        if (type == 1) {
            BufferedImage imgHerbe = gameMap.getIsDay() ? imgHerbre : imgHerbreNuit;
            if (imgHerbe != null) g2.drawImage(imgHerbe, px, py, TILE_SIZE, TILE_SIZE, null);
        }
        if (type == 3 && imgForet != null) {
            g2.drawImage(imgForet, px, py, TILE_SIZE, TILE_SIZE, null);
        }
        if (type == 4 && imgFer != null) {
            g2.drawImage(imgFer, px, py, TILE_SIZE, TILE_SIZE, null);
        }
        if (type == 5 && imgOr != null) {
            g2.drawImage(imgOr, px, py, TILE_SIZE, TILE_SIZE, null);
        }
        if (type == 6 && imgNouriture != null) {
            g2.drawImage(imgNouriture, px, py, TILE_SIZE, TILE_SIZE, null);
        }

        // Images bâtiments
        if (type == 2) {
            String typeBat = gameMap.getTypeBatiment(tileX, tileY);
            if (typeBat != null) {
                if (gameMap.estConstruit(tileX, tileY)) {
                    switch (typeBat) {
                        case Map.TYPE_MAISON -> { if (imgMaison != null) g2.drawImage(imgMaison, px, py, TILE_SIZE, TILE_SIZE, null); }
                        case Map.TYPE_HOTEL_VILLE -> {
                            boolean niveauMax = gameMap.getHotelDeVille().estAuNiveauMax();
                            BufferedImage imgHdv = niveauMax ? imgHotelDeVilleNiveau5 : imgHotelDeVille;
                            if (imgHdv != null) g2.drawImage(imgHdv, px, py, TILE_SIZE, TILE_SIZE, null);
                        }
                        case Map.TYPE_AUTEL_INVOC  -> { if (imgInvocation != null) g2.drawImage(imgInvocation, px, py, TILE_SIZE, TILE_SIZE, null); }
                        case Map.TYPE_TOUR_DEFENSE -> { if (imgTowerDef   != null) g2.drawImage(imgTowerDef,   px, py, TILE_SIZE, TILE_SIZE, null); }
                        case Map.TYPE_ENTREPOT_BOIS  -> { if (imgEntrBois     != null) g2.drawImage(imgEntrBois,     px, py, TILE_SIZE, TILE_SIZE, null); }
                        case Map.TYPE_ENTREPOT_FER   -> { if (imgEntrFer      != null) g2.drawImage(imgEntrFer,      px, py, TILE_SIZE, TILE_SIZE, null); }
                        case Map.TYPE_ENTREPOT_OR    -> { if (imgEntrOr       != null) g2.drawImage(imgEntrOr,       px, py, TILE_SIZE, TILE_SIZE, null); }
                        case Map.TYPE_ENTREPOT_NOURR -> { if (imgEntrNouriture != null) g2.drawImage(imgEntrNouriture, px, py, TILE_SIZE, TILE_SIZE, null); }
                        case Map.TYPE_STATUE_DRAGON  -> { if (imgStatueDragon  != null) g2.drawImage(imgStatueDragon,  px, py, TILE_SIZE, TILE_SIZE, null); }
                    }
                } else {
                    if (imgMaisonNonConstruite != null) {
                        g2.drawImage(imgMaisonNonConstruite, px, py, TILE_SIZE, TILE_SIZE, null);
                    }
                }
            }
        }

        // bordure selon l'état du bâtiment
        if (type == 2 && gameMap.getTypeBatiment(tileX, tileY) != null) {
            if (gameMap.estConstruit(tileX, tileY)) {
                // bordure normale
                g2.setColor(new Color(0, 0, 0, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);

                // bâtiment attaqué : cadre rouge
                if (batiment != null && batiment.isEnAttaque()) {
                    g2.setColor(new Color(255, 40, 40, 220));
                    g2.setStroke(new BasicStroke(4f));
                    g2.drawRect(px + 2, py + 2, TILE_SIZE - 4, TILE_SIZE - 4);

                    g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2.drawString("!", px + TILE_SIZE - 12, py + 14);
                }

                // bâtiment protégé : cadre vert
                if (batiment != null && batiment.isProtege()) {
                    g2.setColor(new Color(40, 220, 60, 220));
                    g2.setStroke(new BasicStroke(4f));
                    g2.drawRect(px + 4, py + 4, TILE_SIZE - 8, TILE_SIZE - 8);
                }

                g2.setStroke(new BasicStroke(1f));
            } else {
                float[] dash = {4f, 4f};
                g2.setColor(new Color(200, 200, 200, 120));
                g2.setStroke(new BasicStroke(
                        1.5f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10f,
                        dash,
                        0f
                ));
                g2.drawRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);
                g2.setStroke(new BasicStroke(1f));
            }
        } else {
            g2.setColor(new Color(0, 0, 0, 30));
            g2.drawRect(px, py, TILE_SIZE - 1, TILE_SIZE - 1);
        }

        // N'afficher le label texte que si aucune image ne couvre cette tuile
        boolean aUneImage = false;
        if (type == 1 && (gameMap.getIsDay() ? imgHerbre : imgHerbreNuit) != null) aUneImage = true;
        if (type == 3 && imgForet    != null) aUneImage = true;
        if (type == 4 && imgFer      != null) aUneImage = true;
        if (type == 5 && imgOr       != null) aUneImage = true;
        if (type == 6 && imgNouriture != null) aUneImage = true;
        if (type == 2) {
            String typeBat = gameMap.getTypeBatiment(tileX, tileY);
            if (typeBat != null) {
                if (gameMap.estConstruit(tileX, tileY)) {
                    if (Map.TYPE_MAISON.equals(typeBat)         && imgMaison             != null) aUneImage = true;
                    if (Map.TYPE_HOTEL_VILLE.equals(typeBat)    && (imgHotelDeVille != null || imgHotelDeVilleNiveau5 != null)) aUneImage = true;
                    if (Map.TYPE_AUTEL_INVOC.equals(typeBat)    && imgInvocation          != null) aUneImage = true;
                    if (Map.TYPE_TOUR_DEFENSE.equals(typeBat)   && imgTowerDef            != null) aUneImage = true;
                    if (Map.TYPE_ENTREPOT_BOIS.equals(typeBat)  && imgEntrBois            != null) aUneImage = true;
                    if (Map.TYPE_ENTREPOT_FER.equals(typeBat)   && imgEntrFer             != null) aUneImage = true;
                    if (Map.TYPE_ENTREPOT_OR.equals(typeBat)    && imgEntrOr              != null) aUneImage = true;
                    if (Map.TYPE_ENTREPOT_NOURR.equals(typeBat) && imgEntrNouriture != null) aUneImage = true;
                    if (Map.TYPE_STATUE_DRAGON.equals(typeBat)  && imgStatueDragon  != null) aUneImage = true;
                } else {
                    if (imgMaisonNonConstruite != null) aUneImage = true;
                }
            }
        }

        if (label != null && !aUneImage) {
            g2.setFont(FONT_RESOURCE);
            FontMetrics fm = g2.getFontMetrics();
            int textX = px + (TILE_SIZE - fm.stringWidth(label)) / 2;
            int textY = py + (TILE_SIZE + fm.getAscent()) / 2 - 2;

            g2.setColor(new Color(0, 0, 0, 100));
            g2.drawString(label, textX + 1, textY + 1);

            g2.setColor(textColor);
            g2.drawString(label, textX, textY);
        }
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(COL_GRID);
        g2.setStroke(new BasicStroke(0.5f));
        int x0 = BORDER_PAD, y0 = BORDER_PAD + TITLE_H;
        int x1 = x0 + gameMap.getWidth()  * TILE_SIZE;
        int y1 = y0 + gameMap.getHeight() * TILE_SIZE;
        for (int x = 0; x <= gameMap.getWidth();  x++) g2.drawLine(x0 + x * TILE_SIZE, y0, x0 + x * TILE_SIZE, y1);
        for (int y = 0; y <= gameMap.getHeight(); y++) g2.drawLine(x0, y0 + y * TILE_SIZE, x1, y0 + y * TILE_SIZE);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawSelection(Graphics2D g2) {
        if (selectedX < 0) return;
        int px = BORDER_PAD + selectedX * TILE_SIZE;
        int py = BORDER_PAD + TITLE_H   + selectedY * TILE_SIZE;
        g2.setColor(COL_SELECTED);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(px + 1, py + 1, TILE_SIZE - 3, TILE_SIZE - 3);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawTooltip(Graphics2D g2) {
        if (selectedX < 0) return;
        String text;
        model.Voleur hoveredVoleur = gameMap.getVoleurAt(selectedX, selectedY);
        if (hoveredVoleur != null) {
            text = String.format("Voleur | ATK: %d | Vitesse: %d | Cible: (%d,%d)",
                    hoveredVoleur.getAtk(),
                    hoveredVoleur.getVitesse(),
                    hoveredVoleur.getCible() != null ? hoveredVoleur.getCible().getX() : -1,
                    hoveredVoleur.getCible() != null ? hoveredVoleur.getCible().getY() : -1
            );
        }
        else if (hoveredPersonnage != null) {
            String etat;
            if (hoveredPersonnage.isPretARecuperer())  etat = "Mission terminée - clique pour récupérer";
            else if (hoveredPersonnage.isEnExecution()) etat = "En exécution";
            else if (hoveredPersonnage.isDeploye())     etat = "En déplacement";
            else                                        etat = "Réserve";
            text = String.format("%s  |  %d★  |  Action: %s  |  (%d, %d)",
                    hoveredPersonnage.getNom(),
                    hoveredPersonnage.getRareteEtoiles(),
                    etat,
                    hoveredPersonnage.getX(), hoveredPersonnage.getY());
        } else {
            int type = gameMap.getTerrainAt(selectedX, selectedY);
            String typeName = switch (type) {
                case 1 -> "Herbe";          case 2 -> "Batiment";
                case 3 -> "Foret -> Bois";  case 4 -> "Mine -> Fer";
                case 5 -> "Gisement -> Or"; case 6 -> "Cueillette -> Nourriture";
                default -> "Inconnu";
            };
            text = String.format("(%d, %d) - %s", selectedX, selectedY, typeName);
        }
        g2.setFont(FONT_TOOLTIP);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text) + 16, th = fm.getHeight() + 6;
        int bx = BORDER_PAD, by = getHeight() - BORDER_PAD / 2 - th;
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(bx - 4, by - 2, tw, th, 8, 8);
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(text, bx + 4, by + fm.getAscent() - 1);
    }

    private void drawNotification(Graphics2D g2) {
        String message = gameMap.getNotificationMessage();
        if (message == null || message.isBlank()) return;
        int boxW = 300, boxH = 60;
        int bx = BORDER_PAD + gameMap.getWidth() * TILE_SIZE - boxW, by = 8;
        g2.setColor(new Color(20, 20, 20, 210));
        g2.fillRoundRect(bx, by, boxW, boxH, 12, 12);
        g2.setColor(new Color(255, 215, 0));
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("Notification", bx + 12, by + 18);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String ligne1 = message.length() > 42 ? message.substring(0, 42) + "..." : message;
        g2.drawString(ligne1, bx + 12, by + 38);
        g2.drawString("Clique sur le personnage pour recuperer.", bx + 12, by + 53);
    }

    private void drawLegend(Graphics2D g2) {
        int lx = BORDER_PAD + gameMap.getWidth() * TILE_SIZE + 16;
        int ly = BORDER_PAD + TITLE_H;

        String[] terrainLabels = {"Herbe", "Batiment", "Foret / Bois", "Mine / Fer", "Gisement / Or", "Cueillette / Nourr."};
        Color[]  terrainColors = {COL_HERBE_JOUR, COL_BATIMENT, COL_BOIS, COL_FER, COL_OR, COL_NOURRITURE};
        if (!gameMap.getIsDay()) terrainColors[0] = COL_HERBE_NUIT;

        int terrainBlockH = terrainLabels.length * 22 + 30;
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(lx - 6, ly - 6, 135, terrainBlockH, 8, 8);

        g2.setFont(FONT_INV_TITLE);
        g2.setColor(new Color(255, 215, 60));
        g2.drawString("TERRAINS", lx, ly + 13);

        g2.setFont(FONT_LEGEND);
        for (int i = 0; i < terrainLabels.length; i++) {
            int iy = ly + 28 + i * 22;
            g2.setColor(terrainColors[i]);
            g2.fillRect(lx, iy, 14, 14);
            g2.setColor(new Color(0, 0, 0, 100));
            g2.drawRect(lx, iy, 14, 14);
            g2.setColor(new Color(220, 205, 170));
            g2.drawString(terrainLabels[i], lx + 18, iy + 11);
        }

        // bloc inventaire
        int invY = ly + terrainBlockH + 14;
        String[] resLabels = {"Bois", "Fer", "Or", "Nourriture"};
        Color[]  resColors = {COL_BOIS, COL_FER, COL_OR, COL_NOURRITURE};
        int[]    resValues = {stockBois, stockFer, stockOr, stockNourriture};

        int invBlockH = resLabels.length * 26 + 34;
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRoundRect(lx - 6, invY - 6, 135, invBlockH, 8, 8);

        g2.setFont(FONT_INV_TITLE);
        g2.setColor(new Color(255, 215, 60));
        g2.drawString("RESSOURCES", lx, invY + 13);
        g2.setColor(new Color(255, 215, 60, 70));
        g2.drawLine(lx, invY + 18, lx + 122, invY + 18);

        // max par ressource depuis l'inventaire (via gameMap)
        int[] resMax = {
            gameMap.getInventaire().getMaxBois(),
            gameMap.getInventaire().getMaxFer(),
            gameMap.getInventaire().getMaxOr(),
            gameMap.getInventaire().getMaxNourriture()
        };

        for (int i = 0; i < resLabels.length; i++) {
            int iy = invY + 30 + i * 26;
            g2.setColor(resColors[i]);
            g2.fillRect(lx, iy, 14, 14);
            g2.setColor(new Color(0, 0, 0, 100));
            g2.drawRect(lx, iy, 14, 14);

            g2.setFont(FONT_LEGEND);
            g2.setColor(new Color(220, 205, 170));
            g2.drawString(resLabels[i], lx + 18, iy + 8);

            // afficher val / max sous le nom
            String maxStr = (resMax[i] < 0) ? "∞" : String.valueOf(resMax[i]);
            String valStr = resValues[i] + "/" + maxStr;
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            // barre de remplissage
            int barW = 110;
            float ratio = (resMax[i] <= 0) ? 0f : Math.min(1f, (float) resValues[i] / resMax[i]);
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRect(lx + 18, iy + 13, barW, 6);
            g2.setColor(resColors[i].darker());
            g2.fillRect(lx + 18, iy + 13, (int)(barW * ratio), 6);
            // texte valeur
            g2.setColor(resValues[i] > 0 ? new Color(255, 240, 160) : new Color(130, 120, 100));
            g2.drawString(valStr, lx + 18 + barW - fm.stringWidth(valStr), iy + 11);
        }
    }
}