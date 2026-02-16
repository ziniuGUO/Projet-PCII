package main;
import javax.swing.JFrame;
//creer une feunetre jfram vide 
public class main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Jeu Medieval");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}   