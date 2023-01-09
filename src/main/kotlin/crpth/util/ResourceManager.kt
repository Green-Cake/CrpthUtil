package crpth.util

import crpth.util.render.Texture
import crpth.util.sound.NewSoundManager
import javax.sound.sampled.Clip

class ResourceManager(val domain: String) : AutoCloseable {

    companion object {

        val STATIC = ResourceManager("static")

    }

    val textures = mutableSetOf<Texture>()

    val sounds = mutableMapOf<String, Clip>()

    fun loadTexture(path: String): Texture {
        val tex = Texture.load(domain, path)
        textures += tex
        return tex
    }

    fun loadTextureLazy(path: String) = lazy {
        val tex = Texture.load(domain, path)
        textures += tex
        tex
    }

    fun loadSound(name: String, path: String) {

        sounds[name] = NewSoundManager.createClip(domain, path)

    }

    fun freeResources() {

        textures.forEach {
            it.delete()
        }

        sounds.values.forEach {
            it.stop()
            it.flush()
            it.close()
        }

        textures.clear()
        sounds.clear()

    }

    fun play(name: String) = sounds[name]?.start()

    fun playRandom(vararg names: String) = play(names.random())

    fun stop(name: String) {
        sounds[name]?.stop()
        sounds[name]?.flush()
    }

    fun freeSound(name: String) {
        sounds[name]?.close()
        sounds.remove(name)
    }

    override fun close() {
        freeResources()
    }

}