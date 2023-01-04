package crpth.util.render

import crpth.util.ResourceManager
import crpth.util.Window
import crpth.util.experimental.TruetypeFont
import crpth.util.render.font.FontManager
import crpth.util.render.font.TextureCharacters
import crpth.util.vec.*
import org.lwjgl.opengl.GL11.*
import java.awt.Font
import kotlin.math.max

class Renderer(val resourceManager: ResourceManager, val windowGetter: ()->Window) {

    object Constants {

        inline val SIZE_FULL get() = vec(2.0f, 2.0f)

    }

    var doDrawOverline = false
    var doDrawUnderline = false

    val fontManager = FontManager(resourceManager, 256)

    val fontEn = fontManager.fontMonospaced
    val fontJa = fontManager.fontYumincho

    @Suppress("NOTHING_TO_INLINE")
    inline fun vertex2f(vec: Vec2f) = glVertex2f(vec.x, vec.y)

    @Suppress("NOTHING_TO_INLINE")
    inline fun glColor4f(vec4f: Vec4f) = glColor4f(vec4f.a, vec4f.b, vec4f.c, vec4f.d)

    fun color4b(color: Vec4b) {
        glColor4f(color.r.resizeToInt()/255f, color.g.resizeToInt()/255f,  color.b.resizeToInt()/255f, color.a.resizeToInt()/255f)
    }

    fun pixels(p: Int): Vec2f {
        val wsize = windowGetter().getWindowSize()
        return Constants.SIZE_FULL / wsize.toVec2f() * p.toFloat()
    }

    fun pixels(p: Vec2i): Vec2f {
        val wsize = windowGetter().getWindowSize()
        return Constants.SIZE_FULL / wsize.toVec2f() * p.toVec2f()
    }

    inline fun draw(mode: Int, block: ()->Unit) {
        glBegin(mode)
        block()
        glEnd()
    }

    fun drawSquare(position: Vec2f, size: Vec2f)  = draw(GL_QUADS) {
        glVertex2f(position.x, position.y + size.y)
        glVertex2f(position.x, position.y)
        glVertex2f(position.x + size.x, position.y)
        glVertex2f(position.x + size.x, position.y + size.y)
    }

