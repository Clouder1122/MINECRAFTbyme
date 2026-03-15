package engine;

public class Game {
    public static void main(String[] args) {
        System.out.println("Starting Voxel Engine...");
        GameLoop gameLoop = new GameLoop();
        gameLoop.start();
    }
}
