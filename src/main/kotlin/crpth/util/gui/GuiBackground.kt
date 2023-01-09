package crpth.util.gui

import crpth.util.RichWindow
import crpth.util.mouse.MouseAction
import crpth.util.mouse.MouseButton
import crpth.util.render.Renderer
import crpth.util.vec.Vec2f
import crpth.util.vec.Vec4b
import crpth.util.vec.Vec4f

class GuiBackground(z: Int, pos: Vec2f, size: Vec2f, val colorFill: Vec4f, val colorStroke: Vec4f) : GuiNode(z, pos, size) {

    override fun update() = Unit

    override fun render(renderer: Renderer) {

        renderer.glColor4f(colorFill)
        renderer.drawSquare(pos, size)

//        renderer.line = 2.0f
//        renderer.lineSmooth = true
//
//        renderer.color4b(colorStroke)
//        renderer.drawLineSquare(pos, size)

    }

    override fun onClicked(window: RichWindow, button: MouseButton, action: MouseAction) = false
}