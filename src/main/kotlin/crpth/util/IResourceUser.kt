package crpth.util

import crpth.util.sound.SoundManager

interface IResourceUser {

    val resourceManager: ResourceManager

    val soundManager: SoundManager

    val resourceUserId: String get() = this::class.simpleName!!

    fun getActualName(name: String): String = "$resourceUserId:$name"

    fun loadSound(name: String, path: String, doLoop: Boolean) {

        soundManager.load(this, getActualName(name), path, doLoop)

    }

    fun setVolume(name: String, volume: Double) {
        soundManager.setVolume(getActualName(name), volume)
    }

    fun freeResources() {

        soundManager.removeUserResources(this)

    }

    fun play(name: String, volume: Double=1.0) = soundManager.play(getActualName(name), volume)

    fun playRandom(vararg names: String, volume: Double=1.0) = soundManager.playRandom(this, *names, volume=volume)

    fun stop(name: String) = soundManager.stop(this, name)

}