    fun clearScreen(clearColor: Vec4f = Vec4f.BLACK) {
        glClearColor(clearColor.d, clearColor.c, clearColor.c, clearColor.d)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun renderLineStrip(positions: ULongArray) {

        draw(GL_LINE_STRIP) {
            positions.forEach {
                vertex2f(Vec2f(it))
            }
        }

    }

    fun renderTexture(texture: ITexture, position: Vec2f, size: Vec2f, srcStart: Vec2f = Vec2f.ZERO, srcEnd: Vec2f = Vec2f.ONE, initColor: Vec4f?= Vec4f.WHITE) {

        if(initColor != null)
            glColor4f(initColor.a, initColor.b, initColor.c, initColor.d)

        texture.bind()

        draw(GL_QUADS) {

            glTexCoord2f(srcStart.x, srcStart.y)
            glVertex2f(position.x, position.y + size.y)

            glTexCoord2f(srcStart.x, srcEnd.y)
            glVertex2f(position.x, position.y)

            glTexCoord2f(srcEnd.x, srcEnd.y)
            glVertex2f(position.x + size.x, position.y)

            glTexCoord2f(srcEnd.x, srcStart.y)
            glVertex2f(position.x + size.x, position.y + size.y)

        }

        texture.debind()

    }

    fun renderTexture(texture: ITexture, position: Vec2f, height: Float, width: Float, srcStart: Vec2f = Vec2f.ZERO, srcEnd: Vec2f = Vec2f.ONE) {

        texture.bind()

        draw(GL_QUADS) {

            glTexCoord2f(srcStart.x, srcStart.y)
            glVertex2f(position.x, position.y + height)

            glTexCoord2f(srcStart.x, srcEnd.y)
            glVertex2f(position.x, position.y)

            glTexCoord2f(srcEnd.x, srcEnd.y)
            glVertex2f(position.x + width, position.y)

            glTexCoord2f(srcEnd.x, srcStart.y)
            glVertex2f(position.x + width, position.y + height)

        }

        texture.debind()

    }

    /**
     * AA means Auto Aspect
     * @return width value calculated and used for rendering.
     */
    fun renderTextureAA(texture: ITexture, position: Vec2f, height: Float, srcStart: Vec2f = Vec2f.ZERO, srcEnd: Vec2f = Vec2f.ONE): Float {

        texture.bind()

        val aspect = texture.getAspectRatio()

        draw(GL_QUADS) {

            glTexCoord2f(srcStart.x, srcStart.y)
            glVertex2f(position.x, position.y + height)

            glTexCoord2f(srcStart.x, srcEnd.y)
            glVertex2f(position.x, position.y)

            glTexCoord2f(srcEnd.x, srcEnd.y)
            glVertex2f(position.x + height * aspect, position.y)

            glTexCoord2f(srcEnd.x, srcStart.y)
            glVertex2f(position.x + height * aspect, position.y + height)

        }

        texture.debind()

        return height * aspect

    }

    /**
     * @return the width of the rendered character.
     */
    @Deprecated("Please use Truetype Font version.")
    fun renderChar(char: Char, tex: TextureCharacters, position: Vec2f, height: Float, fillColor: Vec4f): Float {

        val m = tex.getOrLoad(resourceManager, char)

        glColor4f(fillColor)
        return renderTextureAA(m, position, height)

    }

    /**
     * @return the width of the rendered character.
     */
    fun renderChar(char: Char, ttf: TruetypeFont, position: Vec2f, height: Float, fillColor: Vec4f): Float {

        val tex = try {
            ttf.getOrLoad(char)
        } catch (e: Throwable) {
            return height/2
        }

        tex.bind()
        val h = tex.getHeight()
        tex.debind()

        val info = ttf.getCharInfo(char)

        val offset = (info.pos0.toVec2f() / TruetypeFont.HEIGHT_TO_LOAD) * height

        //

        glColor4f(fillColor)

        renderTextureAA(tex, position + offset, height * h / TruetypeFont.HEIGHT_TO_LOAD)

        return height * (info.advanceWidth / 1000.0f)

    }

    @Deprecated("Please use Truetype Font version.")
    fun renderCharWithBorder(char: Char, tex: TextureCharacters, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f, thickness: Int=1): Float {

        val m = tex.getOrLoad(resourceManager, char)

        for(p in 1 .. max(thickness, 1)) {

            val dif = pixels(p)

            glColor4f(strokeColor)
            renderTextureAA(m, position + dif.dropY(), height)
            renderTextureAA(m, position - dif.dropY(), height)
            renderTextureAA(m, position + dif.dropX(), height)
            renderTextureAA(m, position - dif.dropX(), height)

        }

        glColor4f(fillColor)
        return renderTextureAA(m, position, height)

    }

    fun renderCharWithBorder(char: Char, ttf: TruetypeFont, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f, thickness: Int=1): Float {

        val tex = try {
            ttf.getOrLoad(char)
        } catch (e: Throwable) {
            return height/2
        }

        tex.bind()
        val h = tex.getHeight()
        tex.debind()

        val info = ttf.getCharInfo(char)

        val offset = (info.pos0.toVec2f() / TruetypeFont.HEIGHT_TO_LOAD) * height

        val realHeight = height * h / TruetypeFont.HEIGHT_TO_LOAD

        val position = position + offset

        for(p in 1 .. max(thickness, 1)) {

            val dif = pixels(p)

            glColor4f(strokeColor)
            renderTextureAA(tex, position.plus(dif.x, 0f), realHeight)
            renderTextureAA(tex, position.plus(-dif.x, 0f), realHeight)
            renderTextureAA(tex, position.plus(0f, dif.y), realHeight)
            renderTextureAA(tex, position.plus(0f, -dif.y), realHeight)

            renderTextureAA(tex, position.plus(dif.x, dif.y), realHeight)
            renderTextureAA(tex, position.plus(-dif.x, -dif.y), realHeight)
            renderTextureAA(tex, position.plus(dif.x, dif.y), realHeight)
            renderTextureAA(tex, position.plus(-dif.x, -dif.y), realHeight)

        }

        glColor4f(fillColor)
        renderTextureAA(tex, position, realHeight)
        return height * (info.advanceWidth / 1000.0f)

    }

    @Deprecated("Please use Truetype Font version.")
    fun getTcOrDynamicallyGen(char: Char, dfont: Font=fontJa) = when(char) {

        in fontManager.getOrLoad(fontEn).textureMap ->fontManager.getOrLoad(fontEn)
        in fontManager.getOrLoad(fontJa).textureMap -> fontManager.getOrLoad(fontJa)
        else -> {
            val tc = fontManager.getOrLoad(dfont)
            tc.getOrLoad(resourceManager, char)
            tc
        }

    }

    @Deprecated("Use Truetype Font version.")
    fun getCharAR(char: Char, tex: TextureCharacters?): Float {

        val c = (tex ?: getTcOrDynamicallyGen(char)).getOrLoad(resourceManager, char)

        c.bind()
        val r = c.getAspectRatio()
        c.debind()

        return r

    }

    @Deprecated("Use Truetype Font version.")
    fun getStringAR(str: String, tex: TextureCharacters?): Float {

        return str.sumOf { getCharAR(it, tex).toDouble() }.toFloat()

    }

    fun getCharAR(char: Char, ttf: TruetypeFont): Float {

        val c = try {
            ttf.getOrLoad(char)
        } catch(e: Throwable) {
            return 0.5f
        }

        c.bind()
        val r = c.getAspectRatio()
        c.debind()

        return r

    }

    fun getStringAR(str: String, ttf: TruetypeFont): Float {

        return str.sumOf { getCharAR(it, ttf).toDouble() }.toFloat()

    }

    /**
     * Renders the text with the specified [str]. [str] must not contain any line-separator. To render multiline texts, use [renderStringMultiLine] instead.
     *
     * @see renderStringMultiLine
     * @see renderStringLineCentered
     */
    fun renderStringLine(str: String, tex: TextureCharacters?, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0.0f): Float {

        var offset = Vec2f.ZERO

        str.forEach {

            val t = tex ?: getTcOrDynamicallyGen(it)

            if(strokeColor != null) {
                offset = offset.plus(renderCharWithBorder(it, t, position.plus(offset), height, fillColor, strokeColor, thickness) + spacing, 0f)
            } else {
                offset = offset.plus(renderChar(it, t, position.plus(offset), height, fillColor) + spacing, 0f)
            }

        }

        if(doDrawOverline) {
            glColor3f(1f, 1f, 1f)
            renderLineStrip(ulongArrayOf(position.plus(0.0f, height).data, position.plus(offset).plus(0f, height).data))
        }

        if(doDrawUnderline) {
            glColor3f(1f, 1f, 1f)
            renderLineStrip(ulongArrayOf(position.plus(0.0f, 0.0f).data, position.plus(offset).plus(0f, 0f).data))
        }

        return offset.x

    }

    fun renderStringLine(str: String, ttf: TruetypeFont, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0.0f): Float {

        var offset = Vec2f.ZERO

        str.forEach {

            val xoffset = if(strokeColor != null) {
                renderCharWithBorder(it, ttf, position.plus(offset), height, fillColor, strokeColor, thickness)
            } else {
                renderChar(it, ttf, position.plus(offset), height, fillColor)
            }

            offset = offset.plus(xoffset + spacing, 0f)

        }

        if(doDrawOverline) {
            glColor3f(1f, 1f, 1f)
            renderLineStrip(ulongArrayOf(position.plus(0.0f, height).data, position.plus(offset).plus(0f, height).data))
        }

        if(doDrawUnderline) {
            glColor3f(1f, 1f, 1f)
            renderLineStrip(ulongArrayOf(position.plus(0.0f, 0.0f).data, position.plus(offset).plus(0f, 0f).data))
        }

        return offset.x

    }

    fun renderStringLine(str: String, font: Font, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0f) =
        renderStringLine(str, fontManager.getOrLoad(font), position, height, fillColor, strokeColor, thickness, spacing)

    @Deprecated("Use Truetype Font version.")
    fun renderStringMultiLine(str: String, font: Font, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0f) {

        str.lines().forEachIndexed { i, line ->
            renderStringLine(line, font, position.minus(0f, (height+spacing)*i), height, fillColor, strokeColor, thickness, spacing)
        }

    }

    fun renderStringMultiLine(str: String, ttf: TruetypeFont, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0f) {

        str.lines().forEachIndexed { i, line ->
            renderStringLine(line, ttf, position.minus(0f, (height+spacing)*i), height, fillColor, strokeColor, thickness, spacing)
        }

    }

    @Deprecated("Use Truetype Font version.")
    fun renderStringLineCentered(str: String, tex: TextureCharacters?, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0f): Float {

        val pos = position.minus(height*getStringAR(str, tex)/2 + spacing*(str.length-1), height/2)

        return renderStringLine(str, tex, pos, height, fillColor, strokeColor, thickness, spacing)

    }

    @Deprecated("Use Truetype Font version.")
    fun renderStringLineCentered(str: String, font: Font, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0f) =
        renderStringLineCentered(str, fontManager.getOrLoad(font), position, height, fillColor, strokeColor, thickness, spacing)

    fun renderStringLineCentered(str: String, ttf: TruetypeFont, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0f): Float {

        val pos = position.minus(height*getStringAR(str, ttf)/2 + spacing*(str.length-1), height/2)

        return renderStringLine(str, ttf, pos, height, fillColor, strokeColor, thickness, spacing)

    }

    @Deprecated("Use Truetype Font version.")
    fun renderStringMultiLineCentered(str: String, font: Font, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0f) {

        str.lines().forEachIndexed { i, line ->
            renderStringLineCentered(line, font, position.minus(0f, (height+spacing)*i), height, fillColor, strokeColor, thickness, spacing)
        }

    }

    fun renderStringMultiLineCentered(str: String, ttf: TruetypeFont, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0f) {

        str.lines().forEachIndexed { i, line ->
            renderStringLineCentered(line, ttf, position.minus(0f, (height+spacing)*i), height, fillColor, strokeColor, thickness, spacing)
        }

    }

    //
    inline fun matrix(block: Matrix.()->Unit) {
        glPushMatrix()
        block(Matrix)
        glPopMatrix()
    }

    object Matrix {

        fun rotate(origin: Vec2f, degree: Float, axes: Vec3f) {

            glTranslatef(origin.x, origin.y, 0.0f)
            glRotatef(degree, axes.x, axes.y, axes.z)
            glTranslatef(-origin.x, -origin.y, 0.0f)

        }

    }

}