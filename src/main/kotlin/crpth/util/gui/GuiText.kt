package crpth.util.gui

import crpth.util.RichWindow
import crpth.util.mouse.MouseAction
import crpth.util.mouse.MouseButton
import crpth.util.render.Renderer
import crpth.util.render.font.TruetypeFont
import crpth.util.vec.Vec2f
import crpth.util.vec.Vec4f

class GuiText(z: Int, pos: Vec2f, val height: Float, val text: String, val ttf: TruetypeFont, val colorFill: Vec4f, val colorStroke: Vec4f? = null, val spacing: Float = 0f) : GuiNode(z, pos, Vec2f(height, height)) {

    override fun update() = Unit

    override fun render(renderer: Renderer) {

        renderer.renderString(text, ttf, pos, height, colorFill, colorStroke, true, spacing = spacing)

    }

    override fun onClicked(window: RichWindow, button: MouseButton, action: MouseAction) = false

}