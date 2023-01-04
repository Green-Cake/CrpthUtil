package crpth.util.render.font

import crpth.util.ResourceManager
import java.awt.Font

class FontManager(val resourceManager: ResourceManager, fontsizeToLoad: Int) {

    var isInitialized = false
        private set

    private val textures = mutableMapOf<Font, TextureCharacters>()

    val fontMonospaced = Font(Font.MONOSPACED, Font.PLAIN, fontsizeToLoad)

    val fontYumincho = Font("游明朝体", Font.PLAIN, fontsizeToLoad)

    private fun s(c: Char) = c..c

    fun init() {

        if(isInitialized)
            return

        isInitialized = true

        textures[fontMonospaced] = TextureCharacters(fontMonospaced)

        textures[fontYumincho] = TextureCharacters(fontYumincho)

        load(fontMonospaced, '!'..'~', '¡'..'¿', s(' '))

        load(fontYumincho, '!'..'~', '¡'..'¿', s(' '), 'ぁ'..'ゖ', 'ァ'..'ヿ')

    }

    fun load(font: Font, chars: CharArray) {
        textures[font] = generateTextureCharacters(font, chars)
    }

    fun load(font: Font, vararg chars: CharRange) {
        textures[font] = generateTextureCharacters(font, chars)
    }

    fun generateTextureCharacters(font: Font, characters: CharArray): TextureCharacters {

        return FontLoader.load(resourceManager, characters, font)

    }

    fun generateTextureCharacters(font: Font, chars: Array<out CharRange>): TextureCharacters {

        return FontLoader.load(resourceManager, chars, font)

    }

    fun getOrLoad(font: Font) = textures[font] ?: run {
        load(font)
        textures[font]!!
    }

}