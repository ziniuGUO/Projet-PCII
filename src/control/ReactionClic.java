package control;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import model.ActionType;
import model.Batiment;
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
    private final Runnable onVictoire;

    public ReactionClic(Map gameMap, MapPanel mapPanel, int tileSize, int borderPad, int titleHeight, Runnable onVictoire) {
        this.gameMap     = gameMap;
        this.mapPanel    = mapPanel;
        this.tileSize    = tileSize;
        this.borderPad   = borderPad;
        this.titleHeight = titleHeight;
        this.onVictoire  = onVictoire;
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

        if (gameMap.estHotelDeVille(tileX, tileY) && SwingUtilities.isLeftMouseButton(e)) {
            ouvrirDialogueHotelDeVille();
            return;
        }

        if (gameMap.getTerrainAt(tileX, tileY) == Map.BATIMENT
                && !gameMap.estConstruit(tileX, tileY)
                && SwingUtilities.isLeftMouseButton(e)) {
            ouvrirDialogueConstruction(tileX, tileY);
            return;
        }

        Personnage clicked = gameMap.getPersonnageAt(tileX, tileY);
        if (clicked != null) {
            mapPanel.setSelectedPersonnage(clicked);

            if (clicked.isChoixApresVolRequis()) {
                ouvrirDialogueRetourOuContinuer(clicked);
                mapPanel.repaint();
                return;
            }

            if (clicked.isPretARecuperer()) {
                ActionType ancienneAction = clicked.getActionCourante();
                int gain = gameMap.recupererRecompenseEtRappeler(clicked);

                String msg;
                if (ancienneAction == ActionType.DEFENDRE || gain == 0) {
                    msg = "Mission terminée. Le personnage est retourné.";
                } else {
                    msg = "Récompense récupérée : +" + gain;
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
        }
    }

    private void ouvrirDialogueConstruction(int tileX, int tileY) {
        String type = gameMap.getTypeBatiment(tileX, tileY);
        if (type == null) return;

        // Vérifications spéciales pour la statue
        if (model.Map.TYPE_STATUE_DRAGON.equals(type)) {
            if (!gameMap.tousBatimentsConstructs()) {
                JOptionPane.showMessageDialog(mapPanel,
                    "Tous les bâtiments doivent être construits avant d'ériger la Grande Statue du Dragon !",
                    "Conditions non remplies", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (gameMap.getHotelDeVille().getNiveau() < model.HotelDeVille.NIVEAU_MAX) {
                JOptionPane.showMessageDialog(mapPanel,
                    "L'Hôtel de Ville doit être au niveau " + model.HotelDeVille.NIVEAU_MAX
                    + " (actuel : " + gameMap.getHotelDeVille().getNiveau() + ") !",
                    "Conditions non remplies", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

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
            case model.Map.TYPE_STATUE_DRAGON  -> "Grande Statue du Dragon";
            default -> type;
        };

        // Vérifier si le joueur a assez
        boolean assezBois = inv == null || inv.getBois() >= cout[0];
        boolean assezFer  = inv == null || inv.getFer()  >= cout[1];
        boolean assezOr   = inv == null || inv.getOr()   >= cout[2];
        boolean peutConstruire = assezBois && assezFer && assezOr;

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("<html><b>Construire : " + nom + "</b></html>"));
        if (cout[0] > 0) panel.add(new JLabel("Bois : " + cout[0]));
        if (cout[1] > 0) panel.add(new JLabel("Fer : " + cout[1]));
        if (cout[2] > 0) panel.add(new JLabel("Or : " + cout[2]));

        if (!peutConstruire) panel.add(new JLabel("Ressources insuffisantes."));

        int choix = JOptionPane.showConfirmDialog(
                mapPanel,
                panel,
                "Construction",
                peutConstruire ? JOptionPane.YES_NO_OPTION : JOptionPane.DEFAULT_OPTION
        );

        if (peutConstruire && choix == JOptionPane.YES_OPTION) {
            String erreur = gameMap.tenterConstruction(tileX, tileY);
            if (erreur != null) JOptionPane.showMessageDialog(mapPanel, erreur);
            else {
                JOptionPane.showMessageDialog(mapPanel, nom + " construit !");
                if (model.Map.TYPE_STATUE_DRAGON.equals(type)) {
                    afficherEcranVictoire();
                }
            }
            mapPanel.repaint();
        }
    }

    private void afficherEcranVictoire() {
        JDialog dialog = new JDialog(
                (JFrame) SwingUtilities.getWindowAncestor(mapPanel),
                "Victoire !", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(new Color(15, 10, 5));
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 130, 0), 4));

        // Titre
        JLabel titre = new JLabel("🐉  VOUS AVEZ GAGNÉ  🐉", SwingConstants.CENTER);
        titre.setFont(new Font("Serif", Font.BOLD, 36));
        titre.setForeground(new Color(255, 200, 30));
        titre.setBorder(BorderFactory.createEmptyBorder(30, 30, 0, 30));

        // Sous-titre
        JLabel sousTitre = new JLabel(
            "<html><center>La Grande Statue du Dragon s'élève sur votre cité.<br>Votre règne est légendaire !</center></html>",
            SwingConstants.CENTER);
        sousTitre.setFont(new Font("Serif", Font.ITALIC, 16));
        sousTitre.setForeground(new Color(210, 180, 100));
        sousTitre.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        // Bouton rejouer
        JButton btnRejouer = new JButton("⚔  Rejouer");
        btnRejouer.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnRejouer.setBackground(new Color(180, 60, 0));
        btnRejouer.setForeground(Color.WHITE);
        btnRejouer.setFocusPainted(false);
        btnRejouer.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        btnRejouer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRejouer.addActionListener(e -> {
            dialog.dispose();
            if (onVictoire != null) onVictoire.run();
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(15, 10, 5));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        btnPanel.add(btnRejouer);

        panel.add(titre,     BorderLayout.NORTH);
        panel.add(sousTitre, BorderLayout.CENTER);
        panel.add(btnPanel,  BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(mapPanel);
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
        String actionActuelle = (p.getActionCourante() == null) ? "Aucune" : p.getActionCourante().getLabel();

        Object[] options = {
                ActionType.COUPER_BOIS.getLabel(),
                ActionType.MINER_FER.getLabel(),
                ActionType.DEFENDRE.getLabel(),
                ActionType.CHERCHER_NOURRITURE.getLabel(),
                ActionType.CHERCHER_OR.getLabel(),
                "Rappeler"
        };

        int choix = JOptionPane.showOptionDialog(
                mapPanel,
                "Choisis une action :\nAction actuelle : " + actionActuelle,
                "Actions - " + p.getNom() + " (" + p.getRareteEtoiles() + "★)",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choix == 5) {
            gameMap.rappelerPersonnage(p);
            mapPanel.setSelectedPersonnage(null);
            mapPanel.repaint();
            return;
        }

        ActionType nouvelleAction = null;
        if (choix == 0) nouvelleAction = ActionType.COUPER_BOIS;
        else if (choix == 1) nouvelleAction = ActionType.MINER_FER;
        else if (choix == 2) nouvelleAction = ActionType.DEFENDRE;
        else if (choix == 3) nouvelleAction = ActionType.CHERCHER_NOURRITURE;
        else if (choix == 4) nouvelleAction = ActionType.CHERCHER_OR;
        if (nouvelleAction == null) return;

        if (p.estOccupe() && !p.isChoixApresVolRequis()) {
            JOptionPane.showMessageDialog(mapPanel,
                    p.getNom() + " est occupé et ne peut pas recevoir une autre mission pour l'instant.");
            return;
        }

        if (nouvelleAction == ActionType.DEFENDRE) {
            ouvrirDialogueDefense(p);
            return;
        }

        gameMap.deployerPersonnage(p, nouvelleAction);
        mapPanel.repaint();
    }
    private void ouvrirDialogueDefense(Personnage p) {
        List<Batiment> entrepots = gameMap.getBatimentsDefenseDisponibles();
        if (entrepots.isEmpty()) {
            JOptionPane.showMessageDialog(mapPanel, "Aucun entrepôt construit.");
            return;
        }

        DefaultListModel<Batiment> model = new DefaultListModel<>();
        for (Batiment b : entrepots) model.addElement(b);

        JList<Batiment> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(l, value, index, isSelected, cellHasFocus);
                Batiment b = (Batiment) value;
                String txt = b.getNom() + " (" + b.getX() + "," + b.getY() + ")";
                if (b.isEnAttaque()) txt += "  [ATTAQUE]";
                if (b.isProtege()) txt += "  [PROTEGE]";
                label.setText(txt);

                if (b.isEnAttaque() && !isSelected) {
                    label.setForeground(Color.RED);
                } else if (b.isProtege() && !isSelected) {
                    label.setForeground(new Color(0, 128, 0));
                }
                return label;
            }
        });

        int result = JOptionPane.showConfirmDialog(
                mapPanel,
                new JScrollPane(list),
                "Choisir un entrepôt à défendre",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            Batiment cible = list.getSelectedValue();
            if (cible != null) {
                gameMap.deployerPersonnageDefense(p, cible);
                mapPanel.repaint();
            }
        }
    }
    private void ouvrirDialogueRetourOuContinuer(Personnage p) {
        Object[] options = {"Retour à l'hôtel", "Continuer défendre"};
        int choix = JOptionPane.showOptionDialog(
                mapPanel,
                "Le voleur est arrivé avant " + p.getNom() + ".\nQue faire ?",
                "Défense en retard",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choix == 0) {
            gameMap.choisirRetourApresVol(p, false);
        } else if (choix == 1) {
            gameMap.choisirRetourApresVol(p, true);
        }

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