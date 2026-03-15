package render;

import engine.Window;
import org.joml.Matrix4f;
import world.Chunk;
import world.World;
import java.util.Map;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class Renderer {
    private ShaderProgram shader;
    private Matrix4f projectionMatrix;
    private TextureAtlas textureAtlas;
    private FrustumCulling frustumCulling;

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000.0f;

    public Renderer() throws Exception {
        shader = new ShaderProgram();
        shader.createVertexShader(new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/main/resources/shaders/vertex.glsl"))));
        shader.createFragmentShader(new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/main/resources/shaders/fragment.glsl"))));
        shader.link();
        shader.createUniform("projectionMatrix");
        shader.createUniform("viewMatrix");
        shader.createUniform("modelMatrix");
        shader.createUniform("texture_sampler");
        
        projectionMatrix = new Matrix4f();
        textureAtlas = new TextureAtlas("src/main/resources/textures/atlas.png");
        frustumCulling = new FrustumCulling();
        
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        
        // Basic transparency
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void render(Window window, Camera camera, World world) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        projectionMatrix.identity().perspective(FOV, (float) window.getWidth() / window.getHeight(), Z_NEAR, Z_FAR);
        
        shader.bind();
        shader.setUniform("projectionMatrix", projectionMatrix);
        shader.setUniform("viewMatrix", camera.getViewMatrix());
        shader.setUniform("texture_sampler", 0);

        glActiveTexture(GL_TEXTURE0);
        textureAtlas.bind();
        
        frustumCulling.update(projectionMatrix, camera.getViewMatrix());

        Matrix4f modelMatrix = new Matrix4f();
        for (Map.Entry<Long, Chunk> entry : world.getChunks().entrySet()) {
            Chunk chunk = entry.getValue();
            if (!chunk.isMeshReady() || chunk.getMesh() == null) continue;
            
            float cx = chunk.getChunkX() * Chunk.SIZE;
            float cz = chunk.getChunkZ() * Chunk.SIZE;
            
            if (!frustumCulling.insideFrustum(cx + Chunk.SIZE/2f, Chunk.HEIGHT/2f, cz + Chunk.SIZE/2f, Math.max(Chunk.SIZE, Chunk.HEIGHT))) {
                continue;
            }

            modelMatrix.identity().translate(cx, 0, cz);
            shader.setUniform("modelMatrix", modelMatrix);
            chunk.getMesh().render();
        }

        shader.unbind();
    }
    
    public void cleanup() {
        if (shader != null) shader.cleanup();
        if (textureAtlas != null) textureAtlas.cleanup();
    }
}
