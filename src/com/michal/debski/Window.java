package com.michal.debski;

import static org.lwjgl.glfw.GLFW.*;

public class Window
{
    private static long window;

    static void SetWindow(long win)
    {
       window = win;
    }

    static void FocusWindow()
    {
        glfwFocusWindow(window);
    }

    static void ShowWindow()
    {
        glfwShowWindow(window);
    }

    static void RestoreWindow()
    {
        glfwRestoreWindow(window);
    }

    static void CursorDisabled(boolean cursorDisabled)
    {
        if(cursorDisabled)
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        else
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }
}
