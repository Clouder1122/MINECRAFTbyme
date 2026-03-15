package world;

import java.util.concurrent.atomic.AtomicBoolean;

public class Chunk {
    public static final int SIZE = 16;
    public static final int HEIGHT = 256;

    private final int chunkX, chunkZ;
    private final byte[] blocks;
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private boolean meshReady = false;
    private ChunkMesh mesh;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new byte[SIZE * HEIGHT * SIZE];
    }

    public void setBlock(int x, int y, int z, byte block) {
        if (x < 0 || x >= SIZE || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE) return;
        blocks[x + (y * SIZE) + (z * SIZE * HEIGHT)] = block;
        dirty.set(true);
    }

    public byte getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE) return 0;
        return blocks[x + (y * SIZE) + (z * SIZE * HEIGHT)];
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public boolean isDirty() { return dirty.get(); }
    public void setDirty(boolean d) { dirty.set(d); }
    public boolean isMeshReady() { return meshReady; }
    public void setMeshReady(boolean ready) { this.meshReady = ready; }
    public ChunkMesh getMesh() { return mesh; }
    public void setMesh(ChunkMesh mesh) { this.mesh = mesh; }

    public void cleanup() {
        if (mesh != null) mesh.cleanup();
    }
}
