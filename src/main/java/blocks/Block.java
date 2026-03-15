package blocks;

public class Block {
    private final byte id;
    private final String name;
    private final boolean transparent;
    private final int[] textureCoords;

    public Block(byte id, String name, boolean transparent, int[] textureCoords) {
        this.id = id;
        this.name = name;
        this.transparent = transparent;
        this.textureCoords = textureCoords;
    }

    public byte getId() { return id; }
    public String getName() { return name; }
    public boolean isTransparent() { return transparent; }
    public int[] getTextureCoords() { return textureCoords; }
}
