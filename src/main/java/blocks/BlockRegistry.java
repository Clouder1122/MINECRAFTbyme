package blocks;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {
    public static final byte AIR = 0;
    public static final byte DIRT = 1;
    public static final byte GRASS = 2;
    public static final byte STONE = 3;
    public static final byte WOOD = 4;
    public static final byte LEAVES = 5;
    public static final byte SAND = 6;
    public static final byte WATER = 7;
    public static final byte BEDROCK = 8;
    
    private static final Map<Byte, Block> blocks = new HashMap<>();

    public static void init() {
        register(new Block(AIR, "Air", true, new int[]{0}));
        register(new Block(DIRT, "Dirt", false, new int[]{1})); // Dirt texture
        register(new Block(GRASS, "Grass", false, new int[]{2, 1, 3})); // Top, Bottom, Sides
        register(new Block(STONE, "Stone", false, new int[]{4}));
        register(new Block(WOOD, "Wood", false, new int[]{5, 5, 6}));
        register(new Block(LEAVES, "Leaves", true, new int[]{7}));
        register(new Block(SAND, "Sand", false, new int[]{8}));
        register(new Block(WATER, "Water", true, new int[]{9}));
        register(new Block(BEDROCK, "Bedrock", false, new int[]{10}));
    }

    private static void register(Block block) { blocks.put(block.getId(), block); }
    public static Block get(byte id) { return blocks.getOrDefault(id, blocks.get(AIR)); }
    public static boolean isTransparent(byte id) { return get(id).isTransparent(); }
    
    public static int getTexture(byte id, int face) {
        int[] tex = get(id).getTextureCoords();
        if (tex.length == 1) return tex[0];
        if (face == 0) return tex[0]; // Top
        if (face == 1) return tex[1]; // Bottom
        return tex[2]; // Sides
    }
}
