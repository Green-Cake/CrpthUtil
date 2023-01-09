package crpth.util.sound.old

import javafx.scene.media.AudioClip

@Deprecated("Use NewSoundManager instead.", ReplaceWith("NewSoundManager"))
class SoundManager(val domain: String) {

    val oggManager = OggManager(domain)

    val audios = mutableMapOf<Int, AudioClip>()

    fun init() {
        oggManager.init()
    }

    fun finish() {
        oggManager.finish()
        audios.values.forEach {
            it.stop()
        }
        audios.clear()
    }

    fun nextUniqueID() = audios.keys.max()+1

    operator fun contains(id: Int) = id in audios || id in oggManager

    fun load(id: Int, path: String, doLoop: Boolean): Int {

        if(path.endsWith(".ogg")) {

            oggManager.loadOgg(id, path, doLoop)

            return id
        }

        audios[id] = AudioClip(ClassLoader.getSystemResource("assets/$domain/sounds/$path").toString()).apply {
            cycleCount = if(doLoop) AudioClip.INDEFINITE else 1
        }

        return id

    }

    fun free(id: Int) {

        audios.remove(id)
        oggManager.free(id)

    }

    fun play(id: Int, volume: Double=1.0) {

        if(id in oggManager)
            oggManager.play(id, volume.toFloat())
        else
            audios[id]?.play(volume)

    }

    fun playRandom(vararg ids: Int, volume: Double=1.0) = play(ids.random(), volume)

    fun stop(id: Int) {
        audios[id]?.stop()
        oggManager.stop(id)
    }

    fun setVolume(id: Int, volume: Double) {

        audios[id]?.volume = volume
        oggManager.setVolume(id, volume.toFloat())

    }

}