package crpth.util.sound

import java.io.InputStream
import javax.sound.sampled.UnsupportedAudioFileException

object WavParser {

    val NAME_JUNK = intArrayOf(0x4A, 0x55, 0x4E, 0x4B)

    val NAME_FMT = intArrayOf(0x66, 0x6D, 0x74, 0x20)

    val NAME_DATA = intArrayOf(0x64, 0x61, 0x74, 0x61)

    /**
     * Little endian!!!
     */
    private fun InputStream.read16bit() = read() + (read() shl 8)

    /**
     * Little endian!!!
     */
    private fun InputStream.read32bit() = read().toLong() +
            (read().toLong() shl 8) +
            (read().toLong() shl 16) +
            (read().toLong() shl 24)

    fun parse(stream: InputStream): WavInfo {

        check(stream.read() == 0x52)
        check(stream.read() == 0x49)
        check(stream.read() == 0x46)
        check(stream.read() == 0x46)

        val lengthChunk = stream.read32bit()

        check(stream.read() == 0x57)
        check(stream.read() == 0x41)
        check(stream.read() == 0x56)
        check(stream.read() == 0x45)

        var chunkName = IntArray(4) { stream.read() }

        //skip junk chunk.
        if(chunkName.contentEquals(NAME_JUNK)) {
            val l = stream.read32bit()
            for(i in 0 until l) {
                stream.read()
            }
            chunkName = IntArray(4) { stream.read() }
        }

        //skip until fmt block appears
        while(!chunkName.contentEquals(NAME_FMT)) {
            val l = stream.read32bit()
            for(i in 0 until l) {
                stream.read()
            }
            chunkName = IntArray(4) { stream.read() }
        }

//        if(!isFmt((chunkName))) throw UnsupportedAudioFileException("Illegal format!")

        val lengthFmt = stream.read32bit()

        if(lengthFmt != 16L) {
            throw UnsupportedAudioFileException("Only linear pcm is available!")
        }

        val fmt = stream.read16bit()

        if(fmt != 1) {
            throw UnsupportedAudioFileException("Only uncompressed linear pcm is available!")
        }

        val channel = stream.read16bit()

        val sampleRate = stream.read32bit()

        val avrBytePerSec = stream.read32bit()

        val blockSize = stream.read16bit()

        val bitPerSample = stream.read16bit()

        chunkName = IntArray(4) { stream.read() }

        //skip until fmt block appears
        while(!chunkName.contentEquals(NAME_DATA)) {
            val l = stream.read32bit()
            for(i in 0 until l) {
                stream.read()
            }
            chunkName = IntArray(4) { stream.read() }
        }

        val lengthData = stream.read32bit()

        val pcm = stream.readNBytes(lengthData.toInt())

        return WavInfo(channel, sampleRate, avrBytePerSec, blockSize, bitPerSample, pcm)

    }

}