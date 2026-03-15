package world;

import blocks.BlockRegistry;
import org.joml.SimplexNoise;

import java.util.Random;

public class TerrainGenerator {
    private final long seed;
    private final Random random;

    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    public void generateChunk(Chunk chunk) {
        int cx = chunk.getChunkX() * Chunk.SIZE;
        int cz = chunk.getChunkZ() * Chunk.SIZE;

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int worldX = cx + x;
                int worldZ = cz + z;

                float exSt = SimplexNoise.noise((worldX + seed) * 0.005f, (worldZ + seed) * 0.005f);
                float moisture = SimplexNoise.noise((worldX - seed) * 0.005f, (worldZ - seed) * 0.005f);

                Biome biome = determineBiome(exSt, moisture);
                float noise = SimplexNoise.noise(worldX * 0.01f, worldZ * 0.01f);
                float detailedNoise = SimplexNoise.noise(worldX * 0.05f, worldZ * 0.05f) * 0.5f;
                
                int baseHeight = biome == Biome.MOUNTAINS ? 100 : 64;
                int heightVariation = biome == Biome.MOUNTAINS ? 40 : (biome == Biome.PLAINS ? 5 : 15);
                
                int height = (int) (baseHeight + (noise + detailedNoise) * heightVariation);
                if (height >= Chunk.HEIGHT) height = Chunk.HEIGHT - 1;

                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    if (y == 0) chunk.setBlock(x, y, z, BlockRegistry.BEDROCK);
                    else if (y < height - 4) chunk.setBlock(x, y, z, BlockRegistry.STONE);
                    else if (y < height) {
                        byte sub = biome == Biome.DESERT ? BlockRegistry.SAND : BlockRegistry.DIRT;
                        chunk.setBlock(x, y, z, sub);
                    } else if (y == height) {
                        byte top = biome == Biome.DESERT ? BlockRegistry.SAND : BlockRegistry.GRASS;
                        chunk.setBlock(x, y, z, top);
                    } else if (y <= 62 && biome != Biome.DESERT) {
                        if (chunk.getBlock(x, y, z) == BlockRegistry.AIR) {
                            chunk.setBlock(x, y, z, BlockRegistry.WATER);
                        }
                    }
                }

                if (biome == Biome.FOREST && height > 62 && random.nextFloat() < 0.02f) {
                    generateTree(chunk, x, height + 1, z);
                }
            }
        }
        chunk.setDirty(true);
    }

    private Biome determineBiome(float elevation, float moisture) {
        if (elevation > 0.4f) return Biome.MOUNTAINS;
        if (moisture < 0f) return Biome.DESERT;
        if (moisture > 0.3f) return Biome.FOREST;
        return Biome.PLAINS;
    }

    private void generateTree(Chunk chunk, int x, int y, int z) {
        if (x < 2 || x > Chunk.SIZE - 3 || z < 2 || z > Chunk.SIZE - 3 || y > Chunk.HEIGHT - 6) return;
        for (int i = 0; i < 4; i++) chunk.setBlock(x, y + i, z, BlockRegistry.WOOD);
        for (int ly = y + 3; ly <= y + 5; ly++) {
            for (int lx = x - 2; lx <= x + 2; lx++) {
                for (int lz = z - 2; lz <= z + 2; lz++) {
                    if (chunk.getBlock(lx, ly, lz) == BlockRegistry.AIR) {
                        chunk.setBlock(lx, ly, lz, BlockRegistry.LEAVES);
                    }
                }
            }
        }
    }
}
