package main;

import javax.swing.JFrame;
import model.Map;

public class Main {
    public static void main(String[] args) {
        // Créer une map 20x15
        Map gameMap = new Map(20, 15);
        
        // Afficher la map dans la console
        gameMap.displayMap();
        
        // Fenêtre JFrame (ton code existant)
        JFrame frame = new JFrame("Jeu Médiéval");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}