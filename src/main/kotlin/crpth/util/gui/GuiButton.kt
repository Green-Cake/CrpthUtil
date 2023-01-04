package crpth.util.gui

import crpth.util.RichWindow
import crpth.util.mouse.MouseAction
import crpth.util.mouse.MouseButton
import crpth.util.render.Renderer
import crpth.util.type.BoundingRectangle
import crpth.util.vec.Vec2f

class GuiButton(z: Int, pos: Vec2f, size: Vec2f) : GuiNode(z, pos, size) {

    val bb = BoundingRectangle(pos, size)

    override fun update() {

    }

    override fun render(renderer: Renderer) {

    }

    override fun onClicked(window: RichWindow, button: MouseButton, action: MouseAction): Boolean {

        val cursor = window.cursorPos

        if(cursor !in bb)
            return false

        return true

    }


}