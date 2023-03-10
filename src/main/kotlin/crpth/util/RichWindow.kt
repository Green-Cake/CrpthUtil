package crpth.util

import crpth.util.annotation.RequireGLFWInit
import crpth.util.mouse.MouseAction
import crpth.util.mouse.MouseButton
import crpth.util.vec.Vec2f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack

/**
 * [Window] is so simple an inline class that it cannot deal with keyboard, cursor or something.
 *
 * @see update
 */
class RichWindow(val window: Window, var fOnClicked: (button: MouseButton, action: MouseAction)->Boolean) {

    val keyMapPrev = mutableSetOf<Int>()
    val keyMap = mutableSetOf<Int>()

    var cursorPos = Vec2f.ZERO
        private set

    /**
     * Call this after [GLFW.glfwInit] is called.
     */
    @RequireGLFWInit
    fun init() {

        val windowSize = window.getWindowSize()

        GLFW.glfwSetKeyCallback(window.id, ::onKeyEvent)

        GLFW.glfwSetCursorPosCallback(window.id) { _, x, y ->
            cursorPos = Vec2f(x.toFloat() / windowSize.x, 1.0f - y.toFloat() / windowSize.y) * 2.0f - Vec2f.ONE
        }

        GLFW.glfwSetMouseButtonCallback(window.id) { _, button, action, _, ->
            onClicked(MouseButton.from(button), MouseAction.from(action))
        }

        GLFW.glfwSetWindowSizeCallback(window.id) { window, width, height ->

            GL11.glViewport(0, 0, width, height)

        }

        MemoryStack.stackPush().use {

            val cx = it.mallocDouble(1)
            val cy = it.mallocDouble(1)
            GLFW.glfwGetCursorPos(window.id, cx, cy)
            cursorPos = Vec2f(cx[0].toFloat() / windowSize.x, 1.0f - cy[0].toFloat() / windowSize.y) *2f - Vec2f.ONE

        }

    }

    fun onKeyEvent(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {

        if(action == GLFW.GLFW_RELEASE)
            keyMap.remove(key)
        else
            keyMap += key

    }

    fun onClicked(button: MouseButton, action: MouseAction): Boolean = fOnClicked(button, action)

    fun update() {
        keyMapPrev.clear()
        keyMapPrev.addAll(keyMap)
    }

    fun isKeyPressed(key: Int): Boolean {
        return key !in keyMapPrev && key in keyMap
    }

    fun isKeyRepeated(key: Int): Boolean {
        return key in keyMapPrev && key in keyMap
    }

    fun isKeyDown(key: Int): Boolean {
        return key in keyMap
    }

    fun isKeyGetReleased(key: Int): Boolean {
        return key in keyMapPrev && key !in keyMap
    }

    fun isKeyReleased(key: Int): Boolean {
        return key !in keyMap
    }

    fun resetInput() {
        keyMapPrev.clear()
        keyMap.clear()
    }

}