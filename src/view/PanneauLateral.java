package view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.*;
import model.Map;
import model.Personnage;

/**
 * Panneau latéral droit : colonne gauche = population + ressources, colonne droite = personnages.
 */
public class PanneauLateral extends JPanel {

    private static final Color BG          = new Color(25, 18, 8);
    private static final Color BG_BLOC     = new Color(0, 0, 0, 130);
    private static final Color OR          = new Color(255, 215, 60);
    private static final Color OR_DIM      = new Color(255, 215, 60, 70);
    private static final Color TEXTE       = new Color(220, 205, 170);
    private static final Color COL_BOIS    = new Color(34,  139, 34);
    private static final Color COL_FER     = new Color(105, 105, 105);
    private static final Color COL_OR_R    = new Color(255, 215, 0);
    private static final Color COL_NOURR   = new Color(160, 120, 60);

    private final Map gameMap;
    private final MapPanel mapPanel;

    private int stockBois, stockFer, stockOr, stockNourriture;
    private BufferedImage[] imgEtoiles = new BufferedImage[5];

    public PanneauLateral(Map gameMap, MapPanel mapPanel) {
        this.gameMap  = gameMap;
        this.mapPanel = mapPanel;
        setBackground(BG);
        setPreferredSize(new Dimension(440, 600));

        for (int i = 0; i < 5; i++) {
            try {
                String nom = "/images/" + (i+1) + "etoile" + (i == 0 ? "" : "s") + ".png";
                var stream = getClass().getResourceAsStream(nom);
                if (stream != null) imgEtoiles[i] = javax.imageio.ImageIO.read(stream);
            } catch (Exception ignored) {}
        }
    }

    public void setStockBois(int v)       { stockBois       = v; repaint(); }
    public void setStockFer(int v)        { stockFer        = v; repaint(); }
    public void setStockOr(int v)         { stockOr         = v; repaint(); }
    public void setStockNourriture(int v) { stockNourriture = v; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int halfW    = getWidth() / 2;
        int ligneH   = 52;
        int headerH  = 36; // titre + séparateur
        int panelH   = getHeight();

        int yDebut = 10;

        // Colonne gauche : population + ressources
        int y = yDebut;
        y = dessinerBlocPopulation(g2, 0, halfW, y);
        int yApresRessources = y + 10;
        dessinerBlocRessources(g2, 0, halfW, yApresRessources);

        // Calcul hauteur bloc ressources pour savoir où commence la zone libre à gauche
        int nbLignesRess = 4;
        int ligneHRess   = 28;
        int blocHRess    = nbLignesRess * ligneHRess + 34;
        int yLibreGauche = yApresRessources + blocHRess + 10;

        // Capacité colonne droite (depuis yDebut)
        int maxDansColonneDroite = Math.max(0, (panelH - yDebut - headerH) / ligneH);
        // Capacité zone sous ressources (colonne gauche basse)
        int maxSousRessources    = Math.max(0, (panelH - yLibreGauche - headerH) / ligneH);

        List<Personnage> persos = gameMap.getPersonnages();
        int total = persos.size();

        // Répartition : droite d'abord, puis sous ressources (gauche bas)
        int nbDroite       = Math.min(total, maxDansColonneDroite);
        int nbSousRess     = Math.min(total - nbDroite, maxSousRessources);

        List<Personnage> persosDroite   = persos.subList(0, nbDroite);
        List<Personnage> persosSousRess = persos.subList(nbDroite, nbDroite + nbSousRess);

        // Colonne droite
        dessinerBlocPersonnages(g2, halfW, getWidth(), yDebut, persosDroite, total > 0 && nbDroite == 0);

        // Sous ressources (colonne gauche) si overflow
        if (!persosSousRess.isEmpty()) {
            dessinerBlocPersonnages(g2, 0, halfW, yLibreGauche, persosSousRess, false);
        }
    }

    // ── Bloc timer ────────────────────────────────────────────────────────────

    private void dessinerTimer(Graphics2D g2, int xStart, int xEnd, int y) {
        int w     = xEnd - xStart;
        int blocH = 62;

        long ms      = gameMap.getTempsRestantMs();
        long total   = gameMap.getDureeTotaleMs();
        long heures  = ms / 3600000;
        long minutes = (ms % 3600000) / 60000;
        long secondes= (ms % 60000) / 1000;

        float ratio = total <= 0 ? 0f : Math.min(1f, (float) ms / total);

        // Couleur selon urgence
        Color couleurTimer;
        if (ratio > 0.5f)       couleurTimer = new Color(100, 220, 100);
        else if (ratio > 0.25f) couleurTimer = new Color(220, 180, 0);
        else                    couleurTimer = new Color(220, 60, 60);

        // Fond
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(xStart + 6, y, w - 12, blocH, 10, 10);

        // Titre
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(OR);
        g2.drawString("TEMPS RESTANT", xStart + 14, y + 16);
        g2.setColor(OR_DIM);
        g2.drawLine(xStart + 14, y + 20, xEnd - 14, y + 20);

        // Chrono
        String chrono = String.format("%02d:%02d:%02d", heures, minutes, secondes);
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.setColor(couleurTimer);
        FontMetrics fm = g2.getFontMetrics();
        int tx = xStart + (w - fm.stringWidth(chrono)) / 2;
        g2.drawString(chrono, tx, y + 46);

        // Barre de progression
        int barX = xStart + 14;
        int barW = w - 28;
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRect(barX, y + 50, barW, 6);
        g2.setColor(couleurTimer);
        g2.fillRect(barX, y + 50, (int)(barW * ratio), 6);
    }

