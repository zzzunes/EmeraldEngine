package RenderEngine;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class DisplayManager {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int FPS = 60;
    private static final String TITLE = "Emerald Engine";
    private static long window;

    public static long createDisplay() {
        if (!glfwInit())
            throw new IllegalStateException("Could not initialize GLFW.");

        window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create GLFW window.");

        initializeGL(window);
        glClearColor(0.1f, 0.4f, 0.1f, 0.0f);
        GL11.glViewport(0, 0, WIDTH, HEIGHT);
        setupOrthoProjection();

        return window;
    }

    public static void updateDisplay() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glfwSwapBuffers(window);
    }

    public static void closeDisplay() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private static void initializeGL(long window) {
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSwapInterval(1);
    }

    private static void setupOrthoProjection() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, WIDTH, HEIGHT, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }
}
