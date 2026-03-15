package world;

import java.util.concurrent.ConcurrentHashMap;

public class World {
    private final ConcurrentHashMap<Long, Chunk> chunks = new ConcurrentHashMap<>();
    private final TerrainGenerator terrainGenerator;
    public long seed;

    public World(long seed) {
        this.seed = seed;
        this.terrainGenerator = new TerrainGenerator(seed);
    }

    public void setBlock(int x, int y, int z, byte block) {
        Chunk chunk = getChunkAt(x, z);
        if (chunk != null) {
            chunk.setBlock(x & 15, y, z & 15, block);
            if ((x & 15) == 0) { Chunk n = getChunk(chunk.getChunkX()-1, chunk.getChunkZ()); if (n != null) n.setDirty(true); }
            if ((x & 15) == 15) { Chunk n = getChunk(chunk.getChunkX()+1, chunk.getChunkZ()); if (n != null) n.setDirty(true); }
            if ((z & 15) == 0) { Chunk n = getChunk(chunk.getChunkX(), chunk.getChunkZ()-1); if (n != null) n.setDirty(true); }
            if ((z & 15) == 15) { Chunk n = getChunk(chunk.getChunkX(), chunk.getChunkZ()+1); if (n != null) n.setDirty(true); }
        }
    }

    public byte getBlock(int x, int y, int z) {
        if (y < 0 || y >= Chunk.HEIGHT) return 0;
        Chunk chunk = getChunkAt(x, z);
        if (chunk == null) return 0;
        return chunk.getBlock(x & 15, y, z & 15);
    }

    public Chunk getChunkAt(int x, int z) {
        int cx = x >> 4;
        int cz = z >> 4;
        return chunks.get(getChunkKey(cx, cz));
    }

    public Chunk getChunk(int cx, int cz) {
        return chunks.get(getChunkKey(cx, cz));
    }

    public void addChunk(Chunk chunk) {
        chunks.put(getChunkKey(chunk.getChunkX(), chunk.getChunkZ()), chunk);
        terrainGenerator.generateChunk(chunk);
    }

    public void removeChunk(int cx, int cz) {
        Chunk chunk = chunks.remove(getChunkKey(cx, cz));
        if (chunk != null) chunk.cleanup();
    }

    private long getChunkKey(int cx, int cz) {
        return (((long)cx) << 32) | (cz & 0xffffffffL);
    }
    
    public ConcurrentHashMap<Long, Chunk> getChunks() { return chunks; }
}