    // ── Bloc personnages ──────────────────────────────────────────────────────

    private void dessinerBlocPersonnages(Graphics2D g2, int xStart, int xEnd, int y,
                                         List<Personnage> persos, boolean forceEmpty) {
        int w      = xEnd - xStart;
        int ligneH = 52;
        int blocH  = (persos.isEmpty() && forceEmpty) ? 50
                   : persos.isEmpty() ? 50
                   : persos.size() * ligneH + 36;

        g2.setColor(BG_BLOC);
        g2.fillRoundRect(xStart + 6, y, w - 12, blocH, 10, 10);

        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(OR);
        g2.drawString("PERSONNAGES", xStart + 14, y + 16);
        g2.setColor(OR_DIM);
        g2.drawLine(xStart + 14, y + 20, xEnd - 14, y + 20);

        if (persos.isEmpty()) {
            if (forceEmpty) {
                g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
                g2.setColor(new Color(150, 130, 90));
                g2.drawString("Aucun personnage invoqué", xStart + 14, y + 40);
            }
            // sinon c'est un bloc overflow vide, on ne l'affiche pas
        } else {
            for (int i = 0; i < persos.size(); i++) {
                dessinerLignePersonnage(g2, persos.get(i), xStart + 10, y + 28 + i * ligneH, w - 20);
            }
        }
    }

