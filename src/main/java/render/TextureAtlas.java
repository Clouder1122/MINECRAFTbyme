package render;

import org.lwjgl.system.MemoryStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class TextureAtlas {
    private int textureId;

    public TextureAtlas(String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true);
            ByteBuffer image = stbi_load(path, w, h, comp, 4);
            if (image == null) {
                System.out.println("No atlas texture found at " + path + ". Generating placeholder.");
                createFallbackTexture();
                return;
            }

            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(), h.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            glGenerateMipmap(GL_TEXTURE_2D);
            stbi_image_free(image);
        }
    }
    
    // Generates a simple 16x16 grid of flat colors for block prototyping
    private void createFallbackTexture() {
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        int w = 256, h = 256; 
        int tileSize = 256 / 16;
        ByteBuffer buffer = org.lwjgl.system.MemoryUtil.memAlloc(w * h * 4);
        
        int[][] colors = {
            {255, 255, 255}, // AIR (0)
            {139, 69, 19},   // DIRT (1)
            {34, 139, 34},   // GRASS TOP (2)
            {107, 142, 35},  // GRASS SIDE (3)
            {128, 128, 128}, // STONE (4)
            {160, 82, 45},   // WOOD (5)
            {222, 184, 135}, // LOG SIDE (6)
            {0, 100, 0},     // LEAVES (7)
            {238, 214, 175}, // SAND (8)
            {0, 0, 255},     // WATER (9)
            {50, 50, 50}     // BEDROCK (10)
        };

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int tileX = x / tileSize;
                int tileY = y / tileSize;
                int tileIdx = tileY * 16 + tileX;
                
                int[] c = {200, 200, 200}; // Default missing
                if (tileIdx < colors.length) c = colors[tileIdx];
                int alpha = (tileIdx == 0) ? 0 : (tileIdx == 9 ? 200 : 255); // Air is invisible, water transparent

                // Add grid borders for better visuals
                if (x % tileSize == 0 || y % tileSize == 0) {
                    c[0] = (int)(c[0] * 0.8); c[1] = (int)(c[1] * 0.8); c[2] = (int)(c[2] * 0.8); 
                }

                buffer.put((byte)c[0]);
                buffer.put((byte)c[1]);
                buffer.put((byte)c[2]);
                buffer.put((byte)alpha);
            }
        }
        buffer.flip();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);
        org.lwjgl.system.MemoryUtil.memFree(buffer);
    }
    
    public void bind() { glBindTexture(GL_TEXTURE_2D, textureId); }
    public void cleanup() { glDeleteTextures(textureId); }
}
