package crpth.util

import crpth.util.annotation.RequireGLFWInit
import crpth.util.vec.Vec2i
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.system.MemoryStack

/**
 * Represents a pointer of a window allocated on GLFW, with some functions of use.
 *
 * **ATTENTION** If you want to receive null for Window, use [Window] for type and null for [NULL_W] rather than "Window?"
 *
 * @see RichWindow
 *
 * @constructor
 * You can make directly by passing window id value or create window with parameters through [Window.create]
 * @param id The pointer value.
 */
@RequireGLFWInit
@JvmInline
value class Window(val id: Long) {

    companion object {

        /**
         * Represents null pointer. Do not call any member methods of this instance.
         */
        val NULL_W = Window(0)

        /**
         * A simple wrapper of [GLFW.glfwCreateWindow]. Look at the wrapped function for details.
         *
         * Call this after [GLFW.glfwInit] is called.
         *
         * @param width window's width
         * @param height window's height
         * @param title window's title
         * @param monitor If specified, the window will be fullscreen.
         * @param share If specified, the window will be subordinate to [share].
         * @see GLFW.glfwCreateWindow
         */
        @RequireGLFWInit
        fun create(width: Int, height: Int, title: CharSequence, monitor: Long = 0, share: Window = NULL_W) = Window(GLFW.glfwCreateWindow(width, height, title, monitor, share.id))

    }

    /**
     * If true, the window is in fullscreen mode.
     * @see setFullscreen
     */
    val isFullscreen get() = GLFW.glfwGetWindowMonitor(id) != 0L

    fun setWindowSizeLimits(min: Vec2i, max: Vec2i) = GLFW.glfwSetWindowSizeLimits(id, min.x, min.y, max.x, max.y)

    fun isNull() = id == 0L

    fun freeCallbacks() = Callbacks.glfwFreeCallbacks(id)

    fun destroy() = GLFW.glfwDestroyWindow(id)

    fun makeContextCurrent() = GLFW.glfwMakeContextCurrent(id)

    fun show() = GLFW.glfwShowWindow(id)

    fun shouldClose() = GLFW.glfwWindowShouldClose(id)

    fun swapBuffers() = GLFW.glfwSwapBuffers(id)

    fun getWindowSize(): Vec2i {

        val width: Int
        val height: Int

        MemoryStack.stackPush().use {
            val pWidth = it.mallocInt(1)
            val pHeight = it.mallocInt(1)
            GLFW.glfwGetWindowSize(id, pWidth, pHeight)
            width = pWidth.get()
            height = pHeight.get()
        }

        return Vec2i(width, height)
    }

    fun setWindowSize(size: Vec2i) {
        GLFW.glfwSetWindowSize(id, size.x, size.y)
    }

    fun setAspectRatio(numer: Int, denom: Int) = GLFW.glfwSetWindowAspectRatio(id, numer, denom)

    /**
     * Changes fullscreen-mode.
     * @param value True for fullscreen or false for windowed.
     */
    fun setFullscreen(value: Boolean) {

        if(isFullscreen == value)
            return

        val wsize = getWindowSize()

        if(value) {
            val monitor = GLFW.glfwGetPrimaryMonitor()
            val vidmode = GLFW.glfwGetVideoMode(monitor) ?: return
            GLFW.glfwSetWindowMonitor(id, monitor, 0, 0, vidmode.width(), vidmode.height(), GLFW.GLFW_DONT_CARE)
        } else {
            GLFW.glfwSetWindowMonitor(id, 0L, 50, 50, wsize.x * 3 / 4, wsize.y * 3 / 4, GLFW.GLFW_DONT_CARE)
        }

    }

}

inline fun Window.setWindowSizeCallback(crossinline callback: (window: Window, size: Vec2i)->Unit) = GLFW.glfwSetWindowSizeCallback(id) { windowId, w, h ->
    callback(Window(windowId), Vec2i(w, h))
}