    private void dessinerLignePersonnage(Graphics2D g2, Personnage p, int x, int y, int w) {
        int etoiles = p.getRareteEtoiles();
        BufferedImage img = (etoiles >= 1 && etoiles <= 5) ? imgEtoiles[etoiles - 1] : null;

        Color bgLigne;
        if (p.isPretARecuperer())   bgLigne = new Color(255, 180, 60, 60);
        else if (p.isEnExecution()) bgLigne = new Color(220, 100, 100, 40);
        else if (p.isDeploye())     bgLigne = new Color(60, 120, 200, 40);
        else                        bgLigne = new Color(255, 255, 255, 10);

        g2.setColor(bgLigne);
        g2.fillRoundRect(x, y, w, 46, 8, 8);

        if (img != null) {
            g2.drawImage(img, x + 4, y + 4, 38, 38, null);
        } else {
            g2.setColor(new Color(80, 60, 30));
            g2.fillOval(x + 4, y + 6, 34, 34);
            g2.setColor(OR);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(etoiles + "★", x + 10, y + 28);
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(TEXTE);
        g2.drawString(p.getNom(), x + 48, y + 14);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(OR);
        g2.drawString("★".repeat(etoiles), x + 48, y + 26);

        String statut;
        Color couleurStatut;
        if (p.isPretARecuperer())        { statut = "✔ Terminée";         couleurStatut = new Color(255, 180, 60); }
        else if (p.isEnSoin())           { statut = "♥ En soin";          couleurStatut = new Color(200, 100, 150); }
        else if (p.isEnExecution())      { statut = "⚙ En cours";         couleurStatut = new Color(220, 100, 100); }
        else if (p.isDeploye())          { statut = "→ En déplacement";   couleurStatut = new Color(100, 160, 220); }
        else                             { statut = "◦ Disponible";       couleurStatut = new Color(100, 200, 100); }

        g2.setFont(new Font("SansSerif", Font.ITALIC, 10));
        g2.setColor(couleurStatut);
        g2.drawString(statut, x + 48, y + 38);

        int barX = x + w - 38;
        float ratio = p.getHpMax() > 0 ? (float) p.getHpActuel() / p.getHpMax() : 0f;
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRect(barX, y + 16, 32, 5);
        g2.setColor(ratio > 0.5f ? new Color(60, 180, 60) : ratio > 0.25f ? new Color(220, 180, 0) : new Color(200, 60, 60));
        g2.fillRect(barX, y + 16, (int)(32 * ratio), 5);
    }

    // ── Bloc population (colonne gauche) ──────────────────────────────────────

    private int dessinerBlocPopulation(Graphics2D g2, int xStart, int xEnd, int y) {
        int w     = xEnd - xStart;
        int blocH = 56;

        g2.setColor(BG_BLOC);
        g2.fillRoundRect(xStart + 6, y, w - 12, blocH, 10, 10);

        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(OR);
        g2.drawString("POPULATION", xStart + 14, y + 16);
        g2.setColor(OR_DIM);
        g2.drawLine(xStart + 14, y + 20, xEnd - 14, y + 20);

        int nb  = gameMap.getNombrePersonnages();
        int cap = gameMap.getCapacitePersonnages();
        float ratio = cap <= 0 ? 0f : Math.min(1f, (float) nb / cap);
        int barW = w - 40;

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(TEXTE);
        g2.drawString("Personnages", xStart + 18, y + 35);

        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        String txt = nb + "/" + cap;
        g2.setColor(new Color(255, 240, 160));
        g2.drawString(txt, xStart + 18 + barW - fm.stringWidth(txt), y + 35);

        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRect(xStart + 18, y + 39, barW, 7);
        g2.setColor(nb >= cap ? new Color(220, 60, 60) : new Color(100, 180, 100));
        g2.fillRect(xStart + 18, y + 39, (int)(barW * ratio), 7);

        return y + blocH;
    }

    // ── Bloc ressources (colonne gauche) ──────────────────────────────────────

    private void dessinerBlocRessources(Graphics2D g2, int xStart, int xEnd, int y) {
        int w      = xEnd - xStart;
        String[] labels = {"Bois", "Fer", "Or", "Nourriture"};
        Color[]  colors = {COL_BOIS, COL_FER, COL_OR_R, COL_NOURR};
        int[]    vals   = {stockBois, stockFer, stockOr, stockNourriture};
        int[]    maxs   = {
            gameMap.getMaxBoisInventaire(), gameMap.getMaxFerInventaire(),
            gameMap.getMaxOrInventaire(),   gameMap.getMaxNourritureInventaire()
        };

        int ligneH = 28;
        int blocH  = labels.length * ligneH + 34;
        int barW   = w - 40;

        g2.setColor(BG_BLOC);
        g2.fillRoundRect(xStart + 6, y, w - 12, blocH, 10, 10);

        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(OR);
        g2.drawString("RESSOURCES", xStart + 14, y + 16);
        g2.setColor(OR_DIM);
        g2.drawLine(xStart + 14, y + 20, xEnd - 14, y + 20);

        for (int i = 0; i < labels.length; i++) {
            int iy = y + 28 + i * ligneH;

            g2.setColor(colors[i]);
            g2.fillRect(xStart + 14, iy, 10, 10);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(TEXTE);
            g2.drawString(labels[i], xStart + 30, iy + 9);

            String maxStr = maxs[i] < 0 ? "∞" : String.valueOf(maxs[i]);
            String valStr = vals[i] + "/" + maxStr;
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            float ratio = maxs[i] <= 0 ? 0f : Math.min(1f, (float) vals[i] / maxs[i]);
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRect(xStart + 18, iy + 13, barW, 5);
            g2.setColor(colors[i].darker());
            g2.fillRect(xStart + 18, iy + 13, (int)(barW * ratio), 5);
            g2.setColor(vals[i] > 0 ? new Color(255, 240, 160) : new Color(130, 120, 100));
            g2.drawString(valStr, xStart + 18 + barW - fm.stringWidth(valStr), iy + 11);
        }
    }

    public Personnage trouverPersonnageAuClic(int mouseX, int mouseY) {
        List<Personnage> persos = gameMap.getPersonnages();
        int halfW    = getWidth() / 2;
        int ligneH   = 52;
        int headerH  = 36;
        int panelH   = getHeight();

        // Mêmes seuils que paintComponent
        int yDebut           = 10;
        int yApresPopBloc    = yDebut + 66;
        int yApresRessources = yApresPopBloc + 10;
        int blocHRess        = 4 * 28 + 34;
        int yLibreGauche     = yApresRessources + blocHRess + 10;

        int maxDroite  = Math.max(0, (panelH - 10 - headerH) / ligneH);
        int maxSousRess= Math.max(0, (panelH - yLibreGauche - headerH) / ligneH);

        int nbDroite   = Math.min(persos.size(), maxDroite);
        int nbSousRess = Math.min(persos.size() - nbDroite, maxSousRess);

        // Clic colonne droite
        if (mouseX >= halfW) {
            int debutY = yDebut + 28;
            for (int i = 0; i < nbDroite; i++) {
                int yHaut = debutY + i * ligneH;
                if (mouseY >= yHaut && mouseY <= yHaut + 46) return persos.get(i);
            }
        }

        // Clic sous ressources (colonne gauche basse)
        if (mouseX < halfW) {
            int debutY = yLibreGauche + 28;
            for (int i = 0; i < nbSousRess; i++) {
                int yHaut = debutY + i * ligneH;
                if (mouseY >= yHaut && mouseY <= yHaut + 46) return persos.get(nbDroite + i);
            }
        }

        return null;
    }
}