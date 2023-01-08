package crpth.util.controller

import crpth.util.RichWindow
import crpth.util.type.Direction
import org.lwjgl.glfw.GLFW
import java.nio.FloatBuffer

interface Controller {

    val richWindow: RichWindow

    var isEnabled: Boolean

    val directions: Set<Direction>

    fun update()

    fun enable() {
        isEnabled = true
    }

    fun disable() {
        isEnabled = false
    }

}

class ControllerManager(override val richWindow: RichWindow) : Controller {

    override var isEnabled = true

    val keyboard = ControllerKeyboard(richWindow)
    val hat = ControllerHat(richWindow)
    val joystick = ControllerJoystick(richWindow)

    override val directions get() = joystick.directions + keyboard.directions

    override fun update() {

        if(!isEnabled)
            return

        keyboard.enable()
        hat.enable()
        joystick.enable()

        joystick.update()
        hat.update()
        keyboard.update()

    }

}

class ControllerKeyboard(override val richWindow: RichWindow) : Controller {

    override val directions = mutableSetOf<Direction>()

    override var isEnabled = true

    override fun update() {

        directions.clear()

        if(!isEnabled)
            return

        if(richWindow.isKeyDown(GLFW.GLFW_KEY_UP) || richWindow.isKeyDown(GLFW.GLFW_KEY_W))
            directions += Direction.NORTH

        if(richWindow.isKeyDown(GLFW.GLFW_KEY_RIGHT) || richWindow.isKeyDown(GLFW.GLFW_KEY_D))
            directions += Direction.EAST

        if(richWindow.isKeyDown(GLFW.GLFW_KEY_DOWN) || richWindow.isKeyDown(GLFW.GLFW_KEY_S))
            directions += Direction.SOUTH

        if(richWindow.isKeyDown(GLFW.GLFW_KEY_LEFT) || richWindow.isKeyDown(GLFW.GLFW_KEY_A))
            directions += Direction.WEST

    }

}

class ControllerHat(override val richWindow: RichWindow, val joystickID: Int = GLFW.GLFW_JOYSTICK_1) : Controller {

    override val directions = mutableSetOf<Direction>()

    override var isEnabled = true

    val isPresent get() = GLFW.glfwJoystickPresent(joystickID)

    var hat_n = 0

    override fun update() {

        directions.clear()

        if(!isEnabled)
            return

        if(!isPresent)
            return

        val hats = GLFW.glfwGetJoystickHats(joystickID)

        val hat = hats?.get(hat_n)?.toInt() ?: return

        if(hat and GLFW.GLFW_HAT_UP != 0)
            directions += Direction.NORTH

        if(hat and GLFW.GLFW_HAT_RIGHT != 0)
            directions += Direction.EAST

        if(hat and GLFW.GLFW_HAT_DOWN != 0)
            directions += Direction.SOUTH

        if(hat and GLFW.GLFW_HAT_LEFT != 0)
            directions += Direction.WEST

    }

}

class ControllerJoystick(override val richWindow: RichWindow, val joystickID: Int = GLFW.GLFW_JOYSTICK_1) : Controller {

    override val directions = mutableSetOf<Direction>()

    override var isEnabled = true

    val isPresent get() = GLFW.glfwJoystickPresent(joystickID)

    var axes: FloatBuffer? = null

    override fun update() {

        directions.clear()

        if(!isEnabled)
            return

        if(!isPresent)
            return

        axes = GLFW.glfwGetJoystickAxes(joystickID)

        val axes = axes ?: return

        if(axes[1] < -0.5f)
            directions += Direction.NORTH

        if(axes[0] > 0.5f)
            directions += Direction.EAST

        if(axes[1] > 0.5f)
            directions += Direction.SOUTH

        if(axes[0] < -0.5f)
            directions += Direction.WEST

    }

}