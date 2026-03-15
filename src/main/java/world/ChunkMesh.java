package world;

import blocks.BlockRegistry;
import org.lwjgl.system.MemoryUtil;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL30.*;

public class ChunkMesh {
    private int vaoId, vboId;
    private int vertexCount;
    
    public ChunkMesh() {}

    public void generate(Chunk chunk, World world) {
        List<Float> vertices = new ArrayList<>();
        
        int[] dX = {0, 0, -1, 1, 0, 0};
        int[] dY = {1, -1, 0, 0, 0, 0};
        int[] dZ = {0, 0, 0, 0, -1, 1};
        
        for (int y = 0; y < Chunk.HEIGHT; y++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                for (int x = 0; x < Chunk.SIZE; x++) {
                    byte blockId = chunk.getBlock(x, y, z);
                    if (blockId == BlockRegistry.AIR) continue;
                    
                    for (int f = 0; f < 6; f++) {
                        int nx = x + dX[f], ny = y + dY[f], nz = z + dZ[f];
                        
                        boolean isOpaque = true;
                        if (nx >= 0 && nx < Chunk.SIZE && ny >= 0 && ny < Chunk.HEIGHT && nz >= 0 && nz < Chunk.SIZE) {
                            byte neighborId = chunk.getBlock(nx, ny, nz);
                            if (!BlockRegistry.isTransparent(neighborId)) isOpaque = false;
                        } else {
                            if (world != null) {
                                int wx = chunk.getChunkX() * Chunk.SIZE + nx;
                                int wy = ny;
                                int wz = chunk.getChunkZ() * Chunk.SIZE + nz;
                                byte nId = world.getBlock(wx, wy, wz);
                                if (!BlockRegistry.isTransparent(nId)) isOpaque = false;
                            }
                        }
                        
                        if (isOpaque) {
                            addFace(vertices, x, y, z, f, blockId);
                        }
                    }
                }
            }
        }
        
        vertexCount = vertices.size() / 8; // x, y, z, u, v, nx, ny, nz
        if (vertexCount == 0) return;
        
        FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.size());
        for (float v : vertices) buffer.put(v);
        buffer.flip();
        
        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();
        
        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        
        int stride = 8 * 4;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0); // pos
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3 * 4); // tex
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 5 * 4); // normal
        glEnableVertexAttribArray(2);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        MemoryUtil.memFree(buffer);
    }
    
    private void addFace(List<Float> verts, float x, float y, float z, int face, byte blockId) {
        int texIndex = BlockRegistry.getTexture(blockId, face == 0 ? 0 : (face == 1 ? 1 : 2));
        float tx = (texIndex % 16) / 16.0f;
        float ty = (texIndex / 16) / 16.0f;
        float tw = 1.0f / 16.0f;
        
        float[] p = getFaceVertices(x, y, z, face);
        float[] uv = {
            tx+tw, ty,    tx, ty,    tx, ty+tw,   tx+tw, ty,   tx, ty+tw,   tx+tw, ty+tw
        };
        
        float nx = 0, ny = 0, nz = 0;
        switch(face) {
            case 0: ny = 1; break;
            case 1: ny = -1; break;
            case 2: nx = -1; break;
            case 3: nx = 1; break;
            case 4: nz = -1; break;
            case 5: nz = 1; break;
        }

        for(int i=0; i<6; i++) {
            verts.add(p[i*3]); verts.add(p[i*3+1]); verts.add(p[i*3+2]);
            verts.add(uv[i*2]); verts.add(uv[i*2+1]);
            verts.add(nx); verts.add(ny); verts.add(nz);
        }
    }

    private float[] getFaceVertices(float x, float y, float z, int face) {
        float w = 1.0f, h = 1.0f, d = 1.0f;
        switch (face) {
            case 0: return new float[]{x+w,y+h,z, x,y+h,z, x,y+h,z+d, x+w,y+h,z, x,y+h,z+d, x+w,y+h,z+d}; 
            case 1: return new float[]{x+w,y,z+d, x,y,z+d, x,y,z, x+w,y,z+d, x,y,z, x+w,y,z}; 
            case 2: return new float[]{x,y+h,z, x,y,z, x,y,z+d, x,y+h,z, x,y,z+d, x,y+h,z+d}; 
            case 3: return new float[]{x+w,y+h,z+d, x+w,y,z+d, x+w,y,z, x+w,y+h,z+d, x+w,y,z, x+w,y+h,z}; 
            case 4: return new float[]{x,y+h,z, x+w,y+h,z, x+w,y,z, x,y+h,z, x+w,y,z, x,y,z}; 
            case 5: return new float[]{x+w,y+h,z+d, x,y+h,z+d, x,y,z+d, x+w,y+h,z+d, x,y,z+d, x+w,y,z+d}; 
        }
        return new float[18];
    }

    public void render() {
        if (vertexCount == 0) return;
        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    public void cleanup() {
        if (vaoId != 0) glDeleteVertexArrays(vaoId);
        if (vboId != 0) glDeleteBuffers(vboId);
        vertexCount = 0;
    }
}
