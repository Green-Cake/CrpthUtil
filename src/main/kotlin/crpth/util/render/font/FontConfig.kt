package crpth.util.render.font

import crpth.util.math.sumOf
import crpth.util.vec.Vec4f

data class FontConfig(val ttf: TruetypeFont,
                      val fillColor: Vec4f, val strokeColor: Vec4f? = null,
                      val isCentered: Boolean = false,
                      val thickness: Int=1, val spacing: Float = 0.0f,
                      val lineSpacing: Float = 0f,
                      val drawOverline: Boolean=false, val drawUnderline: Boolean=false, val lineColor: Vec4f = Vec4f.WHITE) {

    /**
     * @return width per height.
     */
    fun getCharAspectRatio(char: Char): Float {

        return ttf.getAdvanceWidth(char) / 1000f

    }

    /**
     * @return width per height.
     */
    fun getStringAspectRatio(str: String): Float {

        return str.sumOf { getCharAspectRatio(it) }

    }

}