package crpth.util.render

import crpth.util.Window
import crpth.util.math.sumOf
import crpth.util.render.font.TruetypeFont
import crpth.util.vec.*
import org.lwjgl.opengl.GL11.*
import kotlin.math.max

@OptIn(ExperimentalUnsignedTypes::class)
class Renderer(val windowGetter: ()->Window) {

    object Constants {

        inline val SIZE_FULL get() = vec(2.0f, 2.0f)

    }

    companion object {

        @Suppress("NOTHING_TO_INLINE")
        inline fun vertex2f(vec: Vec2f) = glVertex2f(vec.x, vec.y)

        @Suppress("NOTHING_TO_INLINE")
        inline fun glColor4f(vec4f: Vec4f) = glColor4f(vec4f.a, vec4f.b, vec4f.c, vec4f.d)

    }

    /**
     * If true, when rendering any text, also draws overline automatically.
     */
    var doDrawOverline = false

    /**
     * If true, when rendering any text, also draws underline automatically.
     */
    var doDrawUnderline = false

    /**
     * Invokes glColor4f with each byte value divided by 255, so 0 to 255 in a byte will be mapped into 0f ~ 1f in float.
     * @param color byte values in order of red, green, blue and alpha.
     */
    fun color4b(color: Vec4b) {
        glColor4f(color.r.resizeToInt()/255f, color.g.resizeToInt()/255f,  color.b.resizeToInt()/255f, color.a.resizeToInt()/255f)
    }

    /**
     * Calculates the value meaning [p] dot(s) in screen, with no consideration of any matrix contexts.
     * @param p pixel(s)
     * @return a calculated value you can use for [glVertex2f] or [vertex2f]
     */
    fun pixels(p: Int): Vec2f {
        val wsize = windowGetter().getWindowSize()
        return Constants.SIZE_FULL / wsize.toVec2f() * p.toFloat()
    }

    /**
     * Calculates the value meaning [p] dot(s) in screen, with no consideration of any matrix contexts.
     * @param p pixel(s)
     * @return a calculated value you can use for [glVertex2f] or [vertex2f]
     */
    fun pixels(p: Vec2i): Vec2f {
        val wsize = windowGetter().getWindowSize()
        return Constants.SIZE_FULL / wsize.toVec2f() * p.toVec2f()
    }

    /**
     * a simple syntax sugar of
     * ```kotlin
     * glBegin(mode)
     * block()
     * glEnd()
     * ```
     * @param mode mode value
     * @param block call vertex or something to draw.
     * @see org.lwjgl.opengl.GL11
     */
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
        glClearColor(clearColor.a, clearColor.b, clearColor.c, clearColor.d)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun renderLineStrip(vararg positions: ULong) {

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

    @Deprecated("Use Vec2f instead!", ReplaceWith("renderTexture(texture, position, vec(width, height), srcStart, srcEnd)"))
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

    fun renderTextureCentered(texture: ITexture, position: Vec2f, size: Vec2f, srcStart: Vec2f = Vec2f.ZERO, srcEnd: Vec2f = Vec2f.ONE)
    = renderTexture(texture, position - size / 2f, size, srcStart, srcEnd)

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

    /**
     * @return the width of the rendered character.
     */
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

    /**
     * @return width per height.
     */
    fun getCharAspectRatio(char: Char, ttf: TruetypeFont): Float {

        return ttf.getAdvanceWidth(char) / 1000f

    }

    /**
     * @return width per height.
     */
    fun getStringAspectRatio(str: String, ttf: TruetypeFont): Float {

        return str.sumOf { getCharAspectRatio(it, ttf) }

    }

    @Deprecated("Use Renderer#renderString instead to make calling a function more simply.", ReplaceWith("renderString(str, ttf, position, height, fillColor, strokeColor, false, thickness, spacing)"))
    fun renderStringSingleLine(str: String, ttf: TruetypeFont, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0.0f): Float {

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
            renderLineStrip(position.plus(0.0f, height).data, position.plus(offset).plus(0f, height).data)
        }

        if(doDrawUnderline) {
            glColor3f(1f, 1f, 1f)
            renderLineStrip(position.plus(0.0f, 0.0f).data, position.plus(offset).plus(0f, 0f).data)
        }

        return offset.x

    }

    @Deprecated("Use Renderer#renderString instead to make calling a function more simply.", ReplaceWith("renderString(str, ttf, position, height, fillColor, strokeColor, false, thickness, spacing)"))
    fun renderStringMultiLine(str: String, ttf: TruetypeFont, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0f) {

        str.lines().forEachIndexed { i, line ->
            renderStringSingleLine(line, ttf, position.minus(0f, (height+spacing)*i), height, fillColor, strokeColor, thickness, spacing)
        }

    }

    @Deprecated("Use Renderer#renderString instead to make calling a function more simply.", ReplaceWith("renderString(str, ttf, position, height, fillColor, strokeColor, true, thickness, spacing)"))
    fun renderStringSingleLineCentered(str: String, ttf: TruetypeFont, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0f): Float {

        val pos = position.minus(height*getStringAspectRatio(str, ttf)/2 + spacing*(str.length-1), height/2)

        return renderStringSingleLine(str, ttf, pos, height, fillColor, strokeColor, thickness, spacing)

    }

    @Deprecated("Use Renderer#renderString instead to make calling a function more simply.", ReplaceWith("renderString(str, ttf, position, height, fillColor, strokeColor, true, thickness, spacing)"))
    fun renderStringMultiLineCentered(str: String, ttf: TruetypeFont, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, thickness: Int=1, spacing: Float = 0f) {

        str.lines().forEachIndexed { i, line ->
            renderStringSingleLineCentered(line, ttf, position.minus(0f, (height+spacing)*i), height, fillColor, strokeColor, thickness, spacing)
        }

    }

    fun renderString(str: String, ttf: TruetypeFont, position: Vec2f, height: Float, fillColor: Vec4f, strokeColor: Vec4f? = null, centered: Boolean=false, thickness: Int=1, spacing: Float = 0f) =
        if(centered)
            renderStringMultiLineCentered(str, ttf, position, height, fillColor, strokeColor, thickness, spacing)
        else
            renderStringMultiLine(str, ttf, position, height, fillColor, strokeColor, thickness, spacing)

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