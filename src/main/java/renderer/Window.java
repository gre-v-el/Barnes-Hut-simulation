package renderer;

import program.ImGuiLayer;
import program.Scene;


import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import program.SpaceScene;
import utils.KeyListener;
import utils.MouseListener;
import utils.Time;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private static int width, height;
    private String title;
    private long glfwWindow = NULL;
    private int targetFPS = 120;

    private static Scene currentScene;

    private static ImGuiLayer guiLayer;

    private static Window window = null;

    private Window(){
        this.width = 1000;
        this.height = 500;
        this.title = "no idea for a title";
    }

    public static Window get() {
        if(Window.window == null){
            Window.window = new Window();
        }

        return Window.window;
    }

    private void windowSizeCallback(long window, int width, int height) {

        get().width = width;
        get().height = height;
        glfwSetWindowSize(glfwWindow, width, height);
        glViewport(0, 0, width, height);
        currentScene.getCamera().setAspect(width, height);
        currentScene.getCamera().calcMatrices();
        currentScene.setCameraChange(true);
    }

    public void run(){
        init();
        loop();

        currentScene.end();

        // Glfw stuff to free the memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);


        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if ( window == null )
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetInputMode(glfwWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        glfwSetWindowSizeLimits(glfwWindow, 667, 579, GLFW_DONT_CARE, GLFW_DONT_CARE);

        int[] tempW = {width};
        int[] tempH = {height};
        glfwGetWindowSize(glfwWindow, tempW, tempH);
        width = tempW[0];
        height = tempH[0];

        glfwMakeContextCurrent(glfwWindow);
        //glfwSwapInterval(1);

        glfwShowWindow(glfwWindow);

        GL.createCapabilities();


        Window.changeScene(new SpaceScene());

        // event listeners
        glfwSetCursorPosCallback(glfwWindow, get()::mousePosCallback);
//        glfwSetMouseButtonCallback(glfwWindow, get()::mouseButtonCallback);
//        glfwSetScrollCallback(glfwWindow, get()::mouseScrollCallback);
//        glfwSetKeyCallback(glfwWindow, get()::keyCallback);
        glfwSetWindowSizeCallback(glfwWindow, get()::windowSizeCallback);

        guiLayer = new ImGuiLayer(Window.getWindowID());
        guiLayer.initImGui();
    }

    public void keyCallback(long l, int i, int i1, int i2, int i3) {
        KeyListener.keyCallback(l, i, i1, i2, i3);
    }

    public void mouseScrollCallback(long l, double v, double v1) {
        MouseListener.mouseScrollCallback(l, v, v1);
    }

    public void mouseButtonCallback(long l, int i, int i1, int i2) {
        MouseListener.mouseButtonCallback(l, i, i1, i2);
    }

    public void mousePosCallback(long window, double xpos, double ypos){
        MouseListener.mousePosCallback(window, xpos, ypos);
    }

    private void loop() {
        // delta time setup
        float[] times = new float[2];
        /*
            0 - begin
            1 - end
         */
        float frameDT = -1f;

        glClearColor(currentScene.getBackgroundColor()[0], currentScene.getBackgroundColor()[1], currentScene.getBackgroundColor()[2], 1f);

        // main loop
        while ( !glfwWindowShouldClose(glfwWindow) ) {
            times[0] = Time.getTime();

            glfwPollEvents();

            if(frameDT > 0) {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                currentScene.inputs(frameDT);
                currentScene.update(frameDT);
                currentScene.render(frameDT);
                guiLayer.update(frameDT, currentScene);

                glfwSwapBuffers(glfwWindow);
                glfwSetWindowTitle(glfwWindow, "" + (1f / frameDT) + "FPS");
            }

            // delta time managing
            frameDT = Time.getTime()-times[1];
            times[1] = Time.getTime();
        }
    }

    public static void changeScene(Scene newScene){
        currentScene = newScene;
        currentScene.init();
    }

    public static int getWidth(){
        return width;
    }

    public static int getHeight(){
        return height;
    }

    public static long getWindowID(){
        return get().glfwWindow;
    }
}
