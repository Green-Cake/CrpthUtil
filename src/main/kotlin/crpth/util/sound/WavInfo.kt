package crpth.util.sound

data class WavInfo(val channel: Int, val sampleRate: Long, val avrBytePerSec: Long, val blockSize: Int, val bitPerSample: Int, val pcm: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WavInfo

        if (channel != other.channel) return false
        if (sampleRate != other.sampleRate) return false
        if (avrBytePerSec != other.avrBytePerSec) return false
        if (blockSize != other.blockSize) return false
        if (bitPerSample != other.bitPerSample) return false
        if (!pcm.contentEquals(other.pcm)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = channel
        result = 31 * result + sampleRate.hashCode()
        result = 31 * result + avrBytePerSec.hashCode()
        result = 31 * result + blockSize
        result = 31 * result + bitPerSample
        result = 31 * result + pcm.contentHashCode()
        return result
    }

    fun getPcmAs16bit(): ShortArray = ShortArray(pcm.size / 2) { index ->
        ((pcm[index*2].toInt() shl 8) or (pcm[index*2 + 1].toInt())).toShort()
    }

    fun getPcm24(): IntArray = IntArray(pcm.size / 3) { index ->

        ((pcm[index*3+2].toInt() shl 16) or (pcm[index*3+1].toInt() shl 8) or (pcm[index*3].toInt()))

    }

    fun getPcm16From24(): ShortArray = ShortArray(pcm.size / 3) { index ->

        ((pcm[index*3+2].toInt() shl 8) or (pcm[index*3+1].toInt())).toShort()

    }

    fun convertToMono(useLeft: Boolean = false): WavInfo {

        if(channel == 1)
            return this

        if(channel != 2)
            throw IllegalStateException()

        return copy(channel = 1, avrBytePerSec = avrBytePerSec/2, pcm = ByteArray(pcm.size/2) { index ->

            if(bitPerSample == 8) {

                pcm[index * 2]

            } else {

                if(useLeft) {
                    if (index % 2 == 0)
                        pcm[index*2]
                    else
                        pcm[index*2-1]
                } else {
                    if (index % 2 == 0)
                        pcm[index*2 + 2]
                    else
                        pcm[index*2 + 1]
                }

            }

        })

    }

}