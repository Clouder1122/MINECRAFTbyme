package world;

import render.Camera;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChunkManager {
    private World world;
    private int renderDistance = 8;
    private ExecutorService chunkLoaderPool = Executors.newFixedThreadPool(2);

    public ChunkManager(World world) {
        this.world = world;
    }

    public void update(Camera camera) {
        int playerCx = (int)Math.floor(camera.getPosition().x / Chunk.SIZE);
        int playerCz = (int)Math.floor(camera.getPosition().z / Chunk.SIZE);

        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int z = -renderDistance; z <= renderDistance; z++) {
                int cx = playerCx + x;
                int cz = playerCz + z;
                
                if (Math.abs(x) + Math.abs(z) > renderDistance * 1.5) continue;

                if (world.getChunk(cx, cz) == null) {
                    Chunk chunk = new Chunk(cx, cz);
                    world.getChunks().put((((long)cx) << 32) | (cz & 0xffffffffL), chunk);
                    chunkLoaderPool.submit(() -> {
                        world.addChunk(chunk); // Overwrites placeholder with generated chunk safely
                    });
                }
            }
        }

        Iterator<Map.Entry<Long, Chunk>> it = world.getChunks().entrySet().iterator();
        while (it.hasNext()) {
            Chunk chunk = it.next().getValue();
            int dx = chunk.getChunkX() - playerCx;
            int dz = chunk.getChunkZ() - playerCz;
            if (Math.abs(dx) > renderDistance + 2 || Math.abs(dz) > renderDistance + 2) {
                chunk.cleanup();
                it.remove();
            } else {
                if (chunk.isDirty()) {
                    chunk.setDirty(false);
                    if (chunk.getMesh() != null) chunk.getMesh().cleanup();
                    ChunkMesh mesh = new ChunkMesh();
                    mesh.generate(chunk, world);
                    chunk.setMesh(mesh);
                    chunk.setMeshReady(true);
                }
            }
        }
    }
    
    public void cleanup() {
        chunkLoaderPool.shutdown();
        for (Chunk chunk : world.getChunks().values()) chunk.cleanup();
        world.getChunks().clear();
    }
}
