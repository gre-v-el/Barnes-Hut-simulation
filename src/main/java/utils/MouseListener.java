package utils;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class MouseListener {
    private static MouseListener instance;
    private double scrollX, scrollY;
    private double xPos, yPos, lastX, lastY;
    private boolean mouseButtonPressed[] = new boolean[3];
    private boolean lastMouseButtonPressed[] = new boolean[3];

    private MouseListener(){
        this.scrollX = 0;
        this.scrollY = 0;
        this.xPos = 0;
        this.yPos = 0;
        this.lastX = 0;
        this.lastY = 0;
    }

    public static MouseListener get(){
        if(instance == null){
            instance = new MouseListener();
        }

        return instance;
    }

    public static void mousePosCallback(long window, double xpos, double ypos){
        get().lastX = get().xPos;
        get().lastY = get().yPos;
        get().xPos = xpos;
        get().yPos = ypos;
    }

    public static void mouseButtonCallback(long window, int button, int action, int mods){
        if(action == GLFW_PRESS && button < get().mouseButtonPressed.length) {
            get().mouseButtonPressed[button] = true;
        }
        else if(action == GLFW_RELEASE && button < get().mouseButtonPressed.length){
            get().mouseButtonPressed[button] = false;
        }
    }

    public static void mouseScrollCallback(long window, double xOffset, double yOffset){
        get().scrollX = xOffset;
        get().scrollY = yOffset;
    }

    public static void endFrame(){
        get().scrollX = 0;
        get().scrollY = 0;
        get().lastX = get().xPos;
        get().lastY = get().yPos;
        get().lastMouseButtonPressed[0] = get().mouseButtonPressed[0];
        get().lastMouseButtonPressed[1] = get().mouseButtonPressed[1];
        get().lastMouseButtonPressed[2] = get().mouseButtonPressed[2];
    }

    public static float getX(){
        return (float)get().xPos;
    }
    public static float getY(){
        return (float)get().yPos;
    }
    public static float getDx(){
        return (float)(get().xPos - get().lastX);
    }
    public static float getDy(){
        return (float)(get().yPos - get().lastY);
    }
    public static float getScrollX(){
        return (float)get().scrollX;
    }
    public static float getScrollY(){
        return (float)get().scrollY;
    }
    public static boolean isButtonDown(int button){
        return get().mouseButtonPressed[button];
    }
    public static boolean isButtonClick(int button) {
        return (get().mouseButtonPressed[button] && !get().lastMouseButtonPressed[button]);
    }
    public static boolean isButtonUnClick(int button) {
        return (!get().mouseButtonPressed[button] && get().lastMouseButtonPressed[button]);
    }
}
