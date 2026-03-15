package engine;

import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;

public class Input {
    private static boolean[] keys = new boolean[GLFW_KEY_LAST];
    private static boolean[] mouseButtons = new boolean[GLFW_MOUSE_BUTTON_LAST];
    private static double mouseX, mouseY;
    private static double lastMouseX, lastMouseY;
    private static Vector2f mouseDisplacement = new Vector2f();
    private static boolean inWindow = false;
    private static boolean cursorLocked = true;

    private static long windowHandle;

    public static void init(long window) {
        windowHandle = window;

        glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (key >= 0 && key < keys.length) {
                keys[key] = (action != GLFW_RELEASE);
            }
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        glfwSetCursorPosCallback(window, (w, xpos, ypos) -> {
            mouseX = xpos;
            mouseY = ypos;
        });

        glfwSetCursorEnterCallback(window, (w, entered) -> {
            inWindow = entered;
        });

        glfwSetMouseButtonCallback(window, (w, button, action, mods) -> {
            if (button >= 0 && button < mouseButtons.length) {
                mouseButtons[button] = (action != GLFW_RELEASE);
            }
        });
        
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public static void update() {
        mouseDisplacement.x = 0;
        mouseDisplacement.y = 0;
        
        if (inWindow && cursorLocked) {
            double deltax = mouseX - lastMouseX;
            double deltay = mouseY - lastMouseY;
            
            if (deltax != 0 || deltay != 0) {
                boolean rotateX = deltax != 0;
                boolean rotateY = deltay != 0;
                if (rotateX) mouseDisplacement.y = (float) deltax; // mouse movement on X changes yaw (Y axis)
                if (rotateY) mouseDisplacement.x = (float) deltay; // mouse movement on Y changes pitch (X axis)
            }
        }
        
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public static boolean isKeyDown(int keycode) {
        return keys[keycode];
    }

    public static boolean isMouseButtonDown(int button) {
        return mouseButtons[button];
    }

    public static Vector2f getMouseDisplacement() {
        return mouseDisplacement;
    }
}
