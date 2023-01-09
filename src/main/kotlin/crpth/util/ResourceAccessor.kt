package crpth.util

import crpth.util.vec.Vec2i
import org.lwjgl.BufferUtils
import java.awt.image.BufferedImage
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.sound.sampled.AudioSystem

object ResourceAccessor {

    fun loadTextureImageBufAndSize(stream: InputStream): Pair<ByteBuffer, Vec2i> {
        return loadTextureImageBufAndSize(ImageIO.read(stream))
    }

    fun loadTextureImageBufAndSize(buf: BufferedImage): Pair<ByteBuffer, Vec2i> {

        val size = Vec2i(buf.width, buf.height)

        val pixelsRaw = buf.getRGB(0, 0, size.x, size.y, null, 0, size.x)
        val pixels = BufferUtils.createByteBuffer(size.x * size.y * 4)

        for (i in 0 until size.y) for (j in 0 until size.x) {

            val p = pixelsRaw[i * size.x + j]
            pixels.put(((p shr 16) and 0xFF).toByte())
            pixels.put(((p shr 8) and 0xFF).toByte())
            pixels.put((p and 0xFF).toByte())
            pixels.put(((p shr 24) and 0xFF).toByte())

        }

        pixels.flip()

        return pixels to size

    }

    fun loadSeparatedImagesWithWidth(stream: InputStream, sizeForEach: Vec2i): Pair<Array<ByteBuffer>, Int> {

        val bi = ImageIO.read(stream)
        val size = Vec2i(bi.width, bi.height)

        if(size.x % sizeForEach.x != 0 || size.y % sizeForEach.y != 0)
            throw RuntimeException("the width or the height of the specified original image of TileSet is not correct for specified size-per-tile, ($size)")

        val pixelsRaw = bi.getRGB(0, 0, size.x, size.y, null, 0, size.x)


        val sizeX = bi.width / sizeForEach.x
        val sizeY = bi.height / sizeForEach.y

        val buffers = Array(sizeX * sizeY) { BufferUtils.createByteBuffer(sizeForEach.x * sizeForEach.y * 4) }

        for(cx in 0 until sizeX) for(cy in 0 until sizeY) {

            val pixels = buffers[sizeX*cy + cx]

            for (i in 0 until sizeForEach.y) for (j in 0 until sizeForEach.x) {

                val p = pixelsRaw[size.x * (sizeForEach.y * cy + i) + sizeForEach.x * cx + j]
                pixels.put(((p shr 16) and 0xFF).toByte())
                pixels.put(((p shr 8) and 0xFF).toByte())
                pixels.put((p and 0xFF).toByte())
                pixels.put(((p shr 24) and 0xFF).toByte())

            }

            pixels.flip()

        }

        return buffers to (size.x % sizeForEach.x)

    }

    private fun resizeBuffer(buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
        val newBuffer = BufferUtils.createByteBuffer(newCapacity)
        buffer.flip()
        newBuffer.put(buffer)
        return newBuffer
    }

    fun loadSoundFile(path: Path): ByteBuffer {

        var buffer: ByteBuffer

        if (!Files.isReadable(path))
            throw Exception()

        Files.newByteChannel(path).use { fc ->
            buffer = BufferUtils.createByteBuffer(fc.size().toInt() + 1)
            while (fc.read(buffer) != -1);
        }
        buffer.flip()
        return buffer.slice()
    }

    @Deprecated("UNUSED")
    fun loadSoundFile(domain: String, name: String) = loadSoundFile(Paths.get(ClassLoader.getSystemResource("assets/$domain/sounds/$name").toURI()))

    fun loadScriptSrc(domain: String, path: String) = String(ClassLoader.getSystemResourceAsStream("assets/$domain/script/$path.kts")!!.readBytes())

    fun getAudioInputStream(domain: String, path: String) = AudioSystem.getAudioInputStream(ClassLoader.getSystemResourceAsStream("assets/$domain/sounds/$path"))

}