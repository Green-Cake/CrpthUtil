package crpth.util.render

import crpth.util.Window
import crpth.util.render.font.*
import crpth.util.vec.*
import org.lwjgl.opengl.GL11.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalUnsignedTypes::class)
@JvmInline
value class Renderer(val window: Window) {

    companion object {

        inline val SIZE_FULL get() = vec(2.0f, 2.0f)

    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun vertex2f(vec: Vec2f) = glVertex2f(vec.x, vec.y)

    @Suppress("NOTHING_TO_INLINE")
    inline fun glColor4f(vec4f: Vec4f) = glColor4f(vec4f.a, vec4f.b, vec4f.c, vec4f.d)

    /**
     * Invokes glColor4f with each byte value divided by 255, so 0 to 255 in a byte will be mapped into 0f ~ 1f in float.
     * @param color byte values in order of red, green, blue and alpha.
     */
    fun glColor4b(color: Vec4b) {
        glColor4f(color.r.resizeToInt()/255f, color.g.resizeToInt()/255f,  color.b.resizeToInt()/255f, color.a.resizeToInt()/255f)
    }

    /**
     * Calculates the value meaning [p] dot(s) in screen, with no consideration of any matrix contexts.
     * @param p pixel(s)
     * @return a calculated value you can use for [glVertex2f] or [vertex2f]
     */
    fun pixels(p: Int): Vec2f {
        val wsize = window.getWindowSize()
        return SIZE_FULL / wsize.toVec2f() * p.toFloat()
    }

    /**
     * Calculates the value meaning [p] dot(s) in screen, with no consideration of any matrix contexts.
     * @param p pixel(s)
     * @return a calculated value you can use for [glVertex2f] or [vertex2f]
     */
    fun pixels(p: Vec2i): Vec2f {
        val wsize = window.getWindowSize()
        return SIZE_FULL / wsize.toVec2f() * p.toVec2f()
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
    @OptIn(ExperimentalContracts::class)
    inline fun draw(mode: Int, block: ()->Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
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

    fun renderTexture(texture: Texture, position: Vec2f, size: Vec2f, srcStart: Vec2f = Vec2f.ZERO, srcEnd: Vec2f = Vec2f.ONE, initColor: Vec4f?= Vec4f.WHITE) {

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
    fun renderTexture(texture: Texture, position: Vec2f, height: Float, width: Float, srcStart: Vec2f = Vec2f.ZERO, srcEnd: Vec2f = Vec2f.ONE) {

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

    fun renderTextureCentered(texture: Texture, position: Vec2f, size: Vec2f, srcStart: Vec2f = Vec2f.ZERO, srcEnd: Vec2f = Vec2f.ONE)
    = renderTexture(texture, position - size / 2f, size, srcStart, srcEnd)

    /**
     * AA means Auto Aspect
     * @return width value calculated and used for rendering.
     */
    fun renderTextureAA(texture: Texture, position: Vec2f, height: Float, srcStart: Vec2f = Vec2f.ZERO, srcEnd: Vec2f = Vec2f.ONE): Float {

        val aspect: Float

        texture.use {

            aspect = texture.getAspectRatio()

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

        }

        return height * aspect

    }

    /**
     * @return the width of the rendered character.
     */
    fun renderChar(char: Char, ttf: TruetypeFont, position: Vec2f, height: Float, fillColor: Vec4f): Float {

        val tex = try {
            ttf.getOrLoad(char)
        } catch (e: Throwable) {
            return height*(ttf.getAspectRatioForBlank(char) ?: 0f)
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
            return height*(ttf.getAspectRatioForBlank(char) ?: 0f)
        }

        tex.bind()
        val h = tex.getHeight()
        tex.debind()

        val info = ttf.getCharInfo(char)

        val offset = (info.pos0.toVec2f() / TruetypeFont.HEIGHT_TO_LOAD) * height

        //

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

    @Deprecated("Use Renderer#renderString instead to make calling a function more simply.", ReplaceWith("renderString(str, ttf, position, height, fillColor, strokeColor, false, thickness, spacing)"))
    fun renderStringSingleLine(
        str: String, position: Vec2f, height: Float, config: FontConfig
    ): Float {

        var offset = Vec2f.ZERO

        str.forEach {

            val xoffset = if(config.strokeColor != null) {
                renderCharWithBorder(it, config.ttf, position.plus(offset), height, config.fillColor, config.strokeColor, config.thickness)
            } else {
                renderChar(it, config.ttf, position.plus(offset), height, config.fillColor)
            }

            offset = offset.plus(xoffset + config.spacing, 0f)

        }

        if(config.drawOverline) {
            glColor4f(config.lineColor)
            renderLineStrip(position.plus(0.0f, height).data, position.plus(offset).plus(0f, height).data)
        }

        if(config.drawUnderline) {
            glColor4f(config.lineColor)
            renderLineStrip(position.plus(0.0f, 0.0f).data, position.plus(offset).plus(0f, 0f).data)
        }

        return offset.x

    }

    @Deprecated("Use Renderer#renderString instead to make calling a function more simply.", ReplaceWith("renderString(str, ttf, position, height, fillColor, strokeColor, false, thickness, spacing)"))
    fun renderStringMultiLine(str: String, position: Vec2f, height: Float, config: FontConfig, firstX: Float=0f): Vec2f {

        var ret = position

        str.lines().forEachIndexed { i, line ->
            ret = position.minus(0f, (height+config.spacing)*i)

            if(i == 0)
                ret = ret.plus(x = firstX)

            val w = renderStringSingleLine(line, ret, height, config)
            ret = ret.plus(x = w)
        }

        return ret

    }

    @Deprecated("Use Renderer#renderString instead to make calling a function more simply.", ReplaceWith("renderString(str, ttf, position, height, fillColor, strokeColor, true, thickness, spacing)"))
    fun renderStringSingleLineCentered(str: String, position: Vec2f, height: Float, config: FontConfig): Float {

        val pos = position.minus(height*config.getStringAspectRatio(str)/2 + config.spacing*(str.length-1), height/2)

        return renderStringSingleLine(str, pos, height, config)

    }

    @Deprecated("Use Renderer#renderString instead to make calling a function more simply.", ReplaceWith("renderString(str, ttf, position, height, fillColor, strokeColor, true, thickness, spacing)"))
    fun renderStringMultiLineCentered(str: String, position: Vec2f, height: Float, config: FontConfig) {

        str.lines().forEachIndexed { i, line ->
            renderStringSingleLineCentered(line, position.minus(0f, (height+config.lineSpacing)*i), height, config)
        }

    }

    /**
     * Renders a specified text of [str].
     * @param str Text string to render. Able to contain line separator.
     * @param position A position in the screen. May be modified if [FontConfig.isCentered] is true.
     * @param height Height of text to render. 1.0f corresponds to a half height of the screen provided no matrix is set.
     * @param config Some parameters in detail.
     * @see FontConfig
     */
    fun renderString(str: String, position: Vec2f, height: Float, config: FontConfig) =
        if(config.isCentered)
            renderStringMultiLineCentered(str, position, height, config)
        else
            renderStringMultiLine(str, position, height, config)

    @OptIn(ExperimentalStdlibApi::class)
    fun renderString(width: Float, lastUnit: TextUnit, charCount: Int = -1) {

        val stack = lastUnit.getStack()

        val first = stack.peek() as TextUnitHead

        val firstX = first.position.x

        val maxX = firstX + width

        var position = first.position

        var config: FontConfig

        var count = if(charCount >= 0) charCount else Int.MAX_VALUE

        while(true) {

            val current = try {
                stack.pop() as TextUnit
            } catch (e: Throwable) {
                return
            }

            config = current.config

            if(current is Br) {

                position = position.minus(y = current.height + config.lineSpacing).copy(x = firstX)

                continue
            }

            var w = 0f
            var appended = 1

            val builder = StringBuilder(current.str)

            var virtualX = position.x

            for(i in 0 ..< min(count, current.str.lastIndex)) {

                w += current.height * config.getCharAspectRatio(current.str[i])
                if(virtualX + w > maxX) {
                    builder.insert(i+appended++, '\n')
                    w = 0f
                    virtualX = firstX
                }

            }

            val lines = builder.lines()

            lines.forEachIndexed {  index, s ->

                position = renderStringMultiLine(s.take(count), position.copy(x = first.position.x), current.height, config, firstX = position.x - firstX)
                count -= s.length

                if(count <= 0)
                    return

                if(index != lines.lastIndex) {
                    position = position.minus(y = current.height + config.lineSpacing).copy(x = firstX)
                }

            }

        }

    }

    //
    @OptIn(ExperimentalContracts::class)
    inline fun matrix(block: Matrix.()->Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
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