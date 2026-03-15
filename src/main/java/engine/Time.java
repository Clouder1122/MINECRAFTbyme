package engine;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Time {
    private static double lastLoopTime;
    
    public static void init() {
        lastLoopTime = getTime();
    }
    
    public static double getTime() {
        return glfwGetTime();
    }
    
    public static float getDeltaTime() {
        double time = getTime();
        float delta = (float) (time - lastLoopTime);
        lastLoopTime = time;
        return delta;
    }
}
