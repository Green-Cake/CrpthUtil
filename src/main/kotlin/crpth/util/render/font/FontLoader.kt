package crpth.util.render.font

import crpth.util.ResourceManager
import crpth.util.render.Texture
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage

object FontLoader {

    private val ctx = FontRenderContext(AffineTransform(), true, true)

    fun load(resourceManager: ResourceManager, chars: CharArray, font: Font, doAntialiasing: Boolean=true): TextureCharacters {

        val textureMap = chars.filter {

            val bound = font.getStringBounds(it.toString(), ctx)

            bound.width != 0.0 && bound.height != 0.0

        }.associateWith {

            val bound = font.getStringBounds(it.toString(), ctx)

            val img = BufferedImage(bound.width.toInt(), bound.height.toInt(), BufferedImage.TYPE_INT_ARGB)

            val graphics = img.createGraphics()

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, if(doAntialiasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF)

            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, if(doAntialiasing) RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB else RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)

            graphics.font = font

            graphics.color = Color.WHITE

            graphics.drawString(it.toString(), 0, img.height - (graphics.fontMetrics.leading + graphics.fontMetrics.descent)/2)

            Texture.load(resourceManager, img)

        }

        return TextureCharacters(font, textureMap.toMutableMap())

    }

    fun load(resourceManager: ResourceManager, chars: Array<out CharRange>, font: Font) =
        load(resourceManager, buildString {
            for (range in chars) for (c in range) {
                append(c)
            }
        }.toCharArray(), font)

    fun load(resourceManager: ResourceManager, char: Char, font: Font) = load(resourceManager, charArrayOf(char), font)

}