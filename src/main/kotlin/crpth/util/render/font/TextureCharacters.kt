package crpth.util.render.font

import crpth.util.ResourceManager
import crpth.util.render.Texture
import java.awt.Font


data class TextureCharacters(val font: Font, val textureMap: MutableMap<Char, Texture> = mutableMapOf()) {

    private fun dynamicLoad(resourceManager: ResourceManager, char: Char): Texture {

        if(char in textureMap)
            return textureMap[char]!!

        val (_, map) = FontLoader.load(resourceManager, char, font)

        textureMap += map

        return Texture(map[char]?.id ?: 0)

    }

    fun getOrLoad(resourceManager: ResourceManager, char: Char) = textureMap[char] ?: dynamicLoad(resourceManager, char)

}