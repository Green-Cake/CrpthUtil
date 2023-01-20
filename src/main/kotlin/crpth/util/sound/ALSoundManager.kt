package crpth.util.sound

import crpth.util.vec.Vec2i
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10.*
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10
import org.lwjgl.stb.STBVorbis
import org.lwjgl.stb.STBVorbisInfo
import org.lwjgl.system.MemoryUtil.NULL
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer
import javax.sound.sampled.UnsupportedAudioFileException

internal object ALSoundManager : Closeable {

    var canPlaySound: Boolean = false
    private var device: Long = NULL
    private var context: Long = NULL

    fun init() {

        device = ALC10.alcOpenDevice(null as ByteBuffer?)

        canPlaySound = device != NULL

        val caps = ALC.createCapabilities(device)

        context = ALC10.alcCreateContext(device, null as IntBuffer?)
        ALC10.alcMakeContextCurrent(context)
        AL.createCapabilities(caps)

    }

    fun loadPcm(pcm: ShortBuffer, sampleRate: Int): Sound {

        val buffer = alGenBuffers()
        val source = alGenSources()

        alBufferData(buffer, AL_FORMAT_STEREO16, pcm, sampleRate)

        alSourcei(source, AL_BUFFER, buffer)

        return Sound(Vec2i(buffer, source))

    }

    fun loadPcmMono(pcm: ShortBuffer, sampleRate: Int): Sound {

        val buffer = alGenBuffers()
        val source = alGenSources()

        alBufferData(buffer, AL_FORMAT_MONO16, pcm, sampleRate)

        alSourcei(source, AL_BUFFER, buffer)

        return Sound(Vec2i(buffer, source))

    }

    fun loadOgg(data: ByteBuffer): Sound {

        STBVorbisInfo.malloc().use {  info ->

            val error = BufferUtils.createIntBuffer(1)
            val decoder = STBVorbis.stb_vorbis_open_memory(data, error, null)

            if(decoder == NULL)
                throw IOException("Can't open OGG Vorbis! error: ${error[0]}")

            STBVorbis.stb_vorbis_get_info(decoder, info)

            val channels = info.channels()
            val length = STBVorbis.stb_vorbis_stream_length_in_samples(decoder)

            val pcm = BufferUtils.createShortBuffer(length * 2)

            STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm)
            STBVorbis.stb_vorbis_close(decoder)

            return loadPcm(pcm, info.sample_rate())

        }

    }

    fun loadOggMono(data: ByteBuffer, useLeft: Boolean): Sound {

        STBVorbisInfo.malloc().use {  info ->

            val error = BufferUtils.createIntBuffer(1)
            val decoder = STBVorbis.stb_vorbis_open_memory(data, error, null)

            if(decoder == NULL)
                throw IOException("Can't open OGG Vorbis! error: ${error[0]}")

            STBVorbis.stb_vorbis_get_info(decoder, info)

            val channels = info.channels()
            val length = STBVorbis.stb_vorbis_stream_length_in_samples(decoder)

            val pcm = BufferUtils.createShortBuffer(length * 2)

            STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm)
            STBVorbis.stb_vorbis_close(decoder)

            val pcmMono = BufferUtils.createShortBuffer(length)
            if(useLeft) for(i in 0 until length) {
                pcmMono.put(pcm[i*2])
            } else for(i in 0 until length) {
                pcmMono.put(pcm[i*2 + 1])
            }
            pcmMono.flip()

            return loadPcmMono(pcmMono, info.sample_rate())

        }

    }

    fun loadWav(info: WavInfo): Sound {

        val fmt = when(info.channel to info.bitPerSample) {
            1 to 8 -> AL_FORMAT_MONO8
            1 to 16 -> AL_FORMAT_MONO16
            1 to 24 -> AL_FORMAT_MONO16 // will be converted
            2 to 8 -> AL_FORMAT_STEREO8
            2 to 16 -> AL_FORMAT_STEREO16
            2 to 24 -> AL_FORMAT_STEREO16 // will be converted
            else -> throw UnsupportedAudioFileException("channel: ${info.channel}, ${info.bitPerSample} bit/sample is unsupported!")
        }

        val bufData = if(info.bitPerSample == 24) BufferUtils.createByteBuffer(info.pcm.size / 3 * 2).apply {
            info.getPcm16From24().forEach {
                putShort(it)
            }
            flip()
        } else BufferUtils.createByteBuffer(info.pcm.size).apply {
            put(info.pcm)
            flip()
        }

        val buffer = alGenBuffers()
        val source = alGenSources()

        alBufferData(buffer, fmt, bufData, info.sampleRate.toInt())
        alSourcei(source, AL_BUFFER, buffer)

        return Sound(Vec2i(buffer, source))

    }

    fun loadWav(stream: InputStream) = loadWav(WavParser.parse(stream))

    fun loadWavMono(stream: InputStream, useLeft: Boolean = false) =
        loadWav(WavParser.parse(stream).convertToMono(useLeft))

    override fun close() {

        device = ALC10.alcGetContextsDevice(context)
        ALC10.alcMakeContextCurrent(0L)
        ALC10.alcDestroyContext(context)
        ALC10.alcCloseDevice(device)

    }

}