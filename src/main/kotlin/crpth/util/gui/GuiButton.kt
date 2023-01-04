package crpth.util.gui

import crpth.util.RichWindow
import crpth.util.mouse.MouseAction
import crpth.util.mouse.MouseButton
import crpth.util.render.Renderer
import crpth.util.type.BoundingRectangle
import crpth.util.vec.Vec2f

class GuiButton(z: Int, pos: Vec2f, size: Vec2f, val text: GuiText?, val actionClicked: ()->Unit) : GuiNode(z, pos, size) {

    val bb = BoundingRectangle(pos, size)

    override fun update() {
        text?.update()
    }

    override fun render(renderer: Renderer) {
        text?.render(renderer)
    }

    override fun onClicked(window: RichWindow, button: MouseButton, action: MouseAction): Boolean {

        val cursor = window.cursorPos

        if(cursor !in bb)
            return false

        if(button == MouseButton.LEFT && action == MouseAction.PRESS) {
            actionClicked()
        }

        return true

    }


}