package crpth.util

import crpth.util.render.Texture
import crpth.util.sound.ALSoundManager
import crpth.util.sound.Sound
import javax.sound.sampled.UnsupportedAudioFileException

class ResourceManager(val domain: String) : AutoCloseable {

    companion object {

        val STATIC = ResourceManager("static")

    }

    private val sounds = mutableMapOf<Int, Sound>()

    private val textures = mutableSetOf<Texture>()

    //audio start

    //only unique in this ResourceManager instance!
    private fun uniqueSoundID() = (sounds.keys.maxOrNull() ?: 0) + 1

    fun init() {
        ALSoundManager.init()
    }

    fun loadOgg(path: String, id: Int=uniqueSoundID()): Int {
        sounds[id] = ALSoundManager.loadOgg(ResourceAccessor.loadSoundFile(domain, path))
        return id
    }

    fun loadOggMono(path: String, id: Int=uniqueSoundID(), useLeft: Boolean = false): Int {
        sounds[id] = ALSoundManager.loadOggMono(ResourceAccessor.loadSoundFile(domain, path), useLeft)
        return id
    }

    fun loadWav(path: String, id: Int=uniqueSoundID()): Int {
        sounds[id] = ALSoundManager.loadWav(ResourceAccessor.getAudioInputStream(domain, path)!!)
        return id
    }

    fun loadWavMono(path: String, id: Int=uniqueSoundID(), useLeft: Boolean = false): Int {
        sounds[id] = ALSoundManager.loadWavMono(ResourceAccessor.getAudioInputStream(domain, path)!!, useLeft)
        return id
    }

    fun load(path: String, id: Int=uniqueSoundID()): Int = when(path.split('.').last().lowercase()) {
        "ogg" -> loadOgg(path, id)
        "wav" -> loadWav(path, id)
        else -> throw UnsupportedAudioFileException()
    }

    /**
     * Forces to load sound with only 1 channel.
     */
    fun loadMono(path: String, id: Int=uniqueSoundID(), useLeft: Boolean=false): Int = when(path.split('.').last().lowercase()) {
        "ogg" -> loadOggMono(path, id, useLeft)
        "wav" -> loadWavMono(path, id, useLeft)
        else -> throw UnsupportedAudioFileException()
    }

    fun getSound(id: Int) = sounds[id]

    fun play(id: Int) = sounds[id]?.play()

    fun pause(id: Int) = sounds[id]?.pause()

    fun rewind(id: Int) = sounds[id]?.rewind()

    fun stop(id: Int) = sounds[id]?.stop()

    //audio end

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

    private fun freeResources() {

        textures.forEach {
            it.delete()
        }

        textures.clear()

        sounds.values.forEach {
            it.delete()
        }

        sounds.clear()

    }

    override fun close() {
        freeResources()
        ALSoundManager.close()
    }

}