package crpth.util.gui

import crpth.util.RichWindow
import crpth.util.mouse.MouseAction
import crpth.util.mouse.MouseButton
import crpth.util.render.Renderer
import crpth.util.render.font.FontConfig
import crpth.util.vec.Vec2f

class GuiText(z: Int, pos: Vec2f, val height: Float, val text: String, val fontConfig: FontConfig) : GuiNode(z, pos, Vec2f(height, height)) {

    override fun update() = Unit

    override fun render(renderer: Renderer) {

        renderer.renderString(text, pos, height, fontConfig)

    }

    override fun onClicked(window: RichWindow, button: MouseButton, action: MouseAction) = false

}