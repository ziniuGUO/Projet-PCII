package model;

public class Map {
    private int width;
    private int height;
    private int[][] terrain; 
    
    /**
     * Constructeur : crée une map vide avec dimensions données
     */
    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.terrain = new int[height][width];
        initializeEmptyMap();
    }
    
    /**
     * Initialise la map avec du terrain par défaut (herbe partout)
     */
    private void initializeEmptyMap() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                terrain[y][x] = 1; // 1 = herbe par défaut
            }
        }
    }
    
    /**
     * Obtenir le type de terrain à une position donnée
     */
    public int getTerrainAt(int x, int y) {
        if (isValidPosition(x, y)) {
            return terrain[y][x];
        }
        return -1; // Position invalide
    }
    
    /**
     * Modifier le terrain à une position donnée
     */
    public void setTerrainAt(int x, int y, int terrainType) {
        if (isValidPosition(x, y)) {
            terrain[y][x] = terrainType;
        }
    }
    
    /**
     * Vérifie si une position est valide sur la map
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    /**
     * Affiche la map dans la console (pour debug)
     */
    public void displayMap() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(terrain[y][x] + " ");
            }
            System.out.println();
        }
    }
    
    // Getters
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}