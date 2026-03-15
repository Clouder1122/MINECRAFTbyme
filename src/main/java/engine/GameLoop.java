package engine;

import render.Camera;
import render.Renderer;
import world.World;
import world.ChunkManager;
import player.PlayerController;
import blocks.BlockRegistry;

public class GameLoop {
    private Window window;
    private Camera camera;
    private World world;
    private ChunkManager chunkManager;
    private Renderer renderer;
    private PlayerController playerController;
    private boolean running;

    public void start() {
        window = new Window("Voxel Engine", 1280, 720);
        window.init();
        Time.init();
        BlockRegistry.init();

        camera = new Camera();
        world = new World(123456L); // Stage 11: World seed
        chunkManager = new ChunkManager(world);
        playerController = new PlayerController(camera, world);
        
        try {
            renderer = new Renderer();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        running = true;
        loop();
        
        renderer.cleanup();
        chunkManager.cleanup();
        window.cleanup();
    }

    private void loop() {
        float timer = 0.0f;
        int frames = 0;

        while (running && !window.windowShouldClose()) {
            float dt = Time.getDeltaTime();
            
            update(dt);
            render();
            
            window.update();

            frames++;
            timer += dt;
            if (timer >= 1.0f) {
                System.out.printf("FPS: %d | Chunks: %d\n", frames, world.getChunks().size());
                frames = 0;
                timer = 0;
            }
        }
    }

    private void update(float dt) {
        playerController.update(dt);
        chunkManager.update(camera);
    }

    private void render() {
        renderer.render(window, camera, world);
    }
}
