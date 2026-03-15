package player;

import engine.Input;
import world.World;
import render.Camera;
import org.joml.Vector3f;
import blocks.BlockRegistry;
import static org.lwjgl.glfw.GLFW.*;

public class PlayerController {
    private Camera camera;
    private World world;
    private float width = 0.6f, height = 1.8f;
    private float yVelocity = 0;
    private boolean onGround = false;
    private float speed = 5.0f;
    private boolean creativeMode = true;

    private long lastClickTime = 0;
    private byte selectedBlock = BlockRegistry.WOOD;

    public PlayerController(Camera camera, World world) {
        this.camera = camera;
        this.world = world;
        camera.getPosition().y = 100;
    }

    public void update(float dt) {
        // Toggle game modes (Stage 9)
        if (Input.isKeyDown(GLFW_KEY_C)) creativeMode = true;
        if (Input.isKeyDown(GLFW_KEY_V)) creativeMode = false;

        org.joml.Vector2f mouseDisp = Input.getMouseDisplacement();
        camera.moveRotation(mouseDisp.x * 0.2f, mouseDisp.y * 0.2f, 0);

        Vector3f move = new Vector3f(0, 0, 0);
        if (Input.isKeyDown(GLFW_KEY_W)) move.z = -1;
        if (Input.isKeyDown(GLFW_KEY_S)) move.z = 1;
        if (Input.isKeyDown(GLFW_KEY_A)) move.x = -1;
        if (Input.isKeyDown(GLFW_KEY_D)) move.x = 1;
        
        // Fast scroll for hotbar (Stage 8)
        if (Input.isKeyDown(GLFW_KEY_1)) selectedBlock = BlockRegistry.DIRT;
        else if (Input.isKeyDown(GLFW_KEY_2)) selectedBlock = BlockRegistry.STONE;
        else if (Input.isKeyDown(GLFW_KEY_3)) selectedBlock = BlockRegistry.WOOD;
        else if (Input.isKeyDown(GLFW_KEY_4)) selectedBlock = BlockRegistry.LEAVES;

        if (creativeMode) {
            if (Input.isKeyDown(GLFW_KEY_SPACE)) move.y = 1;
            if (Input.isKeyDown(GLFW_KEY_LEFT_SHIFT)) move.y = -1;
            camera.movePosition(move.x * speed * dt * 2, move.y * speed * dt * 2, move.z * speed * dt * 2);
        } else {
            float dx = 0, dz = 0;
            if (move.z != 0) {
                dx += (float)Math.sin(Math.toRadians(camera.getRotation().y)) * -1.0f * move.z * speed * dt;
                dz += (float)Math.cos(Math.toRadians(camera.getRotation().y)) * move.z * speed * dt;
            }
            if (move.x != 0) {
                dx += (float)Math.sin(Math.toRadians(camera.getRotation().y - 90)) * -1.0f * move.x * speed * dt;
                dz += (float)Math.cos(Math.toRadians(camera.getRotation().y - 90)) * move.x * speed * dt;
            }

            Vector3f p = camera.getPosition();
            
            if (!checkCollision(p.x + dx, p.y, p.z)) p.x += dx;
            if (!checkCollision(p.x, p.y, p.z + dz)) p.z += dz;

            yVelocity -= 25.0f * dt; // Gravity
            if (Input.isKeyDown(GLFW_KEY_SPACE) && onGround) {
                yVelocity = 8.5f;
                onGround = false;
            }

            if (!checkCollision(p.x, p.y + yVelocity * dt, p.z)) {
                p.y += yVelocity * dt;
                onGround = false;
            } else {
                if (yVelocity < 0) onGround = true; // Landed
                yVelocity = 0;
            }
        }

        handleRaycast();
    }

    private boolean checkCollision(float x, float y, float z) {
        int minX = (int) Math.floor(x - width/2);
        int maxX = (int) Math.floor(x + width/2);
        int minY = (int) Math.floor(y - height + 0.1f);
        int maxY = (int) Math.floor(y + 0.1f);
        int minZ = (int) Math.floor(z - width/2);
        int maxZ = (int) Math.floor(z + width/2);

        for (int bX = minX; bX <= maxX; bX++) {
            for (int bY = minY; bY <= maxY; bY++) {
                for (int bZ = minZ; bZ <= maxZ; bZ++) {
                    if (world.getBlock(bX, bY, bZ) != BlockRegistry.AIR) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void handleRaycast() {
        if (System.currentTimeMillis() - lastClickTime < 200) return;

        boolean leftClick = Input.isMouseButtonDown(GLFW_MOUSE_BUTTON_1);
        boolean rightClick = Input.isMouseButtonDown(GLFW_MOUSE_BUTTON_2);

        if (!leftClick && !rightClick) return;

        Vector3f pos = new Vector3f(camera.getPosition());
        Vector3f dir = new Vector3f(0, 0, -1);
        dir.rotateX((float)Math.toRadians(-camera.getRotation().x));
        dir.rotateY((float)Math.toRadians(-camera.getRotation().y));

        float step = 0.1f;
        float maxDist = 5.0f;
        Vector3f lastAir = new Vector3f(pos);

        for (float d = 0; d < maxDist; d += step) {
            pos.add(dir.x * step, dir.y * step, dir.z * step);
            int bx = (int) Math.floor(pos.x);
            int by = (int) Math.floor(pos.y);
            int bz = (int) Math.floor(pos.z);

            if (world.getBlock(bx, by, bz) != BlockRegistry.AIR) {
                if (leftClick) {
                    world.setBlock(bx, by, bz, BlockRegistry.AIR);
                } else if (rightClick) {
                    world.setBlock((int)Math.floor(lastAir.x), (int)Math.floor(lastAir.y), (int)Math.floor(lastAir.z), selectedBlock); 
                }
                lastClickTime = System.currentTimeMillis();
                break;
            }
            lastAir.set(pos);
        }
    }
}
