package crpth.util.sound

import crpth.util.vec.Vec2i
import crpth.util.vec.Vec3f
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11
import org.lwjgl.system.MemoryStack

@JvmInline
value class Sound(val data: Vec2i) {

    companion object {

        inline val NULL get() = Sound(Vec2i(0, 0))

    }

    val buffer get() = data.x

    val source get() = data.y

    var doLoop: Boolean
        get() = AL10.alGetSourcei(source, AL10.AL_LOOPING) == AL10.AL_TRUE
        set(value) { AL10.alSourcei(source, AL10.AL_LOOPING, if (value) AL10.AL_TRUE else AL10.AL_FALSE) }

    var volume: Float
        get() = AL10.alGetSourcef(source, AL10.AL_GAIN)
        set(value) { AL10.alSourcef(source, AL10.AL_GAIN, value) }

    var pitch: Float
        get() = AL10.alGetSourcef(source, AL10.AL_PITCH)
        set(value) { AL10.alSourcef(source, AL10.AL_PITCH, value) }

    var rolloffFactor: Int
        get() = AL10.alGetSourcei(source, AL10.AL_ROLLOFF_FACTOR)
        set(value) { AL10.alSourcei(source, AL10.AL_ROLLOFF_FACTOR, value) }

    var velocity: Vec3f
        get() {
            MemoryStack.stackPush().use {
                val x = it.mallocFloat(1)
                val y = it.mallocFloat(1)
                val z = it.mallocFloat(1)
                AL10.alGetSource3f(source, AL10.AL_VELOCITY, x, y, z)
                return Vec3f(x.get(), y.get(), z.get())
            }
        }
        set(value) { AL10.alSource3f(source, AL10.AL_VELOCITY, value.x, value.y, value.z) }

    var position: Vec3f
        get() {
            MemoryStack.stackPush().use {
                val x = it.mallocFloat(1)
                val y = it.mallocFloat(1)
                val z = it.mallocFloat(1)
                AL10.alGetSource3f(source, AL10.AL_POSITION, x, y, z)
                return Vec3f(x.get(), y.get(), z.get())
            }
        }
        set(value) { AL10.alSource3f(source, AL10.AL_POSITION, value.x, value.y, value.z) }

    var direction: Vec3f
        get() {
            MemoryStack.stackPush().use {
                val x = it.mallocFloat(1)
                val y = it.mallocFloat(1)
                val z = it.mallocFloat(1)
                AL10.alGetSource3f(source, AL10.AL_DIRECTION, x, y, z)
                return Vec3f(x.get(), y.get(), z.get())
            }
        }
        set(value) { AL10.alSource3f(source, AL10.AL_DIRECTION, value.x, value.y, value.z) }

    var refDistance: Vec3f
        get() {
            MemoryStack.stackPush().use {
                val x = it.mallocFloat(1)
                val y = it.mallocFloat(1)
                val z = it.mallocFloat(1)
                AL10.alGetSource3f(source, AL10.AL_REFERENCE_DISTANCE, x, y, z)
                return Vec3f(x.get(), y.get(), z.get())
            }
        }
        set(value) { AL10.alSource3f(source, AL10.AL_REFERENCE_DISTANCE, value.x, value.y, value.z) }

    var maxDistance: Vec3f
        get() {
            MemoryStack.stackPush().use {
                val x = it.mallocFloat(1)
                val y = it.mallocFloat(1)
                val z = it.mallocFloat(1)
                AL10.alGetSource3f(source, AL10.AL_MAX_DISTANCE, x, y, z)
                return Vec3f(x.get(), y.get(), z.get())
            }
        }
        set(value) { AL10.alSource3f(source, AL10.AL_MAX_DISTANCE, value.x, value.y, value.z) }

    val state get() = State.fromALState(AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE))

    fun delete() {

        AL11.alDeleteBuffers(buffer)
        AL11.alDeleteSources(source)

    }

    operator fun component1() = buffer

    operator fun component2() = source

    fun play() {
        AL10.alSourcePlay(source)
    }

    fun pause() {
        AL10.alSourcePause(source)
    }

    fun rewind() {
        AL10.alSourceRewind(source)
    }

    fun stop() {
        AL10.alSourceStop(source)
    }

    enum class State {

        INITIAL,
        PLAYING,
        PAUSED,
        STOPPED
        ;

        companion object {

            fun fromALState(value: Int) = when(value) {
                AL10.AL_INITIAL -> INITIAL
                AL10.AL_PLAYING -> PLAYING
                AL10.AL_PAUSED -> PAUSED
                AL10.AL_STOPPED -> STOPPED
                else -> throw Exception()
            }

        }

    }

}