package crpth.util.render.font

import crpth.util.ptr.IntPtr
import crpth.util.render.Texture
import crpth.util.vec.Vec2i
import crpth.util.vec.vec
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype.*
import org.lwjgl.system.MemoryStack
import java.nio.file.Files
import java.nio.file.Path

class TruetypeFont(path: Path) {

    companion object {

        const val HEIGHT_TO_LOAD = 128f

    }

    private val textures = mutableMapOf<Int, Texture>()

    private val charInfoMap = mutableMapOf<Int, CharData>()

    private val ttf = Files.readAllBytes(path)

    private val ttfBuf = BufferUtils.createByteBuffer(ttf.size).put(ttf).flip()

    val fontInfo = STBTTFontinfo.create().apply {
        stbtt_InitFont(this, ttfBuf)
    }

    val scale = stbtt_ScaleForPixelHeight(fontInfo, HEIGHT_TO_LOAD)

    val ascent: Int
    val descent: Int

    init {
        val _ascent = IntPtr.alloc()
        val _descent = IntPtr.alloc()
        stbtt_GetFontVMetrics(fontInfo, _ascent.ptr, _descent.ptr, null)
        ascent = _ascent.value
        descent = _descent.value
    }

    val baseline = (ascent * scale).toInt()

    fun getTexture(charInUtf8: Char) = textures[charInUtf8.code]

    fun loadChar(charInUtf8: Char) = loadChar(charInUtf8.code)

    fun getOrLoad(charInUtf8: Char) = getTexture(charInUtf8) ?: loadChar(charInUtf8)

    fun getCharInfo(charInUtf8: Char): CharData = charInfoMap[charInUtf8.code] ?: run {
        loadChar(charInUtf8)
        charInfoMap[charInUtf8.code]!!
    }

    fun getAdvanceWidth(charInUtf8: Char) = charInfoMap[charInUtf8.code]?.advanceWidth ?: MemoryStack.stackPush().use {

        val pAdvanceWidth = it.mallocInt(1)
        stbtt_GetCodepointHMetrics(fontInfo, charInUtf8.code, pAdvanceWidth, null)
        pAdvanceWidth.get()

    }

    fun loadChar(codepoint: Int): Texture {

        val x0 = IntPtr.alloc()
        val y0 = IntPtr.alloc()
        val x1 = IntPtr.alloc()
        val y1 = IntPtr.alloc()
        stbtt_GetCodepointBitmapBox(fontInfo, codepoint, scale, scale, x0.ptr, y0.ptr, x1.ptr, y1.ptr)
        //

        val width = IntPtr.alloc()
        val height = IntPtr.alloc()
        val xoff = IntPtr.alloc()
        val yoff = IntPtr.alloc()

        val advanceWidth: Int
        val leftSideBearing: Int

        MemoryStack.stackPush().use {

            val pAdvanceWidth = it.mallocInt(1)
            val pLeftSideBearing = it.mallocInt(1)
            stbtt_GetCodepointHMetrics(fontInfo, codepoint, pAdvanceWidth, pLeftSideBearing)
            advanceWidth = pAdvanceWidth.get()
            leftSideBearing = pLeftSideBearing.get()

        }

        val image = stbtt_GetCodepointBitmap(fontInfo, scale, scale, codepoint, width.ptr, height.ptr, xoff.ptr, yoff.ptr)
        val imageSize = Vec2i(width.value, height.value)

        val tex = Texture.loadGrayscale(image ?: throw Exception("Failed to load char (0x${codepoint.toString(16)}) size: $imageSize"), imageSize)

        tex.use {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        }

        val charData = CharData(vec(x0.value, HEIGHT_TO_LOAD.toInt() - (baseline + y1.value)), vec(x1.value, HEIGHT_TO_LOAD.toInt() - (baseline + y0.value)), advanceWidth, leftSideBearing)

        textures[codepoint] = tex
        charInfoMap[codepoint] = charData

        return tex

    }

    data class CharData(val pos0: Vec2i, val pos1: Vec2i, val advanceWidth: Int, val leftSideBearing: Int) {

    }

}