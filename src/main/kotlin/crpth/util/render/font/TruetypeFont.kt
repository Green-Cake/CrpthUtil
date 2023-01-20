package crpth.util.render.font

import crpth.util.render.Texture
import crpth.util.vec.Vec2i
import crpth.util.vec.vec
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype.*
import org.lwjgl.system.MemoryStack
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path

class TruetypeFont(path: Path) : Closeable {

    companion object {

        const val HEIGHT_TO_LOAD = 128f

    }

    private val textures = mutableMapOf<Int, Texture>()

    private val charInfoMap = mutableMapOf<Int, CharData>()

    private val bin = Files.readAllBytes(path)

    private val ttfBuf = BufferUtils.createByteBuffer(bin.size).put(bin).flip()

    private val blanks = mutableMapOf<Char, Float>()

    val fontInfo = STBTTFontinfo.create().apply {
        stbtt_InitFont(this, ttfBuf)
    }

    val scale = stbtt_ScaleForPixelHeight(fontInfo, HEIGHT_TO_LOAD)

    val ascent: Int
    val descent: Int

    init {

        MemoryStack.stackPush().use {
            val pAscent = it.mallocInt(1)
            val pDescent = it.mallocInt(1)
            stbtt_GetFontVMetrics(fontInfo, pAscent, pDescent, null)
            ascent = pAscent.get()
            descent = pDescent.get()
        }

        blanks[' '] = 0.5f
        blanks['　'] = 1f

    }

    val baseline = (ascent * scale).toInt()

    /**
     * @return An aspect ratio or null if [char] is not registered.
     */
    fun getAspectRatioForBlank(char: Char): Float? = blanks[char]

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

        val x0: Int
        val y0: Int
        val x1: Int
        val y1: Int

        val advanceWidth: Int
        val leftSideBearing: Int

        val width: Int
        val height: Int

        val image: ByteBuffer?

        MemoryStack.stackPush().use {

            val px0 = it.mallocInt(1)
            val py0 = it.mallocInt(1)
            val px1 = it.mallocInt(1)
            val py1 = it.mallocInt(1)

            stbtt_GetCodepointBitmapBox(fontInfo, codepoint, scale, scale, px0, py0, px1, py1)

            x0 = px0.get()
            y0 = py0.get()
            x1 = px1.get()
            y1 = py1.get()

            val pAdvanceWidth = it.mallocInt(1)
            val pLeftSideBearing = it.mallocInt(1)
            stbtt_GetCodepointHMetrics(fontInfo, codepoint, pAdvanceWidth, pLeftSideBearing)
            advanceWidth = pAdvanceWidth.get()
            leftSideBearing = pLeftSideBearing.get()

            val pWidth = it.mallocInt(1)
            val pHeight = it.mallocInt(1)

            image = stbtt_GetCodepointBitmap(fontInfo, scale, scale, codepoint, pWidth, pHeight, null, null)

            width = pWidth.get()
            height = pHeight.get()

        }


        val imageSize = Vec2i(width, height)

        val tex = Texture.loadGrayscale(image ?: throw Exception("Failed to load char (0x${codepoint.toString(16)}) size: $imageSize"), imageSize)

        tex.use {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        }

        val charData = CharData(vec(x0, HEIGHT_TO_LOAD.toInt() - (baseline + y1)), vec(x1, HEIGHT_TO_LOAD.toInt() - (baseline + y0)), advanceWidth, leftSideBearing)

        textures[codepoint] = tex
        charInfoMap[codepoint] = charData

        return tex

    }

    override fun close() {

        textures.values.forEach {
            it.delete()
        }

        textures.clear()
        charInfoMap.clear()

    }

    data class CharData(val pos0: Vec2i, val pos1: Vec2i, val advanceWidth: Int, val leftSideBearing: Int)

